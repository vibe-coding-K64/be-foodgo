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
@Schema(description = "Thong tin vi tien")
public class WalletDTO {

    @Schema(description = "ID vi", example = "wallet_001")
    private String id;

    @Schema(description = "ID chu vi", example = "user_001")
    private String userId;

    @Schema(description = "Vai tro", example = "driver")
    private String role;

    @Schema(description = "So du hien tai (VND)", example = "2500000.0")
    private Double balance;

    @Schema(description = "Tong thu nhap tu truoc den nay (VND)", example = "5000000.0")
    private Double totalEarned;

    @Schema(description = "Tong so da rut (VND)", example = "2500000.0")
    private Double totalWithdrawn;

    @Schema(description = "So du cho (chua giai ngan, VND)", example = "0.0")
    private Double pendingBalance;

    @Schema(description = "Ten ngan hang thu huong", example = "Vietcombank - CN TP.HCM")
    private String bankName;

    @Schema(description = "So tai khoan ngan hang", example = "012345678901")
    private String bankAccountNumber;

    @Schema(description = "Ten nguoi thu huong", example = "NGUYEN VAN A")
    private String bankAccountName;

    @Schema(description = "Thoi diem tao vi")
    private Instant createdAt;

    @Schema(description = "Thoi diem cap nhat gan nhat")
    private Instant updatedAt;
}
