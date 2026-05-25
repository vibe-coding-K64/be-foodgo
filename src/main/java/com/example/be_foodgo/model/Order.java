package com.example.be_foodgo.model;

import com.google.cloud.firestore.annotation.DocumentId;
import java.util.Date;
import java.util.List;

public class Order {
    private String id;
    private String userId;
    private String storeId;
    private String code;
    
    private String customerName;
    private String customerPhone;
    private String deliveryAddress;
    
    private String driverName;
    private String driverPhone;
    
    private List<OrderItem> items;
    
    private double totalAmount;
    private double shippingFee;
    private double discountAmount;
    private double finalAmount;
    private String paymentMethod;
    
    private Object status; // 0=Chờ xác nhận, 1=Đang chuẩn bị, 2=Đang giao, 3=Hoàn thành, 4=Đã hủy
    private Date createdAt;
    private Date updatedAt;

    public Order() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    public String getDriverPhone() { return driverPhone; }
    public void setDriverPhone(String driverPhone) { this.driverPhone = driverPhone; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public double getShippingFee() { return shippingFee; }
    public void setShippingFee(double shippingFee) { this.shippingFee = shippingFee; }
    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }
    public double getFinalAmount() { return finalAmount; }
    public void setFinalAmount(double finalAmount) { this.finalAmount = finalAmount; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getStatus() {
        if (status instanceof Number) {
            long val = ((Number) status).longValue();
            if (val == 0) return "Chờ xác nhận";
            if (val == 1) return "Đang chuẩn bị";
            if (val == 2) return "Đang giao";
            if (val == 3) return "Hoàn thành";
            if (val == 4) return "Đã hủy";
            return "Chờ xác nhận";
        } else if (status != null) {
            return status.toString();
        }
        return "Chờ xác nhận";
    }

    public int getStatusValue() {
        if (status instanceof Number) {
            return ((Number) status).intValue();
        } else if (status instanceof String) {
            try {
                return Integer.parseInt((String) status);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
    public void setStatus(Object status) { this.status = status; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
