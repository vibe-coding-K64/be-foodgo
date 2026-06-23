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
public class FeaturedProductResponse {

    private String id;
    private String storeId;

    // Thong tin mon an
    private String name;
    private String description;
    private Double basePrice;
    private String imageUrl;
    private Boolean isOutOfStock;
    private Boolean isFeatured;
    private String categoryName;

    // Tu stores (join theo storeId)
    private String storeName;
    private String storeAvtUrl;
    private Double rating;
    private Integer reviewCount;
    private Boolean isOpen;
    private String deliveryTime;
    private Double deliveryFee;
    private String address;
    private Double distance;
    private Long sales;

    // OptionGroups (cho bottom sheet them gio hang)
    private List<OptionGroupDTO> optionGroups;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionGroupDTO {
        private String name;
        private Boolean isSingleSelect;
        private Boolean isRequired;
        private List<OptionDTO> options;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionDTO {
        private String name;
        private Double price;
    }
}
