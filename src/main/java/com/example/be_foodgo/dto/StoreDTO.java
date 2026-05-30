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
    @com.fasterxml.jackson.annotation.JsonProperty("isOpen")
    private boolean isOpen;
    private String deliveryTime;
    private Double deliveryFee;
    private java.util.List<String> categoryIds;
    private Object restaurant_categories;
    private Double lat;
    private Double lng;
}
