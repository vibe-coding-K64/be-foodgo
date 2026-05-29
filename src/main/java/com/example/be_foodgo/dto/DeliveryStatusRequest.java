package com.example.be_foodgo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
}
