package com.example.be_foodgo.dto;

import com.example.be_foodgo.dto.CartRequest.SelectedOptionGroup;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Phản hồi giỏ hàng của người dùng")
public class CartResponse {

    private List<CartItemResponse> items;
    private String storeId;
    private String storeName;
    private String storeImageUrl;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Chi tiết một món trong giỏ hàng")
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
        private String imageUrl;
        private String note;
        private Instant createdAt;
        private Instant updatedAt;

        @Schema(description = "Các tùy chọn đã chọn cho món này (VD: Kich thuoc, Topping)")
        private List<CartRequest.SelectedOptionGroup> selectedOptions;
    }
}
