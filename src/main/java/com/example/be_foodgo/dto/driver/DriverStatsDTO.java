package com.example.be_foodgo.dto.driver;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tong quan thu nhap va thong ke cua tai xe")
public class DriverStatsDTO {

    @Schema(description = "Tong thu nhap tu truoc den nay (VND)", example = "7500000.0")
    private Double totalEarnings;

    @Schema(description = "So du hien tai (VND)", example = "2500000.0")
    private Double balance;

    @Schema(description = "Tong so chuyen giao thanh cong", example = "150")
    private Long totalTrips;

    @Schema(description = "Diem danh gia trung binh (0.0 - 5.0)", example = "4.9")
    private Double averageRating;

    @Schema(description = "Thu nhap hom nay (VND)", example = "150000.0")
    private Double todayEarnings;

    @Schema(description = "So chuyen hom nay", example = "5")
    private Long todayTrips;

    @Schema(description = "Thu nhap thang nay (VND)", example = "3500000.0")
    private Double monthEarnings;

    @Schema(description = "So chuyen thang nay", example = "42")
    private Long monthTrips;
}
