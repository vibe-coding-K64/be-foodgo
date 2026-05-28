package com.example.be_foodgo.dto.driver;

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
@Schema(description = "Lich su giao dich cua tai xe")
public class DriverTransactionDTO {

    @Schema(description = "ID giao dich", example = "trans_001")
    private String id;

    @Schema(description = "ID vi lien quan", example = "wallet_001")
    private String walletId;

    @Schema(description = "ID nguoi thuc hien giao dich", example = "user_001")
    private String userId;

    @Schema(description = "Loai giao dich: delivery_income, withdrawal, refund", example = "delivery_income")
    private String type;

    @Schema(description = "Tong so tien giao dich (VND)", example = "30000.0")
    private Double amount;

    @Schema(description = "Phi giao dich (VND)", example = "4500.0")
    private Double fee;

    @Schema(description = "So tien thuc nhan = amount - fee (VND)", example = "25500.0")
    private Double netAmount;

    @Schema(description = "Mo ta giao dich")
    private String description;

    @Schema(description = "ID don hang lien quan (neu co)", example = "order_001")
    private String orderId;

    @Schema(description = "Trang thai: pending, completed, failed", example = "completed")
    private String status;

    @Schema(description = "Thoi diem tao giao dich")
    private Instant createdAt;
}
