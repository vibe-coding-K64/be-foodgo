package com.example.be_foodgo.service;

import com.example.be_foodgo.dto.CancelOrderResponse;
import com.example.be_foodgo.dto.NotificationDTO;
import com.example.be_foodgo.dto.OrderDTO;
import com.example.be_foodgo.dto.OrderItemDTO;
import com.example.be_foodgo.exception.BusinessException;
import com.example.be_foodgo.model.Order;
import com.example.be_foodgo.model.OrderItem;
import com.example.be_foodgo.model.Store;
import com.example.be_foodgo.repository.OrderRepository;
import com.example.be_foodgo.repository.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private WalletService walletService;

    @Autowired
    private NotificationService notificationService;

    public List<OrderDTO> getOrdersByStoreId(String storeId) throws ExecutionException, InterruptedException {
        List<Order> orders = orderRepository.findByStoreId(storeId);
        List<OrderDTO> dtos = new ArrayList<>();
        for (Order o : orders) {
            dtos.add(convertToDTO(o));
        }
        return dtos;
    }

    public OrderDTO getOrderById(String id) throws ExecutionException, InterruptedException {
        Order o = orderRepository.findById(id);
        if (o != null) return convertToDTO(o);
        return null;
    }

    public String createOrder(OrderDTO dto) throws ExecutionException, InterruptedException {
        Order order = convertToEntity(dto);

        if (order.getStoreName() == null || order.getStoreName().trim().isEmpty()) {
            Store store = storeRepository.getStoreById(dto.getStoreId());
            if (store != null) {
                order.setStoreName(store.getName());
            }
        }

        order.setCreatedAt(new java.util.Date());
        String orderId = orderRepository.save(order);
        
        String orderCode = getOrderCodeDisplay(order);
        String itemsSummary = getOrderItemsSummary(order);
        // Thông báo cho Quán ăn (khi nhận đơn mới)
        NotificationDTO merchantNotif = new NotificationDTO();
        merchantNotif.setTitle("Đơn hàng mới: " + orderCode);
        merchantNotif.setBody("Bạn vừa nhận được đơn hàng mới #" + orderCode + " gồm: " + itemsSummary + ". Vui lòng chuẩn bị món!");
        merchantNotif.setType(1); // 1 = order type
        merchantNotif.setOrderId(orderId);
        notificationService.notifyMerchantByStoreId(dto.getStoreId(), merchantNotif);

        return orderId;
    }

    public String updateOrderStatus(String id, int status) throws ExecutionException, InterruptedException {
        Order order = orderRepository.findById(id);
        if (order != null) {
            order.setStatus(status);
            String result = orderRepository.update(id, order);
            
            // Gửi thông báo theo từng trạng thái
            NotificationDTO userNotif = new NotificationDTO();
            userNotif.setOrderId(id);
            userNotif.setType(1); // 1 = order type

            NotificationDTO merchantNotif = new NotificationDTO();
            merchantNotif.setOrderId(id);
            merchantNotif.setType(1); // 1 = order type

            String orderCode = getOrderCodeDisplay(order);
            String itemsSummary = getOrderItemsSummary(order);
            if (status == 1) {
                // Đang chuẩn bị -> Thông báo cho khách hàng
                userNotif.setTitle("Đơn hàng " + orderCode + " đang chuẩn bị");
                userNotif.setBody("Quán đang chuẩn bị món ăn cho đơn hàng của bạn.");
                notificationService.createNotification("customer_profiles", order.getUserId(), userNotif);
            } else if (status == 2) {
                // Đang giao -> Shipper đã lấy hàng
                String driverName = (order.getDriverName() != null && !order.getDriverName().isEmpty()) ? order.getDriverName() : "Tài xế";
                userNotif.setTitle(driverName + " đã nhận đơn");
                userNotif.setBody(driverName + " đang giao đơn hàng đến bạn. Vui lòng chú ý điện thoại!");
                notificationService.createNotification("customer_profiles", order.getUserId(), userNotif);

                merchantNotif.setTitle("Tài xế đang giao đơn " + orderCode);
                merchantNotif.setBody(driverName + " đã lấy món (" + itemsSummary + ") và đang giao cho khách.");
                notificationService.notifyMerchantByStoreId(order.getStoreId(), merchantNotif);
            } else if (status == 3) {
                double merchantIncome = order.getTotalAmount() - order.getShopDiscountAmount();

                if (merchantIncome > 0) {
                    walletService.createMerchantIncomeTransaction(order.getStoreId(), id, orderCode, merchantIncome);
                }

                // Đơn hoàn thành
                userNotif.setTitle("Giao hàng thành công đơn " + orderCode);
                userNotif.setBody("Đơn hàng " + orderCode + " đã được giao thành công. Chúc bạn ngon miệng!");
                notificationService.createNotification("customer_profiles", order.getUserId(), userNotif);

                merchantNotif.setTitle("Đơn hàng " + orderCode + " hoàn thành");
                merchantNotif.setBody("Đơn hàng #" + orderCode + " (" + itemsSummary + ") đã giao thành công và tiền đã được cộng vào ví.");
                notificationService.notifyMerchantByStoreId(order.getStoreId(), merchantNotif);
            }

            return result;
        }
        return null;
    }

    public CancelOrderResponse cancelOrder(String orderId, String userId, String reason) throws ExecutionException, InterruptedException {
        Order order = orderRepository.findById(orderId);

        if (order == null) {
            throw BusinessException.donHangKhongTimThay(orderId);
        }

        String orderUserId = order.getUserId();
        if (orderUserId == null || !orderUserId.equals(userId)) {
            throw BusinessException.khongPhaiChuDonHang(orderId);
        }

        int statusValue = order.getStatusValue();
        if (statusValue != 0) {
            throw BusinessException.trangThaiKhongTheHuy(orderId, statusValue);
        }

        order.setStatus(4);
        order.setUpdatedAt(new java.util.Date());

        if (reason != null && !reason.trim().isEmpty()) {
            order.setNote(reason);
        }

        Map<String, Object> fields = new HashMap<>();
        fields.put("status", 4);
        fields.put("updatedAt", order.getUpdatedAt());
        if (reason != null && !reason.trim().isEmpty()) {
            fields.put("note", reason);
        }
        String updatedAtStr = orderRepository.updateFields(orderId, fields);

        String orderCode = getOrderCodeDisplay(order);
        // Thông báo hủy đơn cho Quán
        NotificationDTO merchantNotif = new NotificationDTO();
        merchantNotif.setTitle("Đơn hàng " + orderCode + " bị hủy");
        merchantNotif.setBody("Khách hàng đã hủy đơn hàng #" + orderCode + ". Lý do: " + (reason != null ? reason : "Không có"));
        merchantNotif.setType(1);
        merchantNotif.setOrderId(orderId);
        notificationService.notifyMerchantByStoreId(order.getStoreId(), merchantNotif);

        return new CancelOrderResponse(orderId, 4, updatedAtStr);
    }

    private OrderDTO convertToDTO(Order entity) throws ExecutionException, InterruptedException {
        OrderDTO dto = new OrderDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setStoreId(entity.getStoreId());

        String storeName = entity.getStoreName();
        if (storeName == null || storeName.trim().isEmpty()) {
            Store store = storeRepository.getStoreById(entity.getStoreId());
            if (store != null) {
                storeName = store.getName();
            } else {
                storeName = "";
            }
        }
        dto.setStoreName(storeName);

        String code = entity.getCode();
        if (code == null || code.trim().isEmpty()) {
            if (entity.getId() != null && entity.getId().length() >= 6) {
                code = entity.getId().substring(entity.getId().length() - 6).toUpperCase();
            } else {
                code = "ORDER";
            }
        }
        dto.setCode(code);

        dto.setDeliveryAddress(entity.getDeliveryAddress());
        dto.setReceiverName(entity.getReceiverName());
        dto.setReceiverPhone(entity.getReceiverPhone());
        dto.setDeliveryFee(entity.getDeliveryFee());
        dto.setDriverName(entity.getDriverName());
        dto.setDriverPhone(entity.getDriverPhone());
        dto.setTotalAmount(entity.getTotalAmount());
        dto.setDiscountAmount(entity.getDiscountAmount());
        dto.setShopDiscountAmount(entity.getShopDiscountAmount());
        dto.setFreeshipDiscountAmount(entity.getFreeshipDiscountAmount());
        dto.setFinalAmount(entity.getFinalAmount());
        dto.setPaymentMethod(entity.getPaymentMethodString());
        // dto.setStatus(entity.getStatus());
        dto.setStatus(entity.getStatusText());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setNote(entity.getNote());

        if (entity.getItems() != null) {
            List<OrderItemDTO> itemDTOs = new ArrayList<>();
            for (OrderItem item : entity.getItems()) {
                OrderItemDTO idto = new OrderItemDTO();
                idto.setFoodId(item.getFoodId());
                idto.setImageUrl(item.getImageUrl());
                idto.setName(item.getName());
                idto.setOptions(item.getOptions());
                idto.setQuantity(item.getQuantity());
                idto.setPrice(item.getPrice());
                itemDTOs.add(idto);
            }
            dto.setItems(itemDTOs);
        }
        return dto;
    }

    private Order convertToEntity(OrderDTO dto) {
        Order entity = new Order();
        entity.setId(dto.getId());
        entity.setUserId(dto.getUserId());
        entity.setStoreId(dto.getStoreId());
        entity.setStoreName(dto.getStoreName());
        entity.setCode(dto.getCode());
        entity.setDeliveryAddress(dto.getDeliveryAddress());
        entity.setReceiverName(dto.getReceiverName());
        entity.setReceiverPhone(dto.getReceiverPhone());
        entity.setDeliveryFee(dto.getDeliveryFee());
        entity.setDriverName(dto.getDriverName());
        entity.setDriverPhone(dto.getDriverPhone());
        entity.setTotalAmount(dto.getTotalAmount());
        entity.setDiscountAmount(dto.getDiscountAmount());
        entity.setShopDiscountAmount(dto.getShopDiscountAmount());
        entity.setFreeshipDiscountAmount(dto.getFreeshipDiscountAmount());
        entity.setFinalAmount(dto.getFinalAmount());
        entity.setPaymentMethod(dto.getPaymentMethod());
        entity.setStatus(dto.getStatus());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedAt(dto.getUpdatedAt());
        entity.setNote(dto.getNote());

        if (dto.getItems() != null) {
            List<OrderItem> items = new ArrayList<>();
            for (OrderItemDTO itemDTO : dto.getItems()) {
                OrderItem item = new OrderItem();
                item.setFoodId(itemDTO.getFoodId());
                item.setImageUrl(itemDTO.getImageUrl());
                item.setName(itemDTO.getName());
                item.setOptions(itemDTO.getOptions());
                item.setQuantity(itemDTO.getQuantity());
                item.setPrice(itemDTO.getPrice());
                items.add(item);
            }
            entity.setItems(items);
        }
        return entity;
    }

    public List<OrderDTO> getAllOrders() throws ExecutionException, InterruptedException {
        List<Order> orders = orderRepository.findAllOrders();
        List<OrderDTO> dtos = new ArrayList<>();
        for (Order o : orders) {
            dtos.add(convertToDTO(o));
        }
        return dtos;
    }

    private String getOrderCodeDisplay(Order order) {
        if (order.getCode() != null && !order.getCode().trim().isEmpty()) {
            return order.getCode();
        }
        if (order.getId() != null && order.getId().length() >= 6) {
            return order.getId().substring(order.getId().length() - 6).toUpperCase();
        }
        return "ORDER";
    }

    private String getOrderItemsSummary(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            return "các món ăn";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < order.getItems().size(); i++) {
            OrderItem item = order.getItems().get(i);
            sb.append(item.getName());
            if (item.getQuantity() > 1) {
                sb.append(" (x").append(item.getQuantity()).append(")");
            }
            if (i < order.getItems().size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
