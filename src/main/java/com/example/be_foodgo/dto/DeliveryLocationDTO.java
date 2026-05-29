package com.example.be_foodgo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Vi tri GPS cua tai xe (Realtime Database)")
public class DeliveryLocationDTO {

    @Schema(description = "ID tai xe (trung voi userId)", example = "user_001")
    private String driverId;

    @Schema(description = "Vi do (latitude)", example = "10.8500")
    private Double lat;

    @Schema(description = "Kinh do (longitude)", example = "106.7900")
    private Double lng;

    @Schema(description = "Huong di chuyen (do, 0-360)", example = "90.0")
    private Double heading;

    @Schema(description = "Toc do di chuyen (km/h)", example = "30.0")
    private Double speed;

    @Schema(description = "Timestamp cap nhat cuoi (epoch ms)", example = "1712448000000")
    private Long updatedAt;

    @Schema(description = "Tai xe con online khong", example = "true")
    private Boolean isActive;
}
