package com.example.be_foodgo.service;

import com.example.be_foodgo.constant.DeliveryOrderStatus;
import com.example.be_foodgo.dto.DeliveryOrderDTO;
import com.example.be_foodgo.dto.DriverOrderActionResultDTO;
import com.example.be_foodgo.exception.BusinessException;
import com.example.be_foodgo.repository.OrderRequestRepository;
import com.example.be_foodgo.repository.StatsRepository;
import com.example.be_foodgo.repository.VoucherRepository;
import com.example.be_foodgo.repository.WalletRepository;
import com.google.cloud.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class DeliveryOrderService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryOrderService.class);

    private final StatsRepository statsRepository;
    private final WalletRepository walletRepository;
    private final WalletService walletService;
    private final OrderRequestRepository orderRequestRepository;
    private final VoucherRepository voucherRepository;

    public DeliveryOrderService(StatsRepository statsRepository,
                                WalletRepository walletRepository,
                                WalletService walletService,
                                OrderRequestRepository orderRequestRepository,
                                VoucherRepository voucherRepository) {
        this.statsRepository = statsRepository;
        this.walletRepository = walletRepository;
        this.walletService = walletService;
        this.orderRequestRepository = orderRequestRepository;
        this.voucherRepository = voucherRepository;
    }

    public List<DeliveryOrderDTO> getAvailableOrders() {
        log.info("Bat dau lay danh sach don hang kha dung");
        try {
            List<com.google.cloud.firestore.QueryDocumentSnapshot> docs = statsRepository.findAvailableOrders();
            List<DeliveryOrderDTO> orders = new ArrayList<>();

            for (com.google.cloud.firestore.QueryDocumentSnapshot doc : docs) {
                Map<String, Object> data = doc.getData();
                DeliveryOrderDTO dto = buildDeliveryOrderDTO(doc.getId(), data);
                orders.add(dto);
            }

            log.info("Tim thay {} don hang kha dung", orders.size());
            return orders;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi lay don hang kha dung: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        } catch (java.util.concurrent.ExecutionException e) {
            log.error("Loi khi lay don hang kha dung: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        }
    }

    public DeliveryOrderDTO getOrderDetail(String orderId) {
        log.info("Bat dau lay chi tiet don hang cho tai xe: orderId={}", orderId);
        try {
            Map<String, Object> orderData = statsRepository.findOrderRawById(orderId);
            if (orderData == null) {
                throw BusinessException.donHangKhongTimThay(orderId);
            }

            DeliveryOrderDTO dto = buildDeliveryOrderDTO(orderId, orderData);

            log.info("Lay chi tiet don hang thanh cong: orderId={}", orderId);
            return dto;
        } catch (BusinessException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi lay chi tiet don hang cho tai xe: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        } catch (java.util.concurrent.ExecutionException e) {
            log.error("Loi khi lay chi tiet don hang cho tai xe: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        }
    }

    public DeliveryOrderDTO acceptOrder(String orderId, String userId) {
        log.info("Bat dau nhan don hang: orderId={}, userId={}", orderId, userId);
        try {
            Map<String, Object> driverProfileData = walletRepository.findDriverProfileById(userId);
            if (driverProfileData == null) {
                throw BusinessException.hoSoTaiXeChuaTonTai(userId);
            }

            Map<String, Object> userData = walletRepository.findUserById(userId);
            String driverName = userData != null ? (String) userData.get("fullName") : "Tai xe";
            String driverPhone = userData != null ? (String) userData.get("phoneNumber") : "";
            String vehiclePlate = (String) driverProfileData.get("vehiclePlate");

            final String finalDriverName = driverName != null ? driverName : "Tai xe";
            final String finalDriverPhone = driverPhone != null ? driverPhone : "";
            final String finalVehiclePlate = vehiclePlate != null ? vehiclePlate : "";

            try {
                statsRepository.acceptOrderInTransaction(
                        orderId, userId, finalDriverName, finalDriverPhone, finalVehiclePlate);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw BusinessException.loiHeThong(e.getMessage());
            } catch (java.util.concurrent.ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof IllegalStateException) {
                    String msg = cause.getMessage();
                    if (msg.startsWith("ORDER_NOT_FOUND:")) {
                        throw BusinessException.donHangKhongTimThay(orderId);
                    }
                    if (msg.equals("ORDER_STATUS_INVALID")) {
                        throw BusinessException.trangThaiDonHangKhongHopLe(orderId, 1, "nhận");
                    }
                    if (msg.equals("ORDER_ALREADY_ASSIGNED")) {
                        throw BusinessException.donHangDaCoTaiXe(orderId);
                    }
                }
                throw BusinessException.loiHeThong(e.getMessage());
            }

            Map<String, Object> orderData = statsRepository.findOrderRawById(orderId);
            DeliveryOrderDTO dto = buildDeliveryOrderDTO(orderId, orderData);

            log.info("Nhan don hang thanh cong: orderId={}, userId={}", orderId, userId);
            return dto;
        } catch (BusinessException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi nhan don hang: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        } catch (java.util.concurrent.ExecutionException e) {
            log.error("Loi khi nhan don hang: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        }
    }

    public DriverOrderActionResultDTO declineOrder(String orderId, String userId) {
        log.info("Bat dau tu choi don hang: orderId={}, userId={}", orderId, userId);
        try {
            Map<String, Object> notifData = new HashMap<>();
            notifData.put("type", 13);
            notifData.put("title", "Đơn hàng đã được giao cho tài xế khác");
            notifData.put("body", "Đơn hàng [" + orderId + "] đã được tài xế khác nhận. Vui lòng chờ đơn hàng tiếp theo.");
            notifData.put("orderId", orderId);
            notifData.put("referenceId", orderId);
            notifData.put("isRead", false);
            notifData.put("imageUrl", null);
            notifData.put("createdAt", new Date());

            statsRepository.saveDriverNotification(userId, notifData);

            List<com.google.cloud.firestore.QueryDocumentSnapshot> oldNotifs = statsRepository
                    .findDriverNotificationsByTypeAndOrderId(userId, 11, orderId);
            for (com.google.cloud.firestore.QueryDocumentSnapshot doc : oldNotifs) {
                statsRepository.deleteNotification(doc.getReference().getPath());
            }

            log.info("Tu choi don hang thanh cong: orderId={}, userId={}", orderId, userId);
            return DriverOrderActionResultDTO.builder()
                    .orderId(orderId)
                    .requestId(null)
                    .status("DECLINED")
                    .build();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi tu choi don hang: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        } catch (java.util.concurrent.ExecutionException e) {
            log.error("Loi khi tu choi don hang: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        }
    }

    public DeliveryOrderDTO updateOrderStatus(String orderId, String userId, int newStatus) {
        log.info("Bat dau cap nhat trang thai don hang: orderId={}, userId={}, newStatus={}", orderId, userId, newStatus);
        try {
            Map<String, Object> orderData = statsRepository.findOrderRawById(orderId);
            if (orderData == null) {
                throw BusinessException.donHangKhongTimThay(orderId);
            }

            int currentStatus = getOrderStatusValueFromMap(orderData);
            String driverIdOfOrder = (String) orderData.get("driverId");

            if (!userId.equals(driverIdOfOrder)) {
                throw BusinessException.khongPhaiChuDonHang(orderId);
            }

            if (newStatus == DeliveryOrderStatus.DELIVERING) {
                if (currentStatus != DeliveryOrderStatus.WAITING_DRIVER) {
                    throw BusinessException.trangThaiDonHangKhongHopLe(orderId, currentStatus, "cập nhật trạng thái sang 'đã lấy hàng'");
                }
                Map<String, Object> pickupUpdates = new HashMap<>();
                pickupUpdates.put("status", DeliveryOrderStatus.DELIVERING);
                pickupUpdates.put("deliveryStep", "ON_THE_WAY");
                pickupUpdates.put("pickedUpAt", Instant.now());
                pickupUpdates.put("updatedAt", Instant.now());
                statsRepository.updateOrderFields(orderId, pickupUpdates);
                Map<String, Object> dtoOrderData = statsRepository.findOrderRawById(orderId);
                return buildDeliveryOrderDTO(orderId, dtoOrderData);
            }

            if (currentStatus != DeliveryOrderStatus.DELIVERING) {
                throw BusinessException.trangThaiDonHangKhongHopLe(orderId, currentStatus, "cập nhật trạng thái");
            }

            Map<String, Object> driverUpdates = new HashMap<>();
            driverUpdates.put("currentOrderId", null);
            driverUpdates.put("isAvailable", true);
            driverUpdates.put("updatedAt", new Date());

            if (newStatus == 3) {
                String customerId = (String) orderData.get("userId");
                Double deliveryFee = toDouble(orderData.get("deliveryFee") != null ? orderData.get("deliveryFee") : orderData.get("shippingFee"));
                String storeId = (String) orderData.get("storeId");
                Double finalAmount = toDouble(orderData.get("finalAmount"));
                Double totalAmount = toDouble(orderData.get("totalAmount"));

                Double merchantIncome = 0.0;
                if (finalAmount != null && finalAmount > 0) {
                    merchantIncome = finalAmount - (deliveryFee != null ? deliveryFee : 0.0);
                } else if (totalAmount != null) {
                    merchantIncome = totalAmount;
                }

                Map<String, Object> orderUpdates = new HashMap<>();
                orderUpdates.put("status", 3);
                orderUpdates.put("updatedAt", new Date());
                statsRepository.updateOrderFields(orderId, orderUpdates);

                Map<String, Object> driverUpdates = new HashMap<>();
                driverUpdates.put("currentOrderId", null);
                driverUpdates.put("isAvailable", true);
                driverUpdates.put("totalTrips",
                        com.google.cloud.firestore.FieldValue.increment(1));
                driverUpdates.put("updatedAt", Instant.now());
                walletRepository.updateDriverProfileFields(userId, driverUpdates);

                if (customerId != null) {
                    taoThongBaoKhachHang(customerId, orderId);
                    congDiemLoyaltyKhachHang(customerId, finalAmount);
                }

                if (deliveryFee != null && deliveryFee > 0) {
                    walletService.taoGiaoDichThuNhap(userId, orderId, deliveryFee);
                }

                String orderCode = (String) orderData.get("code");
                if (orderCode == null || orderCode.trim().isEmpty()) {
                    orderCode = orderId.length() >= 6 ? orderId.substring(orderId.length() - 6).toUpperCase() : "ORDER";
                }

                if (isCashPayment(orderData.get("paymentMethod"))) {
                    Double codAmount = finalAmount != null ? finalAmount : (totalAmount != null ? totalAmount : 0.0);
                    if (codAmount > 0) {
                        walletService.createDriverCodDebitTransaction(userId, orderId, orderCode, codAmount);
                    }
                }

                if (storeId != null && merchantIncome != null && merchantIncome > 0) {
                    walletService.createMerchantIncomeTransaction(storeId, orderId, orderCode, merchantIncome);
                }

                log.info("Don hang hoan thanh: orderId={}, tien cuoc={}", orderId, deliveryFee);
            } else {
                Map<String, Object> orderUpdates = new HashMap<>();
                orderUpdates.put("status", DeliveryOrderStatus.WAITING_DRIVER);
                orderUpdates.put("deliveryStep", "WAITING_DRIVER");
                orderUpdates.put("driverId", null);
                orderUpdates.put("driverName", null);
                orderUpdates.put("driverPhone", null);
                orderUpdates.put("vehiclePlate", null);
                orderUpdates.put("updatedAt", new Date());
                statsRepository.updateOrderFields(orderId, orderUpdates);

                Map<String, Object> driverUpdates = new HashMap<>();
                driverUpdates.put("currentOrderId", null);
                driverUpdates.put("isAvailable", true);
                driverUpdates.put("updatedAt", Instant.now());
                walletRepository.updateDriverProfileFields(userId, driverUpdates);
                log.info("Don hang da bi tai xe tu choi giao hang. Reset ve cho tai xe (status 1) - orderId={}", orderId);
            }

            Map<String, Object> updatedOrderData = statsRepository.findOrderRawById(orderId);
            return buildDeliveryOrderDTO(orderId, updatedOrderData);
        } catch (BusinessException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi cap nhat trang thai don hang: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        } catch (java.util.concurrent.ExecutionException e) {
            log.error("Loi khi cap nhat trang thai don hang: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        }
    }

    public DeliveryOrderDTO getCurrentOrder(String userId) {
        log.info("Bat dau lay don hien tai cua tai xe: {}", userId);
        try {
            List<com.google.cloud.firestore.QueryDocumentSnapshot> docs = statsRepository
                    .findByDriverIdAndStatus(userId, DeliveryOrderStatus.DELIVERING);
            if (docs.isEmpty()) {
                return null;
            }

            com.google.cloud.firestore.QueryDocumentSnapshot selectedDoc = docs.get(0);
            Instant selectedUpdatedAt = toInstant(selectedDoc.get("updatedAt"));
            Instant selectedCreatedAt = toInstant(selectedDoc.get("createdAt"));

            for (com.google.cloud.firestore.QueryDocumentSnapshot doc : docs) {
                Instant candidateUpdatedAt = toInstant(doc.get("updatedAt"));
                Instant candidateCreatedAt = toInstant(doc.get("createdAt"));
                if (isMoreRecent(candidateUpdatedAt, selectedUpdatedAt, candidateCreatedAt, selectedCreatedAt)) {
                    selectedDoc = doc;
                    selectedUpdatedAt = candidateUpdatedAt;
                    selectedCreatedAt = candidateCreatedAt;
                }
            }

            if (docs.size() > 1) {
                log.warn("Tai xe {} dang co {} don o trang thai DELIVERING, se tra ve don moi nhat {}",
                        userId, docs.size(), selectedDoc.getId());
            }

            return buildDeliveryOrderDTO(selectedDoc.getId(), selectedDoc.getData());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi lay don hien tai: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        } catch (java.util.concurrent.ExecutionException e) {
            log.error("Loi khi lay don hien tai: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        }
    }

    public List<DeliveryOrderDTO> getActiveOrders(String userId) {
        log.info("Bat dau lay don hang active cua tai xe: {}", userId);
        try {
            List<com.google.cloud.firestore.QueryDocumentSnapshot> docs = statsRepository
                    .findByDriverIdAndStatus(userId, DeliveryOrderStatus.DELIVERING);
            List<DeliveryOrderDTO> orders = new ArrayList<>();

            for (com.google.cloud.firestore.QueryDocumentSnapshot doc : docs) {
                Map<String, Object> data = doc.getData();
                DeliveryOrderDTO dto = buildDeliveryOrderDTO(doc.getId(), data);
                orders.add(dto);
            }

            log.info("Tim thay {} don hang active", orders.size());
            return orders;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi lay don hang active: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        } catch (java.util.concurrent.ExecutionException e) {
            log.error("Loi khi lay don hang active: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        }
    }

    public List<DeliveryOrderDTO> getOrderHistory(String userId) {
        log.info("Bat dau lay lich su don hang cua tai xe: {}", userId);
        try {
            List<com.google.cloud.firestore.QueryDocumentSnapshot> docs = statsRepository
                    .findByDriverIdAndStatusOrderByCreatedAt(userId, DeliveryOrderStatus.COMPLETED,
                            com.google.cloud.firestore.Query.Direction.DESCENDING);
            List<DeliveryOrderDTO> orders = new ArrayList<>();

            for (com.google.cloud.firestore.QueryDocumentSnapshot doc : docs) {
                Map<String, Object> data = doc.getData();
                DeliveryOrderDTO dto = buildDeliveryOrderDTO(doc.getId(), data);
                orders.add(dto);
            }

            log.info("Tim thay {} don lich su", orders.size());
            return orders;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi lay lich su don hang: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        } catch (java.util.concurrent.ExecutionException e) {
            log.error("Loi khi lay lich su don hang: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        }
    }

    public DriverOrderActionResultDTO respondDeclineOrder(String orderId, String userId, String requestId) {
        log.info("Tai xe tu choi don tu he thong push: orderId={}, userId={}, requestId={}", orderId, userId, requestId);
        try {
            Map<String, Object> orderRequest = orderRequestRepository.findByOrderId(orderId);
            Map<String, Object> driverRequest = xacThucDriverRequestTam(orderId, userId, requestId);

            String requestStatus = (String) orderRequest.get("status");
            if (!"pending".equals(requestStatus)) {
                throw BusinessException.trangThaiDonHangKhongHopLe(orderId, resolveOrderStatus(orderId), "tu choi");
            }

            List<String> targetDrivers = toStringList(orderRequest.get("targetDriverIds"));
            List<String> attemptedDrivers = toStringList(orderRequest.get("attemptedDriverIds"));

            if (targetDrivers == null || !targetDrivers.contains(userId)) {
                throw BusinessException.donHangDaCoTaiXe(orderId);
            }

            targetDrivers = new ArrayList<>(targetDrivers);
            targetDrivers.remove(userId);
            attemptedDrivers = attemptedDrivers == null ? new ArrayList<>() : new ArrayList<>(attemptedDrivers);
            attemptedDrivers.add(userId);

            Map<String, Object> updates = new HashMap<>();
            updates.put("targetDriverIds", targetDrivers);
            updates.put("attemptedDriverIds", attemptedDrivers);
            orderRequestRepository.updateFieldsByDocId((String) orderRequest.get("id"), updates);

            Map<String, Object> notifData = new HashMap<>();
            notifData.put("type", 13);
            notifData.put("title", "Don hang da duoc giao cho tai xe khac");
            notifData.put("body", "Don hang [" + orderId + "] da duoc tai xe khac nhan. Vui long cho don hang tiep theo.");
            notifData.put("orderId", orderId);
            notifData.put("referenceId", orderId);
            notifData.put("isRead", false);
            notifData.put("imageUrl", null);
            notifData.put("createdAt", new Date());

            walletRepository.getFirestore()
                    .collection("driver_profiles")
                    .document(userId)
                    .collection("notifications")
                    .add(notifData);

            // Backend cleanup: xoa request tam trong order_requests/{driverId}/requests/{requestId}
            xoaDriverRequestTam(userId, requestId, driverRequest);

            log.info("Tai xe [{}] da tu choi don [{}], {} tai xe con lai", userId, orderId, targetDrivers.size());
            return DriverOrderActionResultDTO.builder()
                    .orderId(orderId)
                    .requestId(requestId)
                    .status("DECLINED")
                    .build();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Loi khi xu ly tu choi don [{}]: {}", orderId, e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        }
    }

    public DeliveryOrderDTO respondAcceptOrder(String orderId, String userId, String requestId) {
        log.info("Tai xe chap nhan don tu he thong push: orderId={}, userId={}, requestId={}", orderId, userId, requestId);
        try {
            Map<String, Object> orderRequest = orderRequestRepository.findByOrderId(orderId);
            Map<String, Object> driverRequest = xacThucDriverRequestTam(orderId, userId, requestId);

            List<String> targetDrivers = toStringList(orderRequest.get("targetDriverIds"));
            if (targetDrivers == null || !targetDrivers.contains(userId)) {
                throw BusinessException.donHangDaCoTaiXe(orderId);
            }

            String requestStatus = (String) orderRequest.get("status");
            if (!"pending".equals(requestStatus)) {
                throw BusinessException.trangThaiDonHangKhongHopLe(orderId, resolveOrderStatus(orderId), "nhan");
            }

            Map<String, Object> driverProfileData = walletRepository.findDriverProfileById(userId);
            if (driverProfileData == null) {
                throw BusinessException.hoSoTaiXeChuaTonTai(userId);
            }

            Map<String, Object> userData = walletRepository.findUserById(userId);
            String driverName = userData != null ? (String) userData.get("fullName") : "Tai xe";
            String driverPhone = userData != null ? (String) userData.get("phoneNumber") : "";
            String vehiclePlate = (String) driverProfileData.get("vehiclePlate");

            final String finalDriverName = driverName != null ? driverName : "Tai xe";
            final String finalDriverPhone = driverPhone != null ? driverPhone : "";
            final String finalVehiclePlate = vehiclePlate != null ? vehiclePlate : "";

            try {
                statsRepository.acceptOrderInTransaction(
                        orderId, userId, finalDriverName, finalDriverPhone, finalVehiclePlate);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw BusinessException.loiHeThong(e.getMessage());
            } catch (java.util.concurrent.ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof IllegalStateException) {
                    String msg = cause.getMessage();
                    if (msg != null && msg.startsWith("ORDER_NOT_FOUND:")) {
                        throw BusinessException.donHangKhongTimThay(orderId);
                    }
                    if ("ORDER_STATUS_INVALID".equals(msg)) {
                        throw BusinessException.trangThaiDonHangKhongHopLe(orderId, resolveOrderStatus(orderId), "nhan");
                    }
                    if ("ORDER_ALREADY_ASSIGNED".equals(msg)) {
                        throw BusinessException.donHangDaCoTaiXe(orderId);
                    }
                }
                throw BusinessException.loiHeThong(e.getMessage());
            }

            Map<String, Object> reqUpdates = new HashMap<>();
            reqUpdates.put("status", "accepted");
            reqUpdates.put("acceptedDriverId", userId);
            reqUpdates.put("targetDriverIds", List.of());
            orderRequestRepository.updateFieldsByDocId((String) orderRequest.get("id"), reqUpdates);

            // Backend cleanup: xoa request tam trong order_requests/{driverId}/requests/{requestId}
            xoaDriverRequestTam(userId, requestId, driverRequest);

            Map<String, Object> orderData = statsRepository.findOrderRawById(orderId);
            DeliveryOrderDTO dto = buildDeliveryOrderDTO(orderId, orderData);

            log.info("Tai xe [{}] da nhan don [{}] thanh cong tu he thong push", userId, orderId);
            return dto;
        } catch (BusinessException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi nhan don: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        } catch (java.util.concurrent.ExecutionException e) {
            log.error("Loi khi nhan don: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        }
    }

    /**
     * Xac thuc request tam co ton tai duoi dung driver, thuoc dung order dang xu ly.
     */
    private Map<String, Object> xacThucDriverRequestTam(String orderId, String driverId, String requestId)
            throws ExecutionException, InterruptedException {
        if (requestId == null || requestId.isBlank()) {
            throw BusinessException.loiDinhVi("requestId khong duoc de trong");
        }

        Map<String, Object> driverRequest = orderRequestRepository.findDriverRequestById(driverId, requestId);
        if (driverRequest == null) {
            throw BusinessException.loiDinhVi("Khong tim thay request tam cua tai xe");
        }

        String driverRequestOrderId = String.valueOf(driverRequest.get("orderId"));
        if (!orderId.equals(driverRequestOrderId)) {
            throw BusinessException.loiDinhVi("requestId khong thuoc don hang dang xu ly");
        }

        return driverRequest;
    }

    /**
     * Xoa document request tam cua driver tai order_requests/{driverId}/requests/{requestId}.
     * Neu xoa that bai thi chi log warning, khong lam rollback business logic vi document
     * nay la du lieu tam phuc vu realtime/UI, khong phai source of truth.
     */
    private void xoaDriverRequestTam(String driverId, String requestId, Map<String, Object> driverRequest) {
        if (driverRequest == null) {
            log.debug("Khong co driverRequest da xac thuc, bo qua xoa request tam cho driver [{}]", driverId);
            return;
        }
        try {
            orderRequestRepository.deleteDriverRequest(driverId, requestId);
            log.info("Da xoa request tam [{}] cho driver [{}]", requestId, driverId);
        } catch (Exception e) {
            log.warn("Xoa request tam that bai nhung van tien hanh: driverId={}, requestId={}, loi={}",
                    driverId, requestId, e.getMessage());
        }
    }

    private void taoThongBaoKhachHang(String userId, String orderId) {
        try {
            Map<String, Object> notifData = new HashMap<>();
            notifData.put("type", 2);
            notifData.put("title", "Đơn hàng đã được giao thành công");
            notifData.put("body", "Đơn hàng [" + orderId + "] đã được giao thành công. Cảm ơn bạn đã sử dụng FoodGo!");
            notifData.put("referenceId", orderId);
            notifData.put("isRead", false);
            notifData.put("createdAt", new Date());

            statsRepository.saveCustomerNotification(userId, notifData);
            log.info("Da tao thong bao cho khach hang: userId={}, orderId={}", userId, orderId);
        } catch (Exception e) {
            log.warn("Loi khi tao thong bao cho khach hang: {}", e.getMessage());
        }
    }

    private void congDiemLoyaltyKhachHang(String userId, Double finalAmount) {
        if (userId == null || userId.isBlank()) {
            log.warn("Khong the cong diem loyalty: userId null hoac rong");
            return;
        }
        if (finalAmount == null || finalAmount <= 0) {
            log.warn("Khong the cong diem loyalty: finalAmount={}", finalAmount);
            return;
        }
        int diemCong = (int) Math.floor(finalAmount / 10000.0);
        if (diemCong <= 0) {
            log.info("Don hang co gia tri nho, khong cong diem loyalty (finalAmount={})", finalAmount);
            return;
        }
        try {
            voucherRepository.congLoyaltyPoints(userId, diemCong);
            log.info("Da cong {} diem loyalty cho khach hang [{}] - don hang finalAmount={}",
                    diemCong, userId, finalAmount);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Loi khi cong diem loyalty cho khach hang [{}]: {}", userId, e.getMessage());
        } catch (java.util.concurrent.ExecutionException e) {
            log.warn("Loi khi cong diem loyalty cho khach hang [{}]: {}", userId, e.getMessage());
        }
    }

    private void enrichStoreLocation(DeliveryOrderDTO dto) throws ExecutionException, InterruptedException {
        if (dto == null || dto.getStoreId() == null) {
            return;
        }

        Map<String, Object> storeData = statsRepository.findStoreById(dto.getStoreId());
        if (storeData == null) {
            return;
        }

        dto.setStoreAddress((String) storeData.get("address"));
        dto.setStoreLat(toDouble(storeData.get("lat")));
        dto.setStoreLng(toDouble(storeData.get("lng")));
        dto.setStorePhone((String) storeData.get("phone"));
    }

    @SuppressWarnings("unchecked")
    private DeliveryOrderDTO buildDeliveryOrderDTO(String orderId, Map<String, Object> data)
            throws ExecutionException, InterruptedException {
        DeliveryOrderDTO dto = mapToDeliveryOrderDTO(orderId, data);
        enrichStoreLocation(dto);
        enrichCustomerInfo(dto, data);
        enrichDerivedFields(dto, data);
        return dto;
    }

    @SuppressWarnings("unchecked")
    private DeliveryOrderDTO mapToDeliveryOrderDTO(String orderId, Map<String, Object> data) {
        if (data == null) {
            return DeliveryOrderDTO.builder().id(orderId).build();
        }

        List<Map<String, Object>> rawItems = (List<Map<String, Object>>) data.get("items");
        List<DeliveryOrderDTO.OrderItemData> orderItems = new ArrayList<>();

        if (rawItems != null) {
            for (Map<String, Object> rawItem : rawItems) {
                List<Map<String, Object>> rawOptions = (List<Map<String, Object>>) rawItem.get("options");
                List<DeliveryOrderDTO.OptionData> optionDataList = new ArrayList<>();

                if (rawOptions != null) {
                    for (Map<String, Object> rawOption : rawOptions) {
                        optionDataList.add(DeliveryOrderDTO.OptionData.builder()
                                .name((String) rawOption.get("name"))
                                .price(toDouble(rawOption.get("price")))
                                .build());
                    }
                }

                orderItems.add(DeliveryOrderDTO.OrderItemData.builder()
                        .foodId((String) rawItem.get("foodId"))
                        .name((String) rawItem.get("name"))
                        .price(toDouble(rawItem.get("price")))
                        .quantity(toInt(rawItem.get("quantity")))
                        .imageUrl((String) rawItem.get("imageUrl"))
                        .options(optionDataList)
                        .build());
            }
        }

        return DeliveryOrderDTO.builder()
                .id(orderId)
                .orderCode(resolveOrderCode(orderId, data))
                .userId((String) data.get("userId"))
                .recipientName((String) data.get("receiverName"))
                .recipientPhone((String) data.get("receiverPhone"))
                .storeId((String) data.get("storeId"))
                .storeName((String) data.get("storeName"))
                .items(orderItems)
                .totalAmount(toDouble(data.get("totalAmount")))
                .discountAmount(toDouble(data.get("discountAmount")))
                .deliveryFee(toDouble(data.get("deliveryFee") != null ? data.get("deliveryFee") : data.get("shippingFee")))
                .finalAmount(toDouble(data.get("finalAmount")))
                .status(getOrderStatusValueFromMap(data))
                .paymentStatus(toIntPrimitive(data.get("paymentStatus")))
                .deliveryAddress((String) data.get("deliveryAddress"))
                .deliveryLat(toDouble(data.get("deliveryLat")))
                .deliveryLng(toDouble(data.get("deliveryLng")))
                .distance(resolveDistanceKm(data))
                .paymentMethod(toIntPrimitive(data.get("paymentMethod")))
                .driverId((String) data.get("driverId"))
                .driverName((String) data.get("driverName"))
                .driverPhone((String) data.get("driverPhone"))
                .vehiclePlate((String) data.get("vehiclePlate"))
                .arrivedAtStoreAt(toInstant(data.get("arrivedAtStoreAt")))
                .pickedUpAt(toInstant(data.get("pickedUpAt")))
                .deliveredAt(toInstant(data.get("deliveredAt")))
                .createdAt(toInstant(data.get("createdAt")))
                .updatedAt(toInstant(data.get("updatedAt")))
                .note((String) data.get("note"))
                .deliveryStep((String) data.get("deliveryStep"))
                .build();
    }

    public DeliveryOrderDTO mapToDriverOrderRealtimeDTO(String orderId, String requestId, Instant expiresAt,
                                                        Double estimatedEarning, Double deliveryHeading) {
        DeliveryOrderDTO order = getOrderDetail(orderId);
        order.setRequestId(requestId);
        order.setEstimatedEarning(estimatedEarning);
        order.setExpiresAt(expiresAt);
        order.setExpiresInSeconds(calculateExpiresInSeconds(expiresAt));
        order.setDeliveryHeading(deliveryHeading);
        return order;
    }

    private void enrichCustomerInfo(DeliveryOrderDTO dto, Map<String, Object> data)
            throws ExecutionException, InterruptedException {
        if (dto == null || data == null) {
            return;
        }
        String userId = dto.getUserId();
        if (userId == null || userId.isBlank()) {
            return;
        }
        Map<String, Object> customer = walletRepository.findUserById(userId);
        if (customer == null) {
            customer = statsRepository.findUserById(userId);
        }
        if (customer == null) {
            return;
        }
        dto.setCustomerName((String) customer.get("fullName"));
        dto.setCustomerPhone((String) customer.get("phoneNumber"));
        dto.setCustomerAvatarUrl((String) customer.get("photoUrl"));
    }

    private void enrichDerivedFields(DeliveryOrderDTO dto, Map<String, Object> data) {
        if (dto == null || data == null) {
            return;
        }

        double itemsSubtotal = 0.0;
        double optionsSubtotal = 0.0;
        if (dto.getItems() != null) {
            for (DeliveryOrderDTO.OrderItemData item : dto.getItems()) {
                int quantity = item.getQuantity() != null ? item.getQuantity() : 0;
                double price = item.getPrice() != null ? item.getPrice() : 0.0;
                itemsSubtotal += price * quantity;
                if (item.getOptions() != null) {
                    for (DeliveryOrderDTO.OptionData option : item.getOptions()) {
                        double optionPrice = option.getPrice() != null ? option.getPrice() : 0.0;
                        optionsSubtotal += optionPrice * quantity;
                    }
                }
            }
        }

        dto.setItemsSubtotal(roundCurrency(itemsSubtotal));
        dto.setOptionsSubtotal(roundCurrency(optionsSubtotal));
        dto.setDeliveryDistanceKm(dto.getDistance());
        dto.setEstimatedDurationMinutes(estimateDurationMinutes(dto.getDeliveryDistanceKm()));
        dto.setDriverCollectAmount(resolveDriverCollectAmount(dto));
        dto.setStatusCode(DeliveryOrderStatus.getCode(dto.getStatus() != null ? dto.getStatus() : 0));
        dto.setStatusDescription(DeliveryOrderStatus.getDescription(dto.getStatus() != null ? dto.getStatus() : 0));
        dto.setDeliveryStep(resolveDeliveryStep(dto));
    }

    private String resolveOrderCode(String orderId, Map<String, Object> data) {
        String code = data != null ? (String) data.get("code") : null;
        if (code != null && !code.isBlank()) {
            return code;
        }
        if (orderId == null || orderId.isBlank()) {
            return null;
        }
        String suffix = orderId.length() > 6 ? orderId.substring(orderId.length() - 6) : orderId;
        return "FG" + suffix.toUpperCase();
    }

    private Integer calculateExpiresInSeconds(Instant expiresAt) {
        if (expiresAt == null) {
            return null;
        }
        long seconds = java.time.Duration.between(Instant.now(), expiresAt).getSeconds();
        return (int) Math.max(0, seconds);
    }

    private boolean isMoreRecent(Instant candidateUpdatedAt, Instant selectedUpdatedAt,
                                 Instant candidateCreatedAt, Instant selectedCreatedAt) {
        if (candidateUpdatedAt != null && selectedUpdatedAt != null) {
            if (!candidateUpdatedAt.equals(selectedUpdatedAt)) {
                return candidateUpdatedAt.isAfter(selectedUpdatedAt);
            }
        } else if (candidateUpdatedAt != null) {
            return true;
        } else if (selectedUpdatedAt != null) {
            return false;
        }

        if (candidateCreatedAt != null && selectedCreatedAt != null) {
            return candidateCreatedAt.isAfter(selectedCreatedAt);
        }
        return candidateCreatedAt != null && selectedCreatedAt == null;
    }

    private Double roundCurrency(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private Double resolveDriverCollectAmount(DeliveryOrderDTO dto) {
        if (dto == null) {
            return null;
        }
        if (isCashPayment(dto.getPaymentMethod())) {
            if (dto.getFinalAmount() != null) {
                return dto.getFinalAmount();
            }
            if (dto.getTotalAmount() != null && dto.getDeliveryFee() != null) {
                return dto.getTotalAmount() + dto.getDeliveryFee();
            }
            return dto.getTotalAmount();
        }
        return 0.0;
    }

    private Integer estimateDurationMinutes(Double deliveryDistanceKm) {
        if (deliveryDistanceKm == null) {
            return null;
        }
        double minutes = Math.max(5.0, deliveryDistanceKm * 4.0);
        return (int) Math.round(minutes);
    }

    private String resolveDeliveryStep(DeliveryOrderDTO dto) {
        if (dto == null || dto.getStatus() == null) {
            return "UNKNOWN";
        }
        int status = dto.getStatus();
        if (status == DeliveryOrderStatus.CANCELLED) {
            return "CANCELLED";
        }
        if (status == DeliveryOrderStatus.COMPLETED) {
            return "DELIVERED";
        }
        if (status == DeliveryOrderStatus.DELIVERING) {
            if (dto.getDeliveredAt() != null) {
                return "DELIVERED";
            }
            if (dto.getPickedUpAt() != null) {
                return "ON_THE_WAY";
            }
            if (dto.getArrivedAtStoreAt() != null) {
                return "ARRIVED_STORE";
            }
            return "WAITING_PICKUP";
        }
        if (status == DeliveryOrderStatus.WAITING_DRIVER) {
            return "WAITING_DRIVER";
        }
        if (status == DeliveryOrderStatus.PENDING_STORE_CONFIRMATION) {
            return "PENDING_STORE_CONFIRMATION";
        }
        return "UNKNOWN";
    }

    private int getOrderStatusValueFromMap(Map<String, Object> data) {
        Object statusObj = data.get("status");
        if (statusObj == null) return 0;
        if (statusObj instanceof Number) return ((Number) statusObj).intValue();
        if (statusObj instanceof String) {
            try { return Integer.parseInt((String) statusObj); } catch (NumberFormatException e) { return 0; }
        }
        return 0;
    }

    private Double toDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).doubleValue();
        if (value instanceof String str) {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private Double resolveDistanceKm(Map<String, Object> data) {
        Double distance = toDouble(data.get("distance"));
        if (distance != null) {
            return distance;
        }

        Double storeLat = toDouble(data.get("storeLat"));
        Double storeLng = toDouble(data.get("storeLng"));
        Double deliveryLat = toDouble(data.get("deliveryLat"));
        Double deliveryLng = toDouble(data.get("deliveryLng"));

        if (storeLat == null || storeLng == null || deliveryLat == null || deliveryLng == null) {
            return null;
        }

        return calculateDistanceKm(storeLat, storeLng, deliveryLat, deliveryLng);
    }

    private double calculateDistanceKm(double lat1, double lng1, double lat2, double lng2) {
        final double earthRadiusKm = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round(earthRadiusKm * c * 10.0) / 10.0;
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        return null;
    }

    private Integer toInt(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).intValue();
        if (value instanceof String str) {
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private List<String> toStringList(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof List<?> list) {
            List<String> result = new ArrayList<>();
            for (Object item : list) {
                if (item != null) {
                    result.add(String.valueOf(item));
                }
            }
            return result;
        }
        return null;
    }

    private Integer resolveOrderStatus(String orderId) throws ExecutionException, InterruptedException {
        Map<String, Object> orderData = statsRepository.findOrderRawById(orderId);
        if (orderData == null) {
            return null;
        }
        return getOrderStatusValueFromMap(orderData);
    }

    private int toIntPrimitive(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).intValue();
        return 0;
    }

    private Instant toInstant(Object value) {
        if (value == null) return null;
        if (value instanceof Timestamp) return ((Timestamp) value).toDate().toInstant();
        if (value instanceof java.util.Date) return ((java.util.Date) value).toInstant();
        if (value instanceof Long) return Instant.ofEpochMilli((Long) value);
        if (value instanceof Number) return Instant.ofEpochMilli(((Number) value).longValue());
        if (value instanceof Map<?, ?> map) {
            Object seconds = map.get("_seconds");
            Object nanoseconds = map.get("_nanoseconds");
            if (seconds instanceof Number sec) {
                long nanos = nanoseconds instanceof Number nano ? nano.longValue() : 0L;
                return Instant.ofEpochSecond(sec.longValue(), nanos);
            }
            Object timestamp = map.get("timestamp");
            if (timestamp != null) {
                return toInstant(timestamp);
            }
        }
        if (value instanceof String str) {
            try {
                return Instant.parse(str);
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private boolean isCashPayment(Object paymentMethodObj) {
        if (paymentMethodObj == null) {
            return false;
        }
        if (paymentMethodObj instanceof Number) {
            int val = ((Number) paymentMethodObj).intValue();
            return val == 2;
        }
        String pmStr = paymentMethodObj.toString().toLowerCase().trim();
        return pmStr.equals("2") || pmStr.equals("cash") || pmStr.equals("tiền mặt") || pmStr.equals("tien mat");
    }
}
