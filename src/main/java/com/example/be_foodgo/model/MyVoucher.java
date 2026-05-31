package com.example.be_foodgo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyVoucher {

    private String id;
    private String name;
    private String title;
    private String subtitle;
    private String code;
    private String description;
    private int type; // 1: %, 2: cash (không phần trăm)
    private double value;
    private String imageUrl;
    private String terms;
    private double minOrderValue;
    private Date expiryDate;
    private boolean isActive;
    private boolean isFreeship;
    private Date createdAt;
    private Date updatedAt;
}
