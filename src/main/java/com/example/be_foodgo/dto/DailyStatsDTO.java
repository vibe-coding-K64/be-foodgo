package com.example.be_foodgo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thong ke theo ngay")
public class DailyStatsDTO {

    @Schema(description = "Ngày thong ke", example = "2026-05-27")
    private LocalDate date;

    @Schema(description = "Thu nhap trong ngay (VND)", example = "150000.0")
    private Double earnings;

    @Schema(description = "So chuyen trong ngay", example = "5")
    private Long trips;

    @Schema(description = "Diem danh gia trung binh trong ngay (0.0 - 5.0)", example = "4.8")
    private Double rating;
}
