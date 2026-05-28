package com.example.be_foodgo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyVoucher {

    private String id;
    private String name;
    private String code;
    private String description;
    private Instant expiryDate;
    private Double discountValue;
    private Boolean isPercentage;
    private Double minOrderValue;
    private Instant createdAt;
    private Instant updatedAt;
}
