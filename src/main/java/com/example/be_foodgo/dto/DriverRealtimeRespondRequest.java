package com.example.be_foodgo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload realtime tai xe tra loi yeu cau nhan don")
public class DriverRealtimeRespondRequest {

    @NotBlank(message = "orderId khong duoc de trong")
    @Schema(description = "ID don hang", example = "order_123")
    private String orderId;

    @NotBlank(message = "requestId khong duoc de trong")
    @Schema(description = "ID document request tam trong order_requests/{driverId}/requests/{requestId}", example = "abc123request")
    private String requestId;
}
