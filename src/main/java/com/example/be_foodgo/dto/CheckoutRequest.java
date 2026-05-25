package com.example.be_foodgo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu đặt hàng (Checkout) từ phía khách hàng")
public class CheckoutRequest {

    @NotBlank(message = "userId khong duoc de trong")
    @Schema(description = "ID nguoi dung khach hang", example = "user_001")
    private String userId;

    @NotBlank(message = "addressId khong duoc de trong")
    @Schema(description = "ID dia chi giao hang cua khach hang", example = "addr_001")
    private String addressId;

    @NotBlank(message = "paymentMethod khong duoc de trong")
    @Pattern(regexp = "^(cash|momo|zalo|card)$", message = "paymentMethod phai la mot trong cac gia tri: cash, momo, zalo, card")
    @Schema(description = "Phuong thuc thanh toan", example = "momo",
            allowableValues = {"cash", "momo", "zalo", "card"})
    private String paymentMethod;

    @Schema(description = "ID voucher su dung (co the null)", example = "sys_voucher_001", nullable = true)
    private String voucherId;

    @Schema(description = "Ghi chu cho don hang (co the null)", example = "Giao gap", nullable = true)
    private String note;
}
