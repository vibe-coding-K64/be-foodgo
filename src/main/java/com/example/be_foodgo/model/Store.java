package com.example.be_foodgo.model;

import lombok.Data;

import java.util.List;

@Data
public class Store {
    private String id;
    private String name;
    private String description;
    private String address;
    private Double rating;
    private Integer reviewCount;
    private String avtUrl;
    private String backUrl;
    @com.google.cloud.firestore.annotation.PropertyName("isOpen")
    private boolean isOpen;
    private String deliveryTime;
    private Double deliveryFee;
    private List<String> categoryIds;
    private Object restaurant_categories;
    private Double lat;
    private Double lng;
    private java.util.Date createdAt;
    private java.util.Date updatedAt;
}
