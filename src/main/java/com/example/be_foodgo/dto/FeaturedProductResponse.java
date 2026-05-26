package com.example.be_foodgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeaturedProductResponse {

    private String id;
    private String name;
    private String description;
    private Double basePrice;
    private String imageUrl;
    private Boolean isOutOfStock;
    private StoreSummary store;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StoreSummary {
        private String id;
        private String name;
        private Double rating;
        private String avtUrl;
        private Double deliveryFee;
        private String deliveryTime;
    }
}
