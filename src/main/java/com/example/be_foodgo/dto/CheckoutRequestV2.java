package com.example.be_foodgo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
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
    private String freeshpVoucherId;

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

        @Schema(description = "So luong", example = "2")
        private Integer quantity;

        @Schema(description = "URL anh mon an", example = "https://picsum.photos/seed/milktea1/200")
        private String imageUrl;

        @Schema(description = "Cac tuy chon da chon (topping)", nullable = true)
        private List<ItemOption> options;

        @Schema(description = "Ghi chu cho mon nay", example = "It duong", nullable = true)
        private String note;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Tuy chon cua mon an (VD: topping)")
    public static class ItemOption {

        @Schema(description = "Ten tuy chon", example = "Tran Chau")
        private String name;

        @Schema(description = "Gia tuy chon (VND)", example = "5000.0")
        private Double price;
    }
}
