package com.example.be_foodgo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yeu cau them phuong thuc thanh toan moi")
public class PaymentRequest {

    @NotBlank(message = "Loai phuong thuc thanh toan khong duoc de trong")
    @Schema(description = "Loai phuong thuc thanh toan: momo, zalo, card, cash", example = "momo")
    private String type;

    @NotBlank(message = "Ten phuong thuc thanh toan khong duoc de trong")
    @Schema(description = "Ten hien thi cua phuong thuc thanh toan", example = "Vi MoMo cua toi")
    private String name;

    @Schema(description = "Chi tiet bo sung (VD: so the, ten ngan hang)", example = "**** **** **** 1234")
    private String details;

    @Schema(description = "Dat lam phuong thuc mac dinh", example = "true")
    private Boolean isDefault;
}
