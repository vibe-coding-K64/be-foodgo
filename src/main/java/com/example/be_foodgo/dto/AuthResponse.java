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
@Schema(description = "Phan hoi dang nhap thanh cong, chua token JWT va refresh token")
public class AuthResponse {

    @Schema(description = "Token JWT truy cap", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyXzAwMSJ9...")
    private String token;

    @Schema(description = "Loai token (luon la Bearer)")
    private String tokenType;

    @Schema(description = "Thoi gian het han cua access token (miliseconds)", example = "10800000")
    private Long expiresIn;

    @Schema(description = "Refresh token de lam moi access token", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;

    @Schema(description = "Thoi gian het han cua refresh token (miliseconds)", example = "2592000000")
    private Long refreshExpiresIn;

    @Schema(description = "Thong tin nguoi dung")
    private UserResponse user;

    public static AuthResponse of(String token, Long expiresIn, UserResponse user) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .user(user)
                .build();
    }

    public static AuthResponse of(String token, Long expiresIn, String refreshToken, Long refreshExpiresIn, UserResponse user) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .refreshToken(refreshToken)
                .refreshExpiresIn(refreshExpiresIn)
                .user(user)
                .build();
    }
}
