package com.example.be_foodgo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import java.util.List;

@Data
public class ProductDTO {
    
    @NotBlank(message = "Mã cửa hàng không được để trống")
    private String storeId;

    @NotBlank(message = "Danh mục không được để trống")
    private String categoryId;

    @NotBlank(message = "Tên danh mục không được để trống")
    private String categoryName;

    @NotBlank(message = "Tên món ăn không được để trống")
    private String name;

    private String description;

    @NotNull(message = "Giá không được để trống")
    @PositiveOrZero(message = "Giá phải lớn hơn hoặc bằng 0")
    private Double basePrice;

    private String imageUrl;
    
    private Boolean isOutOfStock;
    private Boolean isFeatured;

    private List<ProductOptionGroupDTO> optionGroups;

    @Data
    public static class ProductOptionGroupDTO {
        @NotBlank(message = "Tên nhóm không được để trống")
        private String name;
        private Boolean isSingleSelect;
        private Boolean isRequired;
        private int maxChoices;
        private List<ProductOptionDTO> options;
    }

    @Data
    public static class ProductOptionDTO {
        @NotBlank(message = "Tên lựa chọn không được để trống")
        private String name;
        @PositiveOrZero(message = "Giá lựa chọn phải >= 0")
        private double price;
    }
}
