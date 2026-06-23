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
public class PaymentMethod {

    private String id;
    private String name;
    private int type;
    private String details;
    private Boolean isDefault;
    private String cardBrand;
    private String last4Digits;
    private String walletBrand;
    private Boolean isLinked;
    private Instant createdAt;
    private Instant updatedAt;
}
