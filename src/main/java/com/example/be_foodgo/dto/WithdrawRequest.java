package com.example.be_foodgo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yeu cau rut tien")
public class WithdrawRequest {

    @NotNull(message = "So tien rut khong duoc de trong")
    @Positive(message = "So tien rut phai lon hon 0")
    @Schema(description = "So tien muon rut (VND)", example = "500000.0")
    private Double amount;
}
