package com.example.be_foodgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NearbyStoreResponse {

    private String id;
    private String name;
    private String address;
    private Double rating;
    private Integer reviewCount;
    private String avtUrl;
    private String deliveryTime;
    private Double deliveryFee;
    private Double distance;
    private Boolean isOpen;
    private List<String> categoryIds;
}
