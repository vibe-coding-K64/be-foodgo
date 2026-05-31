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
@Schema(description = "Phan hoi lam moi access token thanh cong")
public class RefreshTokenResponse {

    @Schema(description = "Access token moi", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "Loai token (luon la Bearer)")
    private String tokenType;

    @Schema(description = "Thoi gian het han cua access token moi (miliseconds)", example = "10800000")
    private Long expiresIn;

    @Schema(description = "Refresh token moi (chi tra ve khi refreshToken cu het han)")
    private String refreshToken;

    @Schema(description = "Thoi gian het han cua refresh token (miliseconds)", example = "2592000000")
    private Long refreshExpiresIn;

    public static RefreshTokenResponse of(String token, Long expiresIn) {
        return RefreshTokenResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .build();
    }

    public static RefreshTokenResponse of(String token, Long expiresIn, String refreshToken, Long refreshExpiresIn) {
        return RefreshTokenResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .refreshToken(refreshToken)
                .refreshExpiresIn(refreshExpiresIn)
                .build();
    }
}
