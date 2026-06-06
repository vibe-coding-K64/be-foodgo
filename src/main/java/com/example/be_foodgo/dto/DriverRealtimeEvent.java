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
@Schema(description = "Su kien realtime tra ve cho tai xe")
public class DriverRealtimeEvent {

    @Schema(description = "Loai su kien", example = "ORDER_ACCEPTED")
    private String event;

    @Schema(description = "Thong diep hien thi", example = "Nhan don hang thanh cong")
    private String message;

    @Schema(description = "ID don hang", example = "order_123")
    private String orderId;

    @Schema(description = "ID request tam phuc vu popup realtime", example = "req_456")
    private String requestId;

    @Schema(description = "Trang thai xu ly", example = "SUCCESS")
    private String status;

    @Schema(description = "Du lieu don hang sau khi xu ly")
    private DeliveryOrderDTO order;

    @Schema(description = "Ket qua toi thieu cua hanh dong trong cac truong hop khong can tra full order")
    private DriverOrderActionResultDTO actionResult;
}
