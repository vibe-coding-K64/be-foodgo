package com.example.be_foodgo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
