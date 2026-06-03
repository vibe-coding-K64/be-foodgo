package com.example.be_foodgo.service;

import com.example.be_foodgo.dto.DeliveryOrderDTO;
import com.example.be_foodgo.exception.BusinessException;
import com.example.be_foodgo.repository.OrderRequestRepository;
import com.example.be_foodgo.repository.StatsRepository;
import com.example.be_foodgo.repository.WalletRepository;
import com.google.cloud.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DeliveryOrderService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryOrderService.class);

    private final StatsRepository statsRepository;
    private final WalletRepository walletRepository;
    private final WalletService walletService;
    private final OrderRequestRepository orderRequestRepository;

    public DeliveryOrderService(StatsRepository statsRepository,
                                WalletRepository walletRepository,
                                WalletService walletService,
                                OrderRequestRepository orderRequestRepository) {
        this.statsRepository = statsRepository;
        this.walletRepository = walletRepository;
        this.walletService = walletService;
        this.orderRequestRepository = orderRequestRepository;
    }

    public List<DeliveryOrderDTO> getAvailableOrders() {
        log.info("Bat dau lay danh sach don hang kha dung");
        try {
            List<com.google.cloud.firestore.QueryDocumentSnapshot> docs = statsRepository.findAvailableOrders();
            List<DeliveryOrderDTO> orders = new ArrayList<>();

            for (com.google.cloud.firestore.QueryDocumentSnapshot doc : docs) {
                Map<String, Object> data = doc.getData();
                DeliveryOrderDTO dto = mapToDeliveryOrderDTO(doc.getId(), data);

                Object storeIdObj = data.get("storeId");
                if (storeIdObj != null) {
                    Map<String, Object> storeData = statsRepository.findStoreById(storeIdObj.toString());
                    if (storeData != null) {
                        dto.setStoreAddress((String) storeData.get("address"));
                        dto.setStoreLat(toDouble(storeData.get("lat")));
                        dto.setStoreLng(toDouble(storeData.get("lng")));
                    }
                }

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
            DeliveryOrderDTO dto = mapToDeliveryOrderDTO(orderId, orderData);

            if (dto.getStoreId() != null) {
                Map<String, Object> storeData = statsRepository.findStoreById(dto.getStoreId());
                if (storeData != null) {
                    dto.setStoreAddress((String) storeData.get("address"));
                    dto.setStoreLat(toDouble(storeData.get("lat")));
                    dto.setStoreLng(toDouble(storeData.get("lng")));
                }
            }

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

    public void declineOrder(String orderId, String userId) {
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
            notifData.put("createdAt", Instant.now());

            statsRepository.saveDriverNotification(userId, notifData);

            List<com.google.cloud.firestore.QueryDocumentSnapshot> oldNotifs = statsRepository
                    .findDriverNotificationsByTypeAndOrderId(userId, 11, orderId);
            for (com.google.cloud.firestore.QueryDocumentSnapshot doc : oldNotifs) {
                statsRepository.deleteNotification(doc.getReference().getPath());
            }

            log.info("Tu choi don hang thanh cong: orderId={}, userId={}", orderId, userId);
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

            if (currentStatus != 2) {
                throw BusinessException.trangThaiDonHangKhongHopLe(orderId, currentStatus, "cập nhật trạng thái");
            }

            if (newStatus != 3 && newStatus != 4) {
                throw BusinessException.trangThaiDonHangKhongHopLe(orderId, currentStatus, "cập nhật trạng thái");
            }

            Map<String, Object> driverUpdates = new HashMap<>();
            driverUpdates.put("currentOrderId", null);
            driverUpdates.put("isAvailable", true);
            driverUpdates.put("updatedAt", Instant.now());

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
                orderUpdates.put("updatedAt", Instant.now());
                statsRepository.updateOrderFields(orderId, orderUpdates);

                driverUpdates.put("totalTrips",
                        com.google.cloud.firestore.FieldValue.increment(1));
                walletRepository.updateDriverProfileFields(userId, driverUpdates);

                if (customerId != null) {
                    taoThongBaoKhachHang(customerId, orderId);
                }

                if (deliveryFee != null && deliveryFee > 0) {
                    walletService.taoGiaoDichThuNhap(userId, orderId, deliveryFee);
                }

                String orderCode = (String) orderData.get("code");
                if (orderCode == null || orderCode.trim().isEmpty()) {
                    orderCode = orderId.length() >= 6 ? orderId.substring(orderId.length() - 6).toUpperCase() : "ORDER";
                }

                // Nếu là đơn COD (tiền mặt), tài xế giữ tiền mặt nên ta trừ số dư ví điện tử của tài xế
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
                // Sửa logic tài xế hủy đơn giao: Reset trạng thái đơn về 1 (Đang chờ tài xế nhận) thay vì 4 (Đã hủy)
                Map<String, Object> orderUpdates = new HashMap<>();
                orderUpdates.put("status", 1);
                orderUpdates.put("driverId", null);
                orderUpdates.put("driverName", null);
                orderUpdates.put("driverPhone", null);
                orderUpdates.put("vehiclePlate", null);
                orderUpdates.put("updatedAt", Instant.now());
                statsRepository.updateOrderFields(orderId, orderUpdates);

                walletRepository.updateDriverProfileFields(userId, driverUpdates);
                log.info("Don hang da bi tai xe tu choi giao hang. Reset ve cho tai xe (status 1) - orderId={}", orderId);
            }

            Map<String, Object> updatedOrderData = statsRepository.findOrderRawById(orderId);
            DeliveryOrderDTO dto = mapToDeliveryOrderDTO(orderId, updatedOrderData);

            if (dto.getStoreId() != null) {
                Map<String, Object> storeData = statsRepository.findStoreById(dto.getStoreId());
                if (storeData != null) {
                    dto.setStoreAddress((String) storeData.get("address"));
                    dto.setStoreLat(toDouble(storeData.get("lat")));
                    dto.setStoreLng(toDouble(storeData.get("lng")));
                }
            }

            log.info("Cap nhat trang thai don hang thanh cong: orderId={}, newStatus={}", orderId, newStatus);
            return dto;
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

    public List<DeliveryOrderDTO> getCurrentOrder(String userId) {
        log.info("Bat dau lay don hien tai cua tai xe: {}", userId);
        try {
            List<com.google.cloud.firestore.QueryDocumentSnapshot> docs = statsRepository
                    .findByDriverIdAndStatus(userId, 2);
            List<DeliveryOrderDTO> orders = new ArrayList<>();

            for (com.google.cloud.firestore.QueryDocumentSnapshot doc : docs) {
                Map<String, Object> data = doc.getData();
                DeliveryOrderDTO dto = mapToDeliveryOrderDTO(doc.getId(), data);

                if (data.get("storeId") != null) {
                    Map<String, Object> storeData = statsRepository
                            .findStoreById(data.get("storeId").toString());
                    if (storeData != null) {
                        dto.setStoreAddress((String) storeData.get("address"));
                        dto.setStoreLat(toDouble(storeData.get("lat")));
                        dto.setStoreLng(toDouble(storeData.get("lng")));
                    }
                }

                orders.add(dto);
            }

            log.info("Tim thay {} don hien tai", orders.size());
            return orders;
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
                    .findByDriverIdAndStatus(userId, 2);
            List<DeliveryOrderDTO> orders = new ArrayList<>();

            for (com.google.cloud.firestore.QueryDocumentSnapshot doc : docs) {
                Map<String, Object> data = doc.getData();
                DeliveryOrderDTO dto = mapToDeliveryOrderDTO(doc.getId(), data);

                if (data.get("storeId") != null) {
                    Map<String, Object> storeData = statsRepository
                            .findStoreById(data.get("storeId").toString());
                    if (storeData != null) {
                        dto.setStoreAddress((String) storeData.get("address"));
                        dto.setStoreLat(toDouble(storeData.get("lat")));
                        dto.setStoreLng(toDouble(storeData.get("lng")));
                    }
                }

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
                    .findByDriverIdAndStatusOrderByCreatedAt(userId, 3,
                            com.google.cloud.firestore.Query.Direction.DESCENDING);
            List<DeliveryOrderDTO> orders = new ArrayList<>();

            for (com.google.cloud.firestore.QueryDocumentSnapshot doc : docs) {
                Map<String, Object> data = doc.getData();
                DeliveryOrderDTO dto = mapToDeliveryOrderDTO(doc.getId(), data);

                if (data.get("storeId") != null) {
                    Map<String, Object> storeData = statsRepository
                            .findStoreById(data.get("storeId").toString());
                    if (storeData != null) {
                        dto.setStoreAddress((String) storeData.get("address"));
                        dto.setStoreLat(toDouble(storeData.get("lat")));
                        dto.setStoreLng(toDouble(storeData.get("lng")));
                    }
                }

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

    public void respondDeclineOrder(String orderId, String userId) {
        log.info("Tai xe tu choi don tu he thong push: orderId={}, userId={}", orderId, userId);
        try {
            Map<String, Object> orderRequest = orderRequestRepository.findByOrderId(orderId);
            if (orderRequest == null) {
                log.warn("Khong tim thay order_request cho don [{}]", orderId);
                return;
            }

            @SuppressWarnings("unchecked")
            List<String> targetDrivers = (List<String>) orderRequest.get("targetDriverIds");
            @SuppressWarnings("unchecked")
            List<String> attemptedDrivers = (List<String>) orderRequest.get("attemptedDriverIds");
            if (attemptedDrivers == null) attemptedDrivers = new ArrayList<>();

            if (targetDrivers == null || !targetDrivers.contains(userId)) {
                log.warn("Tai xe [{}] khong nam trong danh sach yeu cau nhan don [{}]", userId, orderId);
                return;
            }

            targetDrivers = new ArrayList<>(targetDrivers);
            targetDrivers.remove(userId);
            attemptedDrivers = new ArrayList<>(attemptedDrivers);
            attemptedDrivers.add(userId);

            Map<String, Object> updates = new HashMap<>();
            updates.put("targetDriverIds", targetDrivers);
            updates.put("attemptedDriverIds", attemptedDrivers);
            orderRequestRepository.updateFields(orderId, updates);

            Map<String, Object> notifData = new HashMap<>();
            notifData.put("type", 13);
            notifData.put("title", "Don hang da duoc giao cho tai xe khac");
            notifData.put("body", "Don hang [" + orderId + "] da duoc tai xe khac nhan. Vui long cho don hang tiep theo.");
            notifData.put("orderId", orderId);
            notifData.put("referenceId", orderId);
            notifData.put("isRead", false);
            notifData.put("imageUrl", null);
            notifData.put("createdAt", Instant.now());

            walletRepository.getFirestore()
                    .collection("driver_profiles")
                    .document(userId)
                    .collection("notifications")
                    .add(notifData);

            log.info("Tai xe [{}] da tu choi don [{}], {} tai xe con lai", userId, orderId, targetDrivers.size());
        } catch (Exception e) {
            log.error("Loi khi xu ly tu choi don [{}]: {}", orderId, e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        }
    }

    public DeliveryOrderDTO respondAcceptOrder(String orderId, String userId) {
        log.info("Tai xe chap nhan don tu he thong push: orderId={}, userId={}", orderId, userId);
        try {
            Map<String, Object> orderRequest = orderRequestRepository.findByOrderId(orderId);
            if (orderRequest == null) {
                throw BusinessException.donHangKhongTimThay(orderId);
            }

            @SuppressWarnings("unchecked")
            List<String> targetDrivers = (List<String>) orderRequest.get("targetDriverIds");
            if (targetDrivers == null || !targetDrivers.contains(userId)) {
                throw BusinessException.donHangDaCoTaiXe(orderId);
            }

            String requestStatus = (String) orderRequest.get("status");
            if (!"pending".equals(requestStatus)) {
                throw BusinessException.trangThaiDonHangKhongHopLe(orderId, 1, "nhan");
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

            statsRepository.acceptOrderInTransaction(
                    orderId, userId, finalDriverName, finalDriverPhone, finalVehiclePlate);

            Map<String, Object> reqUpdates = new HashMap<>();
            reqUpdates.put("status", "accepted");
            reqUpdates.put("acceptedDriverId", userId);
            reqUpdates.put("targetDriverIds", List.of());
            orderRequestRepository.updateFields(orderId, reqUpdates);

            Map<String, Object> orderData = statsRepository.findOrderRawById(orderId);
            DeliveryOrderDTO dto = mapToDeliveryOrderDTO(orderId, orderData);

            if (dto.getStoreId() != null) {
                Map<String, Object> storeData = statsRepository.findStoreById(dto.getStoreId());
                if (storeData != null) {
                    dto.setStoreAddress((String) storeData.get("address"));
                    dto.setStoreLat(toDouble(storeData.get("lat")));
                    dto.setStoreLng(toDouble(storeData.get("lng")));
                }
            }

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

    private void taoThongBaoKhachHang(String userId, String orderId) {
        try {
            Map<String, Object> notifData = new HashMap<>();
            notifData.put("type", 2);
            notifData.put("title", "Đơn hàng đã được giao thành công");
            notifData.put("body", "Đơn hàng [" + orderId + "] đã được giao thành công. Cảm ơn bạn đã sử dụng FoodGo!");
            notifData.put("referenceId", orderId);
            notifData.put("isRead", false);
            notifData.put("createdAt", Instant.now());

            statsRepository.saveCustomerNotification(userId, notifData);
            log.info("Da tao thong bao cho khach hang: userId={}, orderId={}", userId, orderId);
        } catch (Exception e) {
            log.warn("Loi khi tao thong bao cho khach hang: {}", e.getMessage());
        }
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
                .userId((String) data.get("userId"))
                .storeId((String) data.get("storeId"))
                .storeName((String) data.get("storeName"))
                .items(orderItems)
                .totalAmount(toDouble(data.get("totalAmount")))
                .deliveryFee(toDouble(data.get("deliveryFee") != null ? data.get("deliveryFee") : data.get("shippingFee")))
                .status(getOrderStatusValueFromMap(data))
                .deliveryAddress((String) data.get("deliveryAddress"))
                .paymentMethod(toIntPrimitive(data.get("paymentMethod")))
                .createdAt(toInstant(data.get("createdAt")))
                .updatedAt(toInstant(data.get("updatedAt")))
                .note((String) data.get("note"))
                .build();
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
        return null;
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        return null;
    }

    private Integer toInt(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).intValue();
        return null;
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
        return null;
    }

    private boolean isCashPayment(Object paymentMethodObj) {
        if (paymentMethodObj == null) {
            return true;
        }
        if (paymentMethodObj instanceof Number) {
            int val = ((Number) paymentMethodObj).intValue();
            return val == 1 || val == 0;
        }
        String pmStr = paymentMethodObj.toString().toLowerCase().trim();
        return pmStr.equals("cash") || pmStr.equals("tiền mặt") || pmStr.equals("tien mat") || pmStr.equals("1") || pmStr.equals("0");
    }
}
