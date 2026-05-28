package com.example.be_foodgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDTO {

    private String orderId;
    private List<String> targetDriverIds;
    private List<String> attemptedDriverIds;
    private String acceptedDriverId;
    private Double storeLat;
    private Double storeLng;
    private Double deliveryLat;
    private Double deliveryLng;
    private Double deliveryHeading;
    private Instant expiresAt;
    private String status;
    private Instant createdAt;
}
