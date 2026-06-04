package com.example.be_foodgo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StoreDTO {
    private String id;
    
    @NotBlank(message = "Tên quán không được để trống")
    private String name;
    
    private String description;
    private String address;
    private Double rating;
    private Integer reviewCount;
    private String avtUrl;
    private String backUrl;
    private boolean isOpen;
    private String approvalStatus;
    private String rejectReason;

    @com.fasterxml.jackson.annotation.JsonProperty("isOpen")
    public boolean getIsOpen() {
        return isOpen;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("isOpen")
    public void setIsOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }
    private String deliveryTime;
    private Double deliveryFee;
    private java.util.List<String> categoryIds;
    private Object restaurant_categories;
    private Double lat;
    private Double lng;
}
