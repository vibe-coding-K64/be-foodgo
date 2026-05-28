package com.example.be_foodgo.dto.driver;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yeu cau cap nhat thong tin phuong tien cua tai xe")
public class DriverUpdateVehicleRequest {

    @NotBlank(message = "Bien so xe khong duoc de trong")
    @Size(max = 20, message = "Bien so xe khong duoc vuot qua 20 ky tu")
    @Schema(description = "Bien so xe", example = "59A-123.45")
    private String vehiclePlate;

    @NotBlank(message = "Loai phuong tien khong duoc de trong")
    @Size(max = 50, message = "Loai phuong tien khong duoc vuot qua 50 ky tu")
    @Schema(description = "Loai phuong tien", example = "Honda Wave Alpha")
    private String vehicleType;

    @NotBlank(message = "Bang lai xe khong duoc de trong")
    @Size(max = 30, message = "Bang lai xe khong duoc vuot qua 30 ky tu")
    @Schema(description = "Bang lai xe", example = "DL123456789")
    private String driverLicense;
}
