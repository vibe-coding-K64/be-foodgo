package com.example.be_foodgo.model;

import lombok.Data;

import java.util.List;

@Data
public class Store {
    private String id;
    private String name;
    private String description;
    private String address;
    private String taxCode;
    private String businessLicense;
    private String coverImageUrl;
    private String logoUrl;
    private String bankName;
    private String bankAccountNumber;
    private boolean isAcceptingOrders;
    private boolean isOpen;
    private Double lat;
    private Double lng;
    private Double rating;
    private Integer reviewCount;
    private String avtUrl;
    private String backUrl;
    private String deliveryTime;
    private Double deliveryFee;
    private List<String> categoryIds;
}
