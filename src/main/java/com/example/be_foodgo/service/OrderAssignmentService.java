package com.example.be_foodgo.service;

import com.example.be_foodgo.repository.DriverOrderRepository;
import com.example.be_foodgo.repository.DriverRepository;
import com.example.be_foodgo.repository.OrderRequestRepository;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class OrderAssignmentService {

    private static final Logger log = LoggerFactory.getLogger(OrderAssignmentService.class);

    private static final double BAN_KINH_TRAI_DAT = 6371.0;
    private static final double BAN_KINH_TIM_KM = 5.0;
    private static final int TIMEOUT_GIOY_SECONDS = 10;
    private static final double GOC_CHENH_LECH_HEADING_TOI_DA = 45.0;
    private static final String RDB_ACTIVE_DRIVERS = "active_drivers";

    private final OrderRequestRepository orderRequestRepository;
    private final DriverRepository driverRepository;
    private final FCMService fcmService;

    public OrderAssignmentService(
            OrderRequestRepository orderRequestRepository,
            DriverRepository driverRepository,
            FCMService fcmService) {
        this.orderRequestRepository = orderRequestRepository;
        this.driverRepository = driverRepository;
        this.fcmService = fcmService;
    }

    @Async
    public void batDauGánDon(String orderId, Double storeLat, Double storeLng,
                              Double deliveryLat, Double deliveryLng, Double deliveryHeading) {
        log.info("Bat dau gan don hang [{}] - store: ({},{}), delivery: ({},{}), heading: {}",
                orderId, storeLat, storeLng, deliveryLat, deliveryLng, deliveryHeading);

        try {
            List<String> nearbyDriverIds = timTaiXeGanNhat(storeLat, storeLng, null);
            if (nearbyDriverIds.isEmpty()) {
                log.warn("Khong co tai xe nao trong ban kinh {}km cho don [{}]", BAN_KINH_TIM_KM, orderId);
                thongBaoCuaHangKhongCoTaiXe(orderId);
                return;
            }

            List<String> candidateDriverIds = locTaiXeCungHuong(nearbyDriverIds, storeLat, storeLng, deliveryHeading);
            if (candidateDriverIds.isEmpty()) {
                candidateDriverIds = nearbyDriverIds;
            }

            guiYeuCauTaiXe(orderId, candidateDriverIds, storeLat, storeLng, deliveryLat, deliveryLng, deliveryHeading);
        } catch (Exception e) {
            log.error("Loi khi bat dau gan don [{}]: {}", orderId, e.getMessage());
        }
    }

    public List<String> timTaiXeGanNhat(double storeLat, double storeLng, Set<String> loaiTruIds) {
        List<DriverLocation> activeDrivers = layTatCaActiveDrivers();
        List<String> result = new ArrayList<>();

        for (DriverLocation driver : activeDrivers) {
            if (driver.lat == null || driver.lng == null) continue;
            if (loaiTruIds != null && loaiTruIds.contains(driver.driverId)) continue;

            double khoangCach = tinhKhoangCachHaversine(storeLat, storeLng, driver.lat, driver.lng);
            if (khoangCach <= BAN_KINH_TIM_KM) {
                result.add(driver.driverId);
            }
        }

        log.info("Tim thay {} tai xe trong ban kinh {}km", result.size(), BAN_KINH_TIM_KM);
        return result;
    }

    public List<String> locTaiXeCungHuong(List<String> driverIds, double storeLat, double storeLng,
                                           Double deliveryHeading) {
        if (driverIds.isEmpty() || deliveryHeading == null) return driverIds;

        List<String> result = new CopyOnWriteArrayList<>();

        for (String driverId : driverIds) {
            try {
                Map<String, Object> profile = driverRepository.findDriverProfileById(driverId);
                if (profile == null) continue;

                Double driverLat = toDouble(profile.get("lat"));
                Double driverLng = toDouble(profile.get("lng"));

                if (driverLat == null || driverLng == null) continue;

                double headingToStore = tinhHeading(driverLat, driverLng, storeLat, storeLng);
                if (chechCungHuong(headingToStore, deliveryHeading)) {
                    result.add(driverId);
                }
            } catch (Exception e) {
                log.warn("Loi khi loc tai xe cung huong [{}]: {}", driverId, e.getMessage());
            }
        }

        log.info("Loc cung huong: {} tai xe trong {} tai xe ban dau", result.size(), driverIds.size());
        return new ArrayList<>(result);
    }

    public void guiYeuCauTaiXe(String orderId, List<String> targetDriverIds,
                                 Double storeLat, Double storeLng,
                                 Double deliveryLat, Double deliveryLng,
                                 Double deliveryHeading) {
        if (targetDriverIds.isEmpty()) {
            thongBaoCuaHangKhongCoTaiXe(orderId);
            return;
        }

        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(TIMEOUT_GIOY_SECONDS);

        Map<String, Object> orderRequest = new HashMap<>();
        orderRequest.put("orderId", orderId);
        orderRequest.put("targetDriverIds", targetDriverIds);
        orderRequest.put("attemptedDriverIds", new ArrayList<String>());
        orderRequest.put("acceptedDriverId", null);
        orderRequest.put("storeLat", storeLat);
        orderRequest.put("storeLng", storeLng);
        orderRequest.put("deliveryLat", deliveryLat);
        orderRequest.put("deliveryLng", deliveryLng);
        orderRequest.put("deliveryHeading", deliveryHeading);
        orderRequest.put("expiresAt", expiresAt);
        orderRequest.put("status", "pending");
        orderRequest.put("createdAt", now);

        try {
            orderRequestRepository.save(orderRequest);
            log.info("Da tao order_request cho don [{}] voi {} tai xe, het han luc {}",
                    orderId, targetDriverIds.size(), expiresAt);
        } catch (Exception e) {
            log.error("Loi khi tao order_request cho don [{}]: {}", orderId, e.getMessage());
        }

        for (String driverId : targetDriverIds) {
            guiPushDenTaiXe(driverId, orderId);
        }
    }

    @Async
    public void xuLyTimeout(String docId, String orderId) {
        try {
            Map<String, Object> orderRequest = orderRequestRepository.findByOrderId(orderId);
            if (orderRequest == null) {
                log.warn("Khong tim thay order_request cho don [{}]", orderId);
                return;
            }

            String status = (String) orderRequest.get("status");
            if (!"pending".equals(status)) {
                return;
            }

            Instant expiresAt = toInstant(orderRequest.get("expiresAt"));
            if (expiresAt != null && expiresAt.isAfter(Instant.now())) {
                return;
            }

            log.info("Order [{}] da timeout, tim tai xe tiep theo", orderId);

            List<String> attemptedIds = (List<String>) orderRequest.get("attemptedDriverIds");
            if (attemptedIds == null) attemptedIds = new ArrayList<>();
            Set<String> loaiTru = new HashSet<>(attemptedIds);

            Double storeLat = toDouble(orderRequest.get("storeLat"));
            Double storeLng = toDouble(orderRequest.get("storeLng"));
            Double deliveryHeading = toDouble(orderRequest.get("deliveryHeading"));
            Double deliveryLat = toDouble(orderRequest.get("deliveryLat"));
            Double deliveryLng = toDouble(orderRequest.get("deliveryLng"));

            List<String> nextDrivers = timTaiXeGanNhat(storeLat, storeLng, loaiTru);
            List<String> candidates = locTaiXeCungHuong(nextDrivers, storeLat, storeLng, deliveryHeading);

            if (candidates.isEmpty()) {
                log.warn("Da thu tat ca tai xe gan nhung khong con ai, thong bao cua hang");
                thongBaoCuaHangKhongCoTaiXe(orderId);
                orderRequestRepository.updateFieldsByDocId(docId, Map.of("status", "failed"));
                return;
            }

            Map<String, Object> updates = new HashMap<>();
            List<String> newAttempted = new ArrayList<>(attemptedIds);
            newAttempted.addAll(candidates);
            updates.put("attemptedDriverIds", newAttempted);
            updates.put("targetDriverIds", candidates);
            updates.put("expiresAt", Instant.now().plusSeconds(TIMEOUT_GIOY_SECONDS));
            orderRequestRepository.updateFieldsByDocId(docId, updates);

            for (String driverId : candidates) {
                guiPushDenTaiXe(driverId, orderId);
            }

            log.info("Da gui yeu cau den {} tai xe moi cho don [{}]", candidates.size(), orderId);
        } catch (Exception e) {
            log.error("Loi khi xu ly timeout cho don [{}]: {}", orderId, e.getMessage());
        }
    }

    @Scheduled(fixedRate = 2000)
    public void kiemTraTimeout() {
        try {
            List<com.google.cloud.firestore.QueryDocumentSnapshot> expired =
                    orderRequestRepository.findExpiredPendingRequests(Instant.now());

            for (com.google.cloud.firestore.QueryDocumentSnapshot doc : expired) {
                String docId = doc.getId();
                String orderId = doc.getString("orderId");
                if (orderId != null) {
                    xuLyTimeout(docId, orderId);
                }
            }
        } catch (Exception e) {
            log.error("Loi khi kiem tra timeout: {}", e.getMessage());
        }
    }

    private void guiPushDenTaiXe(String driverId, String orderId) {
        try {
            Map<String, Object> profile = driverRepository.findDriverProfileById(driverId);
            String fcmToken = profile != null ? (String) profile.get("fcmToken") : null;

            if (fcmToken != null && !fcmToken.isBlank()) {
                Map<String, String> data = new HashMap<>();
                data.put("orderId", orderId);
                data.put("type", "new_order_request");
                fcmService.sendToDevice(fcmToken,
                        "Co don hang moi!",
                        "Ban co mot don hang cho nhan. Nhan 'Chap nhan' trong " + TIMEOUT_GIOY_SECONDS + "s.",
                        data);
            }

            Map<String, Object> notifData = new HashMap<>();
            notifData.put("type", 11);
            notifData.put("title", "Yeu cau nhan don moi");
            notifData.put("body", "Co don hang [" + orderId + "] cho ban. Chap nhan trong " + TIMEOUT_GIOY_SECONDS + "s.");
            notifData.put("orderId", orderId);
            notifData.put("referenceId", orderId);
            notifData.put("isRead", false);
            notifData.put("imageUrl", null);
            notifData.put("createdAt", Instant.now());

            driverRepository.getFirestore()
                    .collection("driver_profiles")
                    .document(driverId)
                    .collection("notifications")
                    .add(notifData);

            log.info("Da gui thong bao cho tai xe [{}] ve don [{}]", driverId, orderId);
        } catch (Exception e) {
            log.error("Loi khi gui thong bao den tai xe [{}]: {}", driverId, e.getMessage());
        }
    }

    private void thongBaoCuaHangKhongCoTaiXe(String orderId) {
        log.warn("Thong bao cua hang khong co tai xe cho don [{}]", orderId);
    }

    private List<DriverLocation> layTatCaActiveDrivers() {
        List<DriverLocation> drivers = new CopyOnWriteArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        try {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(RDB_ACTIVE_DRIVERS);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Boolean isActive = child.child("isActive").getValue(Boolean.class);
                        if (isActive == null || !isActive) continue;

                        Double lat = child.child("lat").getValue(Double.class);
                        Double lng = child.child("lng").getValue(Double.class);
                        String driverId = child.child("driverId").getValue(String.class);

                        if (driverId != null && lat != null && lng != null) {
                            drivers.add(new DriverLocation(driverId, lat, lng));
                        }
                    }
                    latch.countDown();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    log.error("Loi khi doc active_drivers tu Realtime DB: {}", error.getMessage());
                    latch.countDown();
                }
            });

            latch.await(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Loi khi lay active drivers: {}", e.getMessage());
        }

        return drivers;
    }

    private double tinhKhoangCachHaversine(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return BAN_KINH_TRAI_DAT * c;
    }

    private double tinhHeading(double fromLat, double fromLng, double toLat, double toLng) {
        double dLng = Math.toRadians(toLng - fromLng);
        double lat1 = Math.toRadians(fromLat);
        double lat2 = Math.toRadians(toLat);

        double x = Math.sin(dLng) * Math.cos(lat2);
        double y = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLng);

        double heading = Math.toDegrees(Math.atan2(x, y));
        return (heading + 360.0) % 360.0;
    }

    private boolean chechCungHuong(double heading1, double heading2) {
        double diff = Math.abs(heading1 - heading2);
        if (diff > 180.0) diff = 360.0 - diff;
        return diff <= GOC_CHENH_LECH_HEADING_TOI_DA;
    }

    private Double toDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return null;
    }

    private Instant toInstant(Object value) {
        if (value == null) return null;
        if (value instanceof com.google.protobuf.Timestamp) {
            com.google.protobuf.Timestamp ts = (com.google.protobuf.Timestamp) value;
            return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
        }
        if (value instanceof java.util.Date) return ((java.util.Date) value).toInstant();
        if (value instanceof Long) return Instant.ofEpochMilli((Long) value);
        return null;
    }

    private static class DriverLocation {
        String driverId;
        Double lat;
        Double lng;

        DriverLocation(String driverId, Double lat, Double lng) {
            this.driverId = driverId;
            this.lat = lat;
            this.lng = lng;
        }
    }
}
