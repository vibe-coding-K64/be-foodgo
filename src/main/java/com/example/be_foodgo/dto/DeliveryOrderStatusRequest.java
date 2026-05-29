package com.example.be_foodgo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yeu cau cap nhat trang thai don hang")
public class DeliveryOrderStatusRequest {

    @NotNull(message = "Trang thai khong duoc de trong")
    @Min(value = 2, message = "Trang thai phai nam trong khoang 2-4")
    @Max(value = 4, message = "Trang thai phai nam trong khoang 2-4")
    @Schema(description = "Trang thai don hang: 2=Dang giao, 3=Hoan thanh, 4=Huy", example = "2")
    private Integer status;
}
