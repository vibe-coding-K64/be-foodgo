package com.example.be_foodgo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu cập nhật số lượng món trong giỏ hàng")
public class CartUpdateQuantityRequest {

    @NotBlank(message = "userId không được để trống")
    @Schema(description = "ID người dùng khách hàng", example = "user_001")
    private String userId;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    @Schema(description = "Số lượng món mới", example = "3")
    private Integer quantity;
}
