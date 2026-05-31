package com.example.be_foodgo.service;

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
        return orderRepository.save(order);
    }

    public String updateOrderStatus(String id, String status) throws ExecutionException, InterruptedException {
        Order order = orderRepository.findById(id);
        if (order != null) {
            order.setStatus(status);
            return orderRepository.update(id, order);
        }
        return null;
    }

    public OrderDTO cancelOrder(String orderId, String userId) throws ExecutionException, InterruptedException {
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

        Map<String, Object> fields = new HashMap<>();
        fields.put("status", 4);
        fields.put("updatedAt", order.getUpdatedAt());
        orderRepository.updateFields(orderId, fields);

        return convertToDTO(order);
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

        String cusName = entity.getCustomerName();
        if (cusName == null || cusName.trim().isEmpty()) {
            cusName = "Khách hàng";
        }
        dto.setCustomerName(cusName);
        dto.setCustomerPhone(entity.getCustomerPhone());
        dto.setDeliveryAddress(entity.getDeliveryAddress());
        dto.setDriverName(entity.getDriverName());
        dto.setDriverPhone(entity.getDriverPhone());
        dto.setTotalAmount(entity.getTotalAmount());
        dto.setShippingFee(entity.getShippingFee());
        dto.setDiscountAmount(entity.getDiscountAmount());
        dto.setFinalAmount(entity.getFinalAmount());
        dto.setPaymentMethod(entity.getPaymentMethod());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setNote(entity.getNote());

        if (entity.getItems() != null) {
            List<OrderItemDTO> itemDTOs = new ArrayList<>();
            for (OrderItem item : entity.getItems()) {
                OrderItemDTO idto = new OrderItemDTO();
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
        entity.setCustomerName(dto.getCustomerName());
        entity.setCustomerPhone(dto.getCustomerPhone());
        entity.setDeliveryAddress(dto.getDeliveryAddress());
        entity.setDriverName(dto.getDriverName());
        entity.setDriverPhone(dto.getDriverPhone());
        entity.setTotalAmount(dto.getTotalAmount());
        entity.setShippingFee(dto.getShippingFee());
        entity.setDiscountAmount(dto.getDiscountAmount());
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
}
