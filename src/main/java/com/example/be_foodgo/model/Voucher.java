package com.example.be_foodgo.model;

import com.google.cloud.firestore.annotation.DocumentId;
import java.util.Date;

public class Voucher {
    @DocumentId
    private String id;
    private String storeId;
    private String code;
    private int type; // 1: %, 2: cash
    private double value;
    private double minOrder;
    private int limitCount;
    private int usedCount;
    private Date expiryDate;
    private boolean isActive;
    private Date createdAt;
    private Date updatedAt;

    public Voucher() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public int getType() { return type; }
    public void setType(int type) { this.type = type; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }

    public double getMinOrder() { return minOrder; }
    public void setMinOrder(double minOrder) { this.minOrder = minOrder; }

    public int getLimitCount() { return limitCount; }
    public void setLimitCount(int limitCount) { this.limitCount = limitCount; }

    public int getUsedCount() { return usedCount; }
    public void setUsedCount(int usedCount) { this.usedCount = usedCount; }

    public Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }

    public boolean getIsActive() { return isActive; }
    public void setIsActive(boolean isActive) { this.isActive = isActive; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
