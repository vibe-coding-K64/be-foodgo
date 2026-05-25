package com.example.be_foodgo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thong tin phuong thuc thanh toan tra ve cho client")
public class PaymentResponse {

    @Schema(description = "ID phuong thuc thanh toan", example = "pm_001")
    private String id;

    @Schema(description = "Ten hien thi cua phuong thuc thanh toan", example = "Vi MoMo cua toi")
    private String name;

    @Schema(description = "Loai phuong thuc thanh toan: momo, zalo, card, cash", example = "momo")
    private String type;

    @Schema(description = "Chi tiet bo sung", example = "**** **** **** 1234")
    private String details;

    @Schema(description = "La phuong thuc mac dinh", example = "true")
    private Boolean isDefault;

    @Schema(description = "Thuong hieu the (neu co)", example = "Visa")
    private String cardBrand;

    @Schema(description = "4 chu so cuoi the (neu co)", example = "1234")
    private String last4Digits;

    @Schema(description = "Thuong hieu vi dien tu (neu co)", example = "momo")
    private String walletBrand;

    @Schema(description = "Da lien ket chua", example = "true")
    private Boolean isLinked;

    @Schema(description = "Thoi diem tao", example = "2026-05-26T00:00:00Z")
    private Instant createdAt;

    @Schema(description = "Thoi diem cap nhat gan nhat", example = "2026-05-26T00:00:00Z")
    private Instant updatedAt;
}
