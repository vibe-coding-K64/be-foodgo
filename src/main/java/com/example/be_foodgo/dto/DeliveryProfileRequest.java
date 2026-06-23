package com.example.be_foodgo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yeu cau cap nhat ho so tai xe")
public class DeliveryProfileRequest {

    @Size(max = 100, message = "Ho ten khong duoc vuot qua 100 ky tu")
    @Schema(description = "Ho va ten day du", example = "Le Van A")
    private String fullName;

    @Schema(description = "So dien thoai di dong", example = "0912345678")
    private String phoneNumber;

    @Schema(description = "URL anh dai dien")
    private String photoUrl;

    @Size(max = 20, message = "Bien so xe khong duoc vuot qua 20 ky tu")
    @Schema(description = "Bien so xe", example = "59A-123.45")
    private String vehiclePlate;

    @Size(max = 50, message = "Loai phuong tien khong duoc vuot qua 50 ky tu")
    @Schema(description = "Loai phuong tien", example = "Honda Wave Alpha")
    private String vehicleType;

    @Size(max = 30, message = "Bang lai xe khong duoc vuot qua 30 ky tu")
    @Schema(description = "Bang lai xe", example = "DL123456789")
    private String driverLicense;
}
