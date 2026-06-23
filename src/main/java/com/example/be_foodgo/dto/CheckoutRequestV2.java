package com.example.be_foodgo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yeu cau dat hang (Checkout) V2 - FE gui items truc tiep")
public class CheckoutRequestV2 {

    @NotBlank(message = "userId khong duoc de trong")
    @Schema(description = "ID nguoi dung khach hang", example = "user_001")
    private String userId;

    @NotBlank(message = "addressId khong duoc de trong")
    @Schema(description = "ID dia chi giao hang cua khach hang", example = "addr_001")
    private String addressId;

    @NotBlank(message = "paymentMethod khong duoc de trong")
    @Schema(description = "ID phuong thuc thanh toan (tu bang payment_methods cua user)", example = "pm_001")
    private String paymentMethod;

    @NotBlank(message = "storeId khong duoc de trong")
    @Schema(description = "ID cua hang", example = "store_001")
    private String storeId;

    @NotEmpty(message = "Danh sach items khong duoc de trong")
    @Valid
    @Schema(description = "Danh sach mon an trong don")
    private List<CheckoutItem> items;

    @Schema(description = "Ghi chu cho don hang", example = "Giao gio hanh chinh", nullable = true)
    private String note;

    @Schema(description = "ID voucher giam gia (he thong)", example = "sys_voucher_001", nullable = true)
    private String discountVoucherId;

    @Schema(description = "ID voucher cua hang", example = "voucher_001", nullable = true)
    private String shopVoucherId;

    @Schema(description = "ID voucher freeship", example = "mv_001", nullable = true)
    @JsonProperty("freeshipVoucherId")
    private String freeshpVoucherId;

    @Schema(description = "Idempotency key de chong dat hang trung lap. Neu gui cung key 2 lan, request thu 2 se tra ve 409 Conflict. Vi du: UUID", example = "550e8400-e29b-41d4-a716-446655440000", nullable = true)
    private String idempotencyKey;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Thong tin mot mon an trong yeu cau checkout")
    public static class CheckoutItem {

        @NotBlank(message = "foodId khong duoc de trong")
        @Schema(description = "ID san pham", example = "prod_001")
        private String foodId;

        @Schema(description = "Ten mon an", example = "Tra Sua Tran Chau Duong")
        private String name;

        @Min(value = 1, message = "So luong phai lon hon 0")
        @Schema(description = "So luong", example = "2")
        private Integer quantity;

        @Schema(description = "URL anh mon an", example = "https://picsum.photos/seed/milktea1/200")
        private String imageUrl;

        @Schema(description = "Cac nhom tuy chon da chon (size, topping)", nullable = true)
        private List<SelectedOptionGroup> selectedOptions;

        @Schema(description = "Ghi chu cho mon nay", example = "It duong", nullable = true)
        private String note;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Nhom tuy chon da chon (VD: Kich thuoc, Topping)")
    public static class SelectedOptionGroup {

        @Schema(description = "Ten nhom tuy chon", example = "Kich thuoc")
        private String name;

        @Schema(description = "Danh sach cac tuy chon da chon trong nhom nay", nullable = true)
        private List<SelectedOption> options;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Mot tuy chon da chon (VD: Lon, Tran Chau)")
    public static class SelectedOption {

        @Schema(description = "Ten tuy chon", example = "Tran Chau")
        private String name;
    }
}
