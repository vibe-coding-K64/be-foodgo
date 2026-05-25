package com.example.be_foodgo.model;

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
public class CartItem {

    private String id;
    private String storeId;
    private String foodId;
    private String name;
    private Double price;
    private Integer quantity;
    private String size;
    private Double sizePrice;
    private List<ToppingItem> toppings;
    private String note;
    private String imageUrl;
    private Instant createdAt;
    private Instant updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToppingItem {

        private String name;
        private Double price;
    }
}
