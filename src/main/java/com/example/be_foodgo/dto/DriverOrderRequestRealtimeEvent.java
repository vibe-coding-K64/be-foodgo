package com.example.be_foodgo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Su kien realtime don moi gui den tai xe")
public class DriverOrderRequestRealtimeEvent {

    @Schema(description = "Loai su kien", example = "ORDER_REQUEST")
    private String event;

    @Schema(description = "Thong diep hien thi", example = "Co don hang moi")
    private String message;

    @Schema(description = "ID don hang", example = "order_123")
    private String orderId;

    @Schema(description = "ID request tam cua tai xe", example = "req_456")
    private String requestId;

    @Schema(description = "Thu nhap uoc tinh cua tai xe cho don nay", example = "15000.0")
    private Double estimatedEarning;

    @Schema(description = "Thoi diem het han nhan don")
    private Instant expiresAt;

    @Schema(description = "So giay con lai den khi popup nhan don het han", example = "10")
    private Integer expiresInSeconds;

    @Schema(description = "Huong tu cua hang den diem giao (do), phuc vu UI/map", example = "120.0")
    private Double deliveryHeading;

    @Schema(description = "Thong tin chi tiet don hang phuc vu popup realtime")
    private DeliveryOrderDTO order;
}
