package com.example.be_foodgo.model;

import lombok.Data;

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
    private Double lat;
    private Double lng;
}
