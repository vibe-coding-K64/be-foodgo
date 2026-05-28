package com.example.be_foodgo.dto;

import com.example.be_foodgo.model.CartItem;
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
public class CartResponse {

    private List<CartItemResponse> items;
    private String storeId;
    private String storeName;
    private String storeImageUrl;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemResponse {

        private String id;
        private String userId;
        private String storeId;
        private String storeName;
        private String storeImageUrl;
        private String foodId;
        private String name;
        private Double price;
        private Integer quantity;
        private String size;
        private Double sizePrice;
        private List<CartItem.ToppingItem> toppings;
        private String note;
        private String imageUrl;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
