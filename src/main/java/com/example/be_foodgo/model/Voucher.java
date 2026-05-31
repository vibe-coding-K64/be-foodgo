package com.example.be_foodgo.model;

import java.util.Date;

public class Voucher {

    private String id;
    private String storeId;
    private String name;
    private String title;
    private String subtitle;
    private String code;
    private int type;
    private double value;
    private int pointsRequired;
    private String imageUrl;
    private int remaining;
    private String terms;
    private double minOrderValue;
    private int limitCount;
    private int usedCount;
    private Date expiryDate;
    private boolean isActive;
    private int validityDays;
    private boolean isFreeship;
    private Date createdAt;
    private Date updatedAt;

    public Voucher() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public int getType() { return type; }
    public void setType(int type) { this.type = type; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }

    public int getPointsRequired() { return pointsRequired; }
    public void setPointsRequired(int pointsRequired) { this.pointsRequired = pointsRequired; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getRemaining() { return remaining; }
    public void setRemaining(int remaining) { this.remaining = remaining; }

    public String getTerms() { return terms; }
    public void setTerms(String terms) { this.terms = terms; }

    public double getMinOrderValue() { return minOrderValue; }
    public void setMinOrderValue(double minOrderValue) { this.minOrderValue = minOrderValue; }

    public int getLimitCount() { return limitCount; }
    public void setLimitCount(int limitCount) { this.limitCount = limitCount; }

    public int getUsedCount() { return usedCount; }
    public void setUsedCount(int usedCount) { this.usedCount = usedCount; }

    public Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }

    public boolean getIsActive() { return isActive; }
    public void setIsActive(boolean isActive) { this.isActive = isActive; }

    public int getValidityDays() { return validityDays; }
    public void setValidityDays(int validityDays) { this.validityDays = validityDays; }

    public boolean getIsFreeship() { return isFreeship; }
    public void setIsFreeship(boolean isFreeship) { this.isFreeship = isFreeship; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
