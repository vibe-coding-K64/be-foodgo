package com.example.be_foodgo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yeu cau bat/tat trang thai online")
public class DeliveryStatusRequest {

    @NotNull(message = "Trang thai isActive khong duoc de trong")
    @Schema(description = "true = san sang nhan don, false = offline", example = "true")
    private Boolean isActive;

    @DecimalMin(value = "-90.0", message = "Vĩ độ phải từ -90 đến 90")
    @DecimalMax(value = "90.0", message = "Vĩ độ phải từ -90 đến 90")
    @Schema(description = "Vĩ độ GPS khi bật trạng thái hoạt động (bắt buộc nếu isActive=true)", example = "10.8500")
    private Double lat;

    @DecimalMin(value = "-180.0", message = "Kinh độ phải từ -180 đến 180")
    @DecimalMax(value = "180.0", message = "Kinh độ phải từ -180 đến 180")
    @Schema(description = "Kinh độ GPS khi bật trạng thái hoạt động (bắt buộc nếu isActive=true)", example = "106.7900")
    private Double lng;

    @DecimalMin(value = "0.0", message = "Hướng phải từ 0 đến 360")
    @DecimalMax(value = "360.0", message = "Hướng phải từ 0 đến 360")
    @Schema(description = "Hướng di chuyển khi bật trạng thái (độ, 0-360)", example = "90.0")
    private Double heading;

    @DecimalMin(value = "0.0", message = "Tốc độ phải >= 0")
    @Schema(description = "Tốc độ di chuyển khi bật trạng thái (km/h)", example = "0.0")
    private Double speed;
}
