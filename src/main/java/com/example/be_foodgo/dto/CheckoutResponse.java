package com.example.be_foodgo.dto;

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
@Schema(description = "Phan hoi sau khi dat hang thanh cong")
public class CheckoutResponse {

    @Schema(description = "ID don hang duoc tao", example = "AbCdEfGhIjKlMnOpQrStUvWxYz123456")
    private String orderId;

    @Schema(description = "Ma don hang", example = "QRSTUV")
    private String orderCode;

    @Schema(description = "ID cua hang", example = "store_001")
    private String storeId;

    @Schema(description = "Ten cua hang", example = "Com tam Phuc Loc Tho")
    private String storeName;

    @Schema(description = "ID nguoi dung", example = "user_001")
    private String userId;

    @Schema(description = "Danh sach mon an trong don")
    private List<OrderItemData> items;

    @Schema(description = "Tong tien hang (chua tinh ph ship va giam gia)", example = "90000.0")
    private Double totalAmount;

    @Schema(description = "Phi giao hang", example = "15000.0")
    private Double deliveryFee;

    @Schema(description = "So tien duoc giam tu voucher he thong (discountVoucher)", example = "20000.0")
    private Double discountAmount;

    @Schema(description = "So tien duoc giam tu voucher cua hang (shopVoucher)", example = "5000.0")
    private Double shopDiscountAmount;

    @Schema(description = "So tien duoc giam tu voucher freeship (freeshipVoucher)", example = "15000.0")
    private Double freeshipDiscountAmount;

    @Schema(description = "Tong so tien phai thanh toan", example = "85000.0")
    private Double finalAmount;

    @Schema(description = "Phuong thuc thanh toan", example = "momo")
    private String paymentMethod;

    @Schema(description = "Dia chi giao hang", example = "Ky tuc xa UTC2, Quan 9, TP.HCM")
    private String deliveryAddress;

    @Schema(description = "Trang thai don hang (0 = Cho xac nhan)", example = "0")
    private Integer status;

    @Schema(description = "Thoi gian tao don", example = "2026-05-25T10:30:00Z")
    private Instant createdAt;

    @Schema(description = "Ghi chu", example = "Giao gap")
    private String note;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Thong tin mot tuy chon (topping/size) cua mon an")
    public static class ItemOption {

        @Schema(description = "Ten tuy chon", example = "Tran chau")
        private String name;

        @Schema(description = "Gia cua tuy chon (VND)", example = "5000.0")
        private Double price;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Thong tin mot mon an trong don")
    public static class OrderItemData {

        @Schema(description = "ID san pham", example = "prod_001")
        private String foodId;

        @Schema(description = "Ten mon an", example = "Com tam suon bi cha")
        private String name;

        @Schema(description = "Don gia", example = "45000.0")
        private Double price;

        @Schema(description = "So luong", example = "2")
        private Integer quantity;

        @Schema(description = "URL anh mon an", example = "https://images.unsplash.com/photo-xxx")
        private String imageUrl;

        @Schema(description = "Kich thuoc (VD: S, M, L)", example = "M", nullable = true)
        private String size;

        @Schema(description = "Cac tuy chon da chon (size, topping)", example = "[{\"name\": \"Tran chau\", \"price\": 5000.0}]")
        private List<ItemOption> options;
    }
}
