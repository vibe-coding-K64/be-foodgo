package com.example.be_foodgo.model;

import com.google.cloud.firestore.annotation.DocumentId;
import java.util.Date;
import java.util.List;

public class Order {
    private String id;
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
    
    private Object status; // Chờ xác nhận, Đang chế biến, Đang giao, Hoàn thành, Đã hủy
    private Date createdAt;

    public Order() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
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
            if (val == 1) return "Chờ xác nhận";
            if (val == 2) return "Đang chế biến";
            if (val == 3) return "Đang giao";
            if (val == 4) return "Hoàn thành";
            if (val == 5) return "Đã hủy";
            return "Chờ xác nhận";
        } else if (status != null) {
            return status.toString();
        }
        return "Chờ xác nhận";
    }
    public void setStatus(Object status) { this.status = status; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
