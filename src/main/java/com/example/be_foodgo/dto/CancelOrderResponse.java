package com.example.be_foodgo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Phan hoi ket qua huy don hang")
public class CancelOrderResponse {

    @Schema(description = "ID don hang", example = "order_001")
    private String id;

    @Schema(description = "Trang thai don hang (4 = Da huy)", example = "4")
    private int status;

    @Schema(description = "Thoi gian cap nhat huy don", example = "2026-05-31T18:41:00Z")
    private String updatedAt;

    public CancelOrderResponse() {}

    public CancelOrderResponse(String id, int status, String updatedAt) {
        this.id = id;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
