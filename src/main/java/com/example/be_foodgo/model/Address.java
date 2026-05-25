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
public class Address {

    private String id;
    private String name;
    private String address;
    private String receiverName;
    private String receiverPhone;
    private Double lat;
    private Double lng;
    private Boolean isDefault;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
}
