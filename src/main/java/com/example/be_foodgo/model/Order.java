package com.example.be_foodgo.model;

import com.google.cloud.firestore.annotation.DocumentId;
import java.util.Date;
import java.util.List;

public class Order {
    private String id;
    private String userId;
    private String storeId;
    private String storeName;
    private String code;

    private String deliveryAddress;
    private String addressId;

    private String receiverName;
    private String receiverPhone;
    private double deliveryFee;

    private String driverName;
    private String driverPhone;

    private List<OrderItem> items;

    private double totalAmount;
    private double discountAmount;
    private double shopDiscountAmount;
    private double freeshipDiscountAmount;
    private double finalAmount;
    private Object paymentMethod;

    private Object status; // 0=Chờ xác nhận, 1=Đang chuẩn bị, 2=Đang giao, 3=Hoàn thành, 4=Đã hủy
    private Object createdAt;
    private Object updatedAt;
    private Object deletedAt;

    private Double deliveryHeading;
    private Double deliveryLat;
    private Double deliveryLng;

    private String note;

    private String deliveryStep;

    public Order() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }
    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public String getAddressId() { return addressId; }
    public void setAddressId(String addressId) { this.addressId = addressId; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }
    public double getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(double deliveryFee) { this.deliveryFee = deliveryFee; }
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    public String getDriverPhone() { return driverPhone; }
    public void setDriverPhone(String driverPhone) { this.driverPhone = driverPhone; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }
    public double getShopDiscountAmount() { return shopDiscountAmount; }
    public void setShopDiscountAmount(double shopDiscountAmount) { this.shopDiscountAmount = shopDiscountAmount; }
    public double getFreeshipDiscountAmount() { return freeshipDiscountAmount; }
    public void setFreeshipDiscountAmount(double freeshipDiscountAmount) { this.freeshipDiscountAmount = freeshipDiscountAmount; }
    public double getFinalAmount() { return finalAmount; }
    public void setFinalAmount(double finalAmount) { this.finalAmount = finalAmount; }
    public int getPaymentMethod() {
        if (paymentMethod instanceof Number) {
            return ((Number) paymentMethod).intValue();
        } else if (paymentMethod instanceof String) {
            try {
                return Integer.parseInt((String) paymentMethod);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    public String getPaymentMethodString() {
        if (paymentMethod == null) return "MoMo";
        if (paymentMethod instanceof Number) {
            int val = ((Number) paymentMethod).intValue();
            if (val == 1) return "MoMo";
            if (val == 2) return "Tiền mặt";
            if (val == 3) return "ZaloPay";
            if (val == 4) return "Thẻ ngân hàng";
            return "MoMo";
        }
        String str = paymentMethod.toString().toLowerCase().trim();
        if ("1".equals(str) || "momo".equals(str)) return "MoMo";
        if ("2".equals(str) || "cash".equals(str) || "tiền mặt".equals(str) || "tien mat".equals(str)) return "Tiền mặt";
        if ("3".equals(str) || "zalo".equals(str) || "zalopay".equals(str)) return "ZaloPay";
        if ("4".equals(str) || "vnpay".equals(str) || "card".equals(str)) return "Thẻ ngân hàng";
        return str;
    }

    public void setPaymentMethod(Object paymentMethod) { this.paymentMethod = paymentMethod; }

    private Integer paymentStatus;

    public Integer getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(Integer paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Object getStatus() {
        return status;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    @com.google.cloud.firestore.annotation.Exclude
    public String getStatusText() {
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

    @com.fasterxml.jackson.annotation.JsonIgnore
    @com.google.cloud.firestore.annotation.Exclude
    public String getStatusAsString() {
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

    @com.fasterxml.jackson.annotation.JsonIgnore
    @com.google.cloud.firestore.annotation.Exclude
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
    public Date getCreatedAt() { return parseDate(createdAt); }
    public void setCreatedAt(Object createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return parseDate(updatedAt); }
    public void setUpdatedAt(Object updatedAt) { this.updatedAt = updatedAt; }
    public Date getDeletedAt() { return parseDate(deletedAt); }
    public void setDeletedAt(Object deletedAt) { this.deletedAt = deletedAt; }

    private Date parseDate(Object val) {
        if (val == null) return null;
        if (val instanceof Date) return (Date) val;
        if (val instanceof com.google.cloud.Timestamp) return ((com.google.cloud.Timestamp) val).toDate();
        if (val instanceof java.util.Map) {
            java.util.Map<?, ?> map = (java.util.Map<?, ?>) val;
            if (map.containsKey("_seconds")) {
                long sec = ((Number) map.get("_seconds")).longValue();
                return new Date(sec * 1000);
            }
        }
        if (val instanceof Long) return new Date((Long) val);
        return null;
    }
    public Double getDeliveryHeading() { return deliveryHeading; }
    public void setDeliveryHeading(Double deliveryHeading) { this.deliveryHeading = deliveryHeading; }
    public Double getDeliveryLat() { return deliveryLat; }
    public void setDeliveryLat(Double deliveryLat) { this.deliveryLat = deliveryLat; }
    public Double getDeliveryLng() { return deliveryLng; }
    public void setDeliveryLng(Double deliveryLng) { this.deliveryLng = deliveryLng; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getDeliveryStep() { return deliveryStep; }
    public void setDeliveryStep(String deliveryStep) { this.deliveryStep = deliveryStep; }
}
