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
@Schema(description = "Thong tin ho so tai xe")
public class DeliveryProfileDTO {

    @Schema(description = "ID tai xe (trung voi userId)", example = "user_001")
    private String id;

    @Schema(description = "Bien so xe", example = "59A-123.45")
    private String vehiclePlate;

    @Schema(description = "Loai phuong tien", example = "Honda Wave Alpha")
    private String vehicleType;

    @Schema(description = "Bang lai xe", example = "DL123456789")
    private String driverLicense;

    @Schema(description = "Trang thai online (san sang nhan don)", example = "true")
    private Boolean isActive;

    @Schema(description = "Tai xe co dang rahnh nhan don khong", example = "true")
    private Boolean isAvailable;

    @Schema(description = "Diem danh gia trung binh (0.0 - 5.0)", example = "4.9")
    private Double rating;

    @Schema(description = "Tong so chuyen giao thanh cong", example = "150")
    private Long totalTrips;

    @Schema(description = "Tong thu nhap tu truoc den nay (VND)", example = "7500000.0")
    private Double totalEarnings;

    @Schema(description = "ID don hang dang giao hien tai", example = "order_001")
    private String currentOrderId;

    @Schema(description = "Vi do hien tai", example = "10.8500")
    private Double lat;

    @Schema(description = "Kinh do hien tai", example = "106.7900")
    private Double lng;

    @Schema(description = "Email tai xe", example = "driver@example.com")
    private String email;

    @Schema(description = "Ho va ten day du", example = "Le Van A")
    private String fullName;

    @Schema(description = "So dien thoai", example = "0912345678")
    private String phoneNumber;

    @Schema(description = "URL anh dai dien tai xe")
    private String photoUrl;

    @Schema(description = "Thoi diem tao ho so")
    private Instant createdAt;

    @Schema(description = "Thoi diem cap nhat gan nhat")
    private Instant updatedAt;
}
