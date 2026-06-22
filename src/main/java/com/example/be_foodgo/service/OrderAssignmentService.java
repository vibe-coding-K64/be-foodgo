package com.example.be_foodgo.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.example.be_foodgo.dto.DeliveryOrderDTO;
import com.example.be_foodgo.dto.DriverOrderRequestRealtimeEvent;
import com.example.be_foodgo.repository.OrderRequestRepository;
import com.example.be_foodgo.repository.StatsRepository;
import com.example.be_foodgo.repository.StoreRepository;
import com.example.be_foodgo.repository.WalletRepository;

@Service
public class OrderAssignmentService {

    private static final Logger log = LoggerFactory.getLogger(OrderAssignmentService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final double BAN_KINH_TRAI_DAT = 6371.0;
    private static final double BAN_KINH_TIM_KM = 5.0;
    private static final int TIMEOUT_GIOY_SECONDS = 10;
    private static final double GOC_CHENH_LECH_HEADING_TOI_DA = 45.0;

    private final OrderRequestRepository orderRequestRepository;
    private final StatsRepository statsRepository;
    private final StoreRepository storeRepository;
    private final WalletRepository walletRepository;
    private final FCMService fcmService;
    private final SimpMessagingTemplate messagingTemplate;
    private final DeliveryOrderService deliveryOrderService;
    private final MapboxService mapboxService;

    private final Set<String> processingOrders = ConcurrentHashMap.newKeySet();
    private final Set<String> timeoutOrdersInFlight = ConcurrentHashMap.newKeySet();

    public OrderAssignmentService(
            OrderRequestRepository orderRequestRepository,
            StatsRepository statsRepository,
            StoreRepository storeRepository,
            WalletRepository walletRepository,
            FCMService fcmService,
            SimpMessagingTemplate messagingTemplate,
            DeliveryOrderService deliveryOrderService,
            MapboxService mapboxService) {
        this.orderRequestRepository = orderRequestRepository;
        this.statsRepository = statsRepository;
        this.storeRepository = storeRepository;
        this.walletRepository = walletRepository;
        this.fcmService = fcmService;
        this.messagingTemplate = messagingTemplate;
        this.deliveryOrderService = deliveryOrderService;
        this.mapboxService = mapboxService;
    }

    public void triggerAssignmentForOrder(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            log.warn("Bo qua trigger gan don vi orderId rong.");
            return;
        }

        if (!processingOrders.add(orderId)) {
            log.info("Bo qua trigger gan don cho [{}] vi dang duoc xu ly.", orderId);
            return;
        }

        xuLyGanDonTuFirestore(orderId);
    }

    private void xuLyGanDonTuFirestore(String orderId) {
        CompletableFuture.runAsync(() -> {
            try {
                AssignmentContext context = taoAssignmentContext(orderId);
                if (context == null) {
                    return;
                }

                if (daCoOrderRequestDangPending(orderId)) {
                    log.info("Bo qua trigger gan don cho [{}] vi da ton tai order_request pending.", orderId);
                    return;
                }

                batDauGanDon(orderId,
                        context.storeLat,
                        context.storeLng,
                        context.deliveryLat,
                        context.deliveryLng,
                        context.deliveryHeading);
            } catch (Exception e) {
                log.error("Loi xu ly gan don tu Firestore [{}]: {}", orderId, e.getMessage(), e);
            } finally {
                processingOrders.remove(orderId);
            }
        });
    }

    private AssignmentContext taoAssignmentContext(String orderId) {
        try {
            Map<String, Object> orderData = statsRepository.findOrderRawById(orderId);
            if (orderData == null) {
                log.warn("Khong tim thay don hang [{}] de gan tai xe.", orderId);
                return null;
            }

            int orderStatus = toInt(orderData.get("status"));
            if (orderStatus != 1) {
                log.info("Bo qua gan don [{}] vi status hien tai la {} thay vi 1.", orderId, orderStatus);
                return null;
            }

            String storeId = orderData.get("storeId") != null ? String.valueOf(orderData.get("storeId")) : null;
            Double storeLat = null;
            Double storeLng = null;
            var store = storeId != null ? storeRepository.getStoreById(storeId) : null;
            if (store != null) {
                storeLat = store.getLat();
                storeLng = store.getLng();
            }

            Double deliveryLat = toDouble(orderData.get("deliveryLat"));
            Double deliveryLng = toDouble(orderData.get("deliveryLng"));
            Double deliveryHeading = toDouble(orderData.get("deliveryHeading"));

            if (deliveryHeading == null && storeLat != null && storeLng != null
                    && deliveryLat != null && deliveryLng != null) {
                deliveryHeading = tinhHeading(storeLat, storeLng, deliveryLat, deliveryLng);
            }

            if (storeLat == null || storeLng == null || deliveryLat == null || deliveryLng == null) {
                log.warn("Khong du du lieu vi tri de gan don [{}]. store=({},{}), delivery=({},{}).",
                        orderId, storeLat, storeLng, deliveryLat, deliveryLng);
            }

            return new AssignmentContext(storeLat, storeLng, deliveryLat, deliveryLng, deliveryHeading);
        } catch (Exception e) {
            log.error("Loi tao assignment context cho don [{}]: {}", orderId, e.getMessage(), e);
            return null;
        }
    }

    private boolean daCoOrderRequestDangPending(String orderId) {
        try {
            Map<String, Object> existing = orderRequestRepository.findByOrderId(orderId);
            if (existing == null) {
                return false;
            }
            String status = existing.get("status") != null ? String.valueOf(existing.get("status")) : null;
            return "pending".equalsIgnoreCase(status);
        } catch (Exception e) {
            log.warn("Khong the kiem tra order_request pending cho [{}]: {}", orderId, e.getMessage());
            return false;
        }
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

    @Async
    public void batDauGanDon(String orderId, Double storeLat, Double storeLng,
                              Double deliveryLat, Double deliveryLng, Double deliveryHeading) {
        log.info("Bat dau gan don hang [{}] - store: ({},{}), delivery: ({},{}), heading: {}",
                orderId, storeLat, storeLng, deliveryLat, deliveryLng, deliveryHeading);

        try {
            if (storeLat == null || storeLng == null) {
                log.warn("Khong the gan don [{}] vi thieu vi tri cua hang.", orderId);
                thongBaoCuaHangKhongCoTaiXe(orderId);
                return;
            }

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
            log.error("Loi khi bat dau gan don [{}]: {}", orderId, e.getMessage(), e);
        }
    }

    public List<String> timTaiXeGanNhat(double storeLat, double storeLng, Set<String> loaiTruIds) {
        List<DriverLocation> activeDrivers = layTatCaActiveDrivers();
        List<String> result = new ArrayList<>();

        for (DriverLocation driver : activeDrivers) {
            if (driver.lat == null || driver.lng == null) continue;
            if (loaiTruIds != null && loaiTruIds.contains(driver.driverId)) continue;

            double khoangCach = mapboxService.isEnabled()
                    ? mapboxService.getDriverToStoreDistanceKm(driver.lat, driver.lng, storeLat, storeLng)
                    : tinhKhoangCachHaversine(storeLat, storeLng, driver.lat, driver.lng);
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
                Map<String, Object> profile = walletRepository.findDriverProfileById(driverId);
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

        boolean orderRequestSaved = false;
        try {
            orderRequestRepository.save(orderRequest);
            orderRequestSaved = true;
            log.info("Da tao order_request cho don [{}] voi {} tai xe, het han luc {}",
                    orderId, targetDriverIds.size(), expiresAt);
        } catch (Exception e) {
            log.error("Loi khi tao order_request cho don [{}]: {}", orderId, e.getMessage(), e);
        }

        for (String driverId : targetDriverIds) {
            boolean driverRequestSaved = false;
            String driverRequestId = null;
            if (orderRequestSaved) {
                try {
                    driverRequestId = orderRequestRepository.savePerDriver(driverId, orderRequest);
                    driverRequestSaved = true;
                } catch (Exception e) {
                    log.error("Loi khi ghi order_request cho tai xe [{}]: {}", driverId, e.getMessage(), e);
                }
            }

            if (orderRequestSaved && driverRequestSaved && driverRequestId != null) {
                guiRealtimeVaPushDenTaiXe(driverId, orderId, driverRequestId, expiresAt, storeLat, storeLng,
                        deliveryLat, deliveryLng, deliveryHeading);
            } else {
                log.warn("Bo qua gui push cho tai xe [{}] vi order_request chua duoc luu day du cho don [{}].",
                        driverId, orderId);
            }
        }
    }

    @Async
    public void xuLyTimeout(String docId, String orderId) {
        if (!timeoutOrdersInFlight.add(orderId)) {
            log.debug("Bo qua timeout xu ly cho don [{}] vi dang co task timeout khac.", orderId);
            return;
        }

        try {
            Map<String, Object> orderRequest = orderRequestRepository.findByOrderId(orderId);
            if (orderRequest == null) {
                log.warn("Khong tim thay order_request cho don [{}]", orderId);
                return;
            }

            if (docId != null && orderRequest.get("id") != null && !docId.equals(orderRequest.get("id"))) {
                log.debug("Bo qua timeout cu cho don [{}] vi doc hien tai da thay doi tu {} sang {}.",
                        orderId, docId, orderRequest.get("id"));
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

            List<String> currentTargets = toStringList(orderRequest.get("targetDriverIds"));
            List<String> attemptedIds = toStringList(orderRequest.get("attemptedDriverIds"));
            Set<String> loaiTru = new HashSet<>(attemptedIds);
            loaiTru.addAll(currentTargets);

            Double storeLat = toDouble(orderRequest.get("storeLat"));
            Double storeLng = toDouble(orderRequest.get("storeLng"));
            Double deliveryHeading = toDouble(orderRequest.get("deliveryHeading"));
            Double deliveryLat = toDouble(orderRequest.get("deliveryLat"));
            Double deliveryLng = toDouble(orderRequest.get("deliveryLng"));

            Map<String, Object> expireCurrentTargets = new HashMap<>();
            expireCurrentTargets.put("attemptedDriverIds", new ArrayList<>(loaiTru));
            expireCurrentTargets.put("targetDriverIds", List.of());
            orderRequestRepository.updateFieldsByDocId(docId, expireCurrentTargets);

            List<String> nextDrivers = timTaiXeGanNhat(storeLat, storeLng, loaiTru);
            List<String> candidates = locTaiXeCungHuong(nextDrivers, storeLat, storeLng, deliveryHeading);
            if (candidates.isEmpty()) {
                candidates = nextDrivers;
            }

            if (candidates.isEmpty()) {
                log.warn("Da thu tat ca tai xe gan nhung khong con ai, thong bao cua hang");
                thongBaoCuaHangKhongCoTaiXe(orderId);
                orderRequestRepository.updateFieldsByDocId(docId, Map.of("status", "failed", "targetDriverIds", List.of()));
                return;
            }

            Map<String, Object> updates = new HashMap<>();
            List<String> newAttempted = new ArrayList<>(loaiTru);
            updates.put("attemptedDriverIds", newAttempted);
            updates.put("targetDriverIds", candidates);
            Instant nextExpiresAt = Instant.now().plusSeconds(TIMEOUT_GIOY_SECONDS);
            updates.put("expiresAt", nextExpiresAt);
            orderRequestRepository.updateFieldsByDocId(docId, updates);

            Map<String, Object> refreshedOrderRequest = orderRequestRepository.findByOrderId(orderId);
            if (refreshedOrderRequest == null) {
                log.warn("Khong the tai lai order_request sau timeout cho don [{}]", orderId);
                return;
            }

            for (String driverId : candidates) {
                String driverRequestId = null;
                try {
                    driverRequestId = orderRequestRepository.savePerDriver(driverId, refreshedOrderRequest);
                } catch (Exception e) {
                    log.error("Loi khi ghi order_request timeout cho tai xe [{}]: {}", driverId, e.getMessage(), e);
                }

                if (driverRequestId != null) {
                    guiRealtimeVaPushDenTaiXe(driverId, orderId, driverRequestId, nextExpiresAt, storeLat, storeLng,
                            deliveryLat, deliveryLng, deliveryHeading);
                } else {
                    log.warn("Bo qua gui timeout request cho tai xe [{}] vi khong tao duoc request tam cho don [{}].",
                            driverId, orderId);
                }
            }

            log.info("Da gui yeu cau den {} tai xe moi cho don [{}]", candidates.size(), orderId);
        } catch (Exception e) {
            log.error("Loi khi xu ly timeout cho don [{}]: {}", orderId, e.getMessage(), e);
        } finally {
            timeoutOrdersInFlight.remove(orderId);
        }
    }

    @Scheduled(fixedRateString = "${order.assignment.timeout.check.interval:2000}")
    public void kiemTraTimeout() {
        try {
            List<Map<String, Object>> expired =
                    orderRequestRepository.findExpiredPendingRequests(Instant.now());

            for (Map<String, Object> doc : expired) {
                String docId = (String) doc.get("id");
                String orderId = (String) doc.get("orderId");
                if (orderId != null) {
                    xuLyTimeout(docId, orderId);
                }
            }
        } catch (Exception e) {
            log.error("Loi khi kiem tra timeout: {}", e.getMessage(), e);
        }
    }

    private void guiRealtimeVaPushDenTaiXe(String driverId, String orderId, String requestId, Instant expiresAt,
                                            Double storeLat, Double storeLng,
                                            Double deliveryLat, Double deliveryLng,
                                            Double deliveryHeading) {
        log.info("[ORDER_DISPATCH] Bat dau dispatch don moi: orderId={}, requestId={}, driverId={}, expiresAt={}, storeLat={}, storeLng={}, deliveryLat={}, deliveryLng={}, deliveryHeading={}",
                orderId, requestId, driverId, expiresAt, storeLat, storeLng, deliveryLat, deliveryLng, deliveryHeading);
        guiRealtimeOrderRequestDenTaiXe(driverId, orderId, requestId, expiresAt, storeLat, storeLng,
                deliveryLat, deliveryLng, deliveryHeading);
        guiPushDenTaiXe(driverId, orderId, requestId);
    }

    private void guiRealtimeOrderRequestDenTaiXe(String driverId, String orderId, String requestId, Instant expiresAt,
                                                 Double storeLat, Double storeLng,
                                                 Double deliveryLat, Double deliveryLng,
                                                 Double deliveryHeading) {
        try {
            Double estimatedEarning = tinhThuNhapUocTinh(orderId);
            DeliveryOrderDTO realtimeOrder = deliveryOrderService
                    .mapToDriverOrderRealtimeDTO(orderId, requestId, expiresAt, estimatedEarning, deliveryHeading);
            Integer expiresInSeconds = realtimeOrder.getExpiresInSeconds();

            DriverOrderRequestRealtimeEvent event = DriverOrderRequestRealtimeEvent.builder()
                    .event("ORDER_REQUEST")
                    .message("Co don hang moi")
                    .orderId(orderId)
                    .requestId(requestId)
                    .estimatedEarning(estimatedEarning)
                    .expiresAt(expiresAt)
                    .expiresInSeconds(expiresInSeconds)
                    .deliveryHeading(deliveryHeading)
                    .order(realtimeOrder)
                    .build();

            log.info("[ORDER_DISPATCH][STOMP] Chuan bi gui user-destination: principalName={}, destination=/user/queue/order-request, internalDestination=/queue/order-request, orderId={}, requestId={}, expiresAt={}, expiresInSeconds={}",
                    driverId, orderId, requestId, expiresAt, expiresInSeconds);
            Map<String, Object> driverProfile = walletRepository.findDriverProfileById(driverId);
            Object profileUserId = driverProfile != null ? driverProfile.get("userId") : null;
            String profileDocumentId = driverId;
            boolean matchesUserId = profileUserId instanceof String profileUserIdValue
                    && driverId.equals(profileUserIdValue);
            log.info("[ORDER_DISPATCH][STOMP] Driver identity cross-check: dispatchDriverId={}, driverProfile.userId={}, driverProfile.documentId={}, matchesUserId={}, matchesDocumentId={}",
                    driverId,
                    profileUserId,
                    profileDocumentId,
                    matchesUserId,
                    true);
            log.info("[STOMP DEBUG] convertAndSendToUser → principal='{}', destination='/queue/order-request', orderId='{}'", driverId, orderId);
            log.info("[ORDER_DISPATCH][STOMP] Payload ORDER_REQUEST: {}", toJsonSafely(event));

            messagingTemplate.convertAndSendToUser(driverId, "/queue/order-request", event);
            log.info("Da gui websocket ORDER_REQUEST cho tai xe [{}] voi don [{}], requestId={}, het han luc {}",
                    driverId, orderId, requestId, expiresAt);
        } catch (Exception e) {
            log.error("Loi khi gui websocket ORDER_REQUEST den tai xe [{}] cho don [{}], requestId={}: {}",
                    driverId, orderId, requestId, e.getMessage(), e);
        }
    }

    private void guiPushDenTaiXe(String driverId, String orderId, String requestId) {
        try {
            Map<String, Object> profile = walletRepository.findDriverProfileById(driverId);
            String fcmToken = profile != null ? (String) profile.get("fcmToken") : null;

            if (fcmToken != null && !fcmToken.isBlank()) {
                Double estimatedEarning = tinhThuNhapUocTinh(orderId);
                DeliveryOrderDTO realtimeOrder = deliveryOrderService
                        .mapToDriverOrderRealtimeDTO(orderId, requestId, null, estimatedEarning, null);

                Map<String, String> data = new HashMap<>();
                data.put("orderId", orderId);
                data.put("requestId", requestId);
                data.put("driverId", driverId);
                data.put("type", "new_order_request");
                putIfPresent(data, "estimatedEarning", estimatedEarning);
                putIfPresent(data, "paymentMethod", realtimeOrder.getPaymentMethod());
                putIfPresent(data, "paymentStatus", realtimeOrder.getPaymentStatus());
                putIfPresent(data, "totalAmount", realtimeOrder.getTotalAmount());
                putIfPresent(data, "deliveryFee", realtimeOrder.getDeliveryFee());
                putIfPresent(data, "finalAmount", realtimeOrder.getFinalAmount());
                putIfPresent(data, "driverCollectAmount", realtimeOrder.getDriverCollectAmount());
                putIfPresent(data, "deliveryAddress", realtimeOrder.getDeliveryAddress());
                putIfPresent(data, "storeName", realtimeOrder.getStoreName());
                log.info("[ORDER_DISPATCH][FCM] Chuan bi gui FCM: driverId={}, orderId={}, requestId={}, tokenPreview={}...", driverId, orderId, requestId,
                        fcmToken.substring(0, Math.min(12, fcmToken.length())));
                log.info("[ORDER_DISPATCH][FCM] Payload data: {}", data);
                fcmService.sendToDevice(fcmToken,
                        "Co don hang moi!",
                        "Ban co mot don hang cho nhan. Nhan 'Chap nhan' trong " + TIMEOUT_GIOY_SECONDS + "s.",
                        data);
            } else {
                log.warn("[ORDER_DISPATCH][FCM] Khong co fcmToken hop le cho driverId={}, orderId={}, requestId={}",
                        driverId, orderId, requestId);
            }

            Map<String, Object> notifData = new HashMap<>();
            notifData.put("type", 11);
            notifData.put("title", "Yeu cau nhan don moi");
            notifData.put("body", "Co don hang [" + orderId + "] cho ban. Chap nhan trong " + TIMEOUT_GIOY_SECONDS + "s.");
            notifData.put("orderId", orderId);
            notifData.put("requestId", requestId);
            notifData.put("referenceId", orderId);
            notifData.put("isRead", false);
            notifData.put("imageUrl", null);
            notifData.put("createdAt", Instant.now());

            walletRepository.getFirestore()
                    .collection("driver_profiles")
                    .document(driverId)
                    .collection("notifications")
                    .add(notifData);

            log.info("Da gui thong bao cho tai xe [{}] ve don [{}], requestId={}", driverId, orderId, requestId);
        } catch (Exception e) {
            log.error("Loi khi gui thong bao den tai xe [{}] cho don [{}], requestId={}: {}", driverId, orderId, requestId, e.getMessage(), e);
        }
    }

    private void thongBaoCuaHangKhongCoTaiXe(String orderId) {
        log.warn("Thong bao cua hang khong co tai xe cho don [{}]", orderId);
    }

    private String toJsonSafely(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            return "<json-serialize-error:" + e.getMessage() + ">";
        }
    }

    private void putIfPresent(Map<String, String> data, String key, Object value) {
        if (data == null || key == null || key.isBlank() || value == null) {
            return;
        }
        data.put(key, String.valueOf(value));
    }

    private Double tinhThuNhapUocTinh(String orderId) {
        try {
            Map<String, Object> orderData = statsRepository.findOrderRawById(orderId);
            if (orderData == null) {
                return null;
            }
            Double deliveryFee = toDouble(orderData.get("deliveryFee") != null
                    ? orderData.get("deliveryFee")
                    : orderData.get("shippingFee"));
            return deliveryFee != null ? deliveryFee : 0.0;
        } catch (Exception e) {
            log.warn("Khong the tinh estimatedEarning cho don [{}]: {}", orderId, e.getMessage());
            return null;
        }
    }

    private List<DriverLocation> layTatCaActiveDrivers() {
        List<DriverLocation> drivers = new CopyOnWriteArrayList<>();

        try {
            List<Map<String, Object>> activeProfiles = walletRepository.findActiveDriverProfiles();
            for (Map<String, Object> profile : activeProfiles) {
                if (profile == null) continue;

                String driverId = profile.get("userId") != null
                        ? String.valueOf(profile.get("userId"))
                        : profile.get("id") != null ? String.valueOf(profile.get("id")) : null;
                Double lat = toDouble(profile.get("lat"));
                Double lng = toDouble(profile.get("lng"));

                if (driverId != null && lat != null && lng != null) {
                drivers.add(new DriverLocation(driverId, lat, lng));
            }
            }
        } catch (Exception e) {
            log.error("Loi khi lay active drivers tu Firestore: {}", e.getMessage(), e);
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

    private List<String> toStringList(Object value) {
        if (!(value instanceof List<?> list)) {
            return new ArrayList<>();
        }

        List<String> result = new ArrayList<>();
        for (Object item : list) {
            if (item != null) {
                result.add(String.valueOf(item));
            }
        }
        return result;
    }

    private int toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number number) return number.intValue();
        if (value instanceof String text) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }

    private Instant toInstant(Object value) {
        if (value == null) return null;
        if (value instanceof java.util.Date) return ((java.util.Date) value).toInstant();
        if (value instanceof Instant instant) return instant;
        if (value instanceof Long) return Instant.ofEpochMilli((Long) value);
        if (value instanceof Number) return Instant.ofEpochMilli(((Number) value).longValue());
        return null;
    }

    private record AssignmentContext(
            Double storeLat,
            Double storeLng,
            Double deliveryLat,
            Double deliveryLng,
            Double deliveryHeading
    ) {}

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
