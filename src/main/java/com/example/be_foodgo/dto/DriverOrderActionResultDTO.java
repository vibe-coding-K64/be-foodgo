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
@Schema(description = "Ket qua toi thieu cua mot hanh dong xu ly don hang phia tai xe")
public class DriverOrderActionResultDTO {

    @Schema(description = "ID don hang", example = "order_123")
    private String orderId;

    @Schema(description = "ID request tam phuc vu popup realtime", example = "req_456")
    private String requestId;

    @Schema(description = "Trang thai xu ly hanh dong", example = "DECLINED")
    private String status;
}
