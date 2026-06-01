package com.example.be_foodgo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Yeu cau huy don hang")
public class CancelOrderRequest {

    @Schema(description = "Ly do huy don hang", example = "Doi y", maxLength = 500)
    @Size(max = 500, message = "Lý do huỷ không được vượt quá 500 ký tự")
    private String reason;

    public CancelOrderRequest() {}

    public CancelOrderRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
