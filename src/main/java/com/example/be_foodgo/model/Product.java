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
    private boolean isOutOfStock;
    private boolean isFeatured;
    private List<ProductOptionGroup> optionGroups;

    @Data
    public static class ProductOptionGroup {
        private String name;
        private boolean isRequired;
        private int maxChoices;
        private List<ProductOption> options;
    }

    @Data
    public static class ProductOption {
        private String name;
        private double price;
    }
}
