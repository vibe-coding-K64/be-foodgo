package com.example.be_foodgo.model;

public class Conversation {
    private String id;
    private String orderId;
    private String customerId;
    private String customerName;
    private String driverId;
    private String driverName;
    private String lastMessage;
    private long lastMessageAt;
    private int unreadCustomer;
    private int unreadDriver;
    private String status;
    private long createdAt;
    private long updatedAt;

    public Conversation() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public long getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(long lastMessageAt) { this.lastMessageAt = lastMessageAt; }
    public int getUnreadCustomer() { return unreadCustomer; }
    public void setUnreadCustomer(int unreadCustomer) { this.unreadCustomer = unreadCustomer; }
    public int getUnreadDriver() { return unreadDriver; }
    public void setUnreadDriver(int unreadDriver) { this.unreadDriver = unreadDriver; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
