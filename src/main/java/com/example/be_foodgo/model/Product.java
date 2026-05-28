package com.example.be_foodgo.model;

import lombok.Data;
import java.util.List;

@Data
public class Product {
    private String id;
    private String storeId;
    private String categoryId;
    private String categoryName;
    private String name;
    private String description;
    private double basePrice;
    private String imageUrl;
    private Boolean isOutOfStock;
    private Boolean isFeatured;
    private List<ProductOptionGroup> optionGroups;
    private com.google.cloud.Timestamp createdAt;
    private com.google.cloud.Timestamp updatedAt;

    @Data
    public static class ProductOptionGroup {
        private String name;
        private Boolean isSingleSelect;
        private Boolean isRequired;
        private int maxChoices;
        private List<ProductOption> options;
    }

    @Data
    public static class ProductOption {
        private String name;
        private double price;
    }
}
