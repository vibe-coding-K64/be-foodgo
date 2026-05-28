package com.example.be_foodgo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ket qua tim kiem mon an hoac quan an")
public class SearchResultResponse {

    @Schema(description = "ID cua san pham (mon an)", example = "prod_001")
    private String productId;

    @Schema(description = "Ten mon an", example = "Com tam suon bi cha")
    private String productName;

    @Schema(description = "ID cua cua hang", example = "store_001")
    private String storeId;

    @Schema(description = "Ten cua hang", example = "Com tam Phuc Loc Tho")
    private String storeName;

    @Schema(description = "Gia co so cua mon an (VND)", example = "45000.0")
    private double price;

    @Schema(description = "Diem danh gia trung binh cua cua hang (0.0 - 5.0)", example = "4.8")
    private Double rating;

    @Schema(description = "Tong so danh gia cua cua hang", example = "500")
    private Integer reviewCount;

    @Schema(description = "Khoang cach tu vi tri nguoi dung den cua hang (km)", example = "2.5")
    private Double distance;

    @Schema(description = "URL hinh anh mon an", example = "https://images.unsplash.com/photo-xxx")
    private String imageUrl;

    @Schema(description = "Danh sach nhom tuy chon cua mon an (VD: kich thuoc, topping, duong)")
    private List<OptionGroupDTO> optionGroups;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Nhom tuy chon (VD: Kich thuoc, Topping)")
    public static class OptionGroupDTO {

        @Schema(description = "Ten nhom tuy chon", example = "Kich thuoc")
        private String name;

        @Schema(description = "Cho phep chon nhieu tuy chon (true = mot tuy chon, false = nhieu tuy chon)", example = "true")
        private Boolean isSingleSelect;

        @Schema(description = "Bat buoc chon tuy chon nay", example = "false")
        private Boolean isRequired;

        @Schema(description = "So tuy chon toi da co the chon", example = "1")
        private Integer maxChoices;

        @Schema(description = "Danh sach cac tuy chon trong nhom")
        private List<OptionDTO> options;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Tuy chon trong nhom (VD: Nho (+0đ), Lon (+5000đ))")
    public static class OptionDTO {

        @Schema(description = "Ten tuy chon", example = "Lon")
        private String name;

        @Schema(description = "Gia them cua tuy chon (VND)", example = "5000.0")
        private double price;
    }
}
