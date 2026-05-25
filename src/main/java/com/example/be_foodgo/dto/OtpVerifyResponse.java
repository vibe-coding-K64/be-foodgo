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
@Schema(description = "Phan hoi xac thuc OTP thanh cong, chua token tam thoi")
public class OtpVerifyResponse {

    @Schema(description = "Token tam thoi dung de dat lai mat khau", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyXzAwMSIsInR5cGUiOiJUTVBfVE9LRU4ifQ...")
    private String tempToken;

    @Schema(description = "Loai token (luon la Bearer)")
    private String tokenType;

    @Schema(description = "Thoi gian het han cua token tam thoi (miliseconds)", example = "300000")
    private Long expiresIn;

    @Schema(description = "Thoi gian het han (timestamp ISO 8601)", example = "2026-05-26T13:05:00Z")
    private String expiresAt;

    public static OtpVerifyResponse of(String tempToken, Long expiresIn) {
        return OtpVerifyResponse.builder()
                .tempToken(tempToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .expiresAt(java.time.Instant.now().plusMillis(expiresIn).toString())
                .build();
    }
}
