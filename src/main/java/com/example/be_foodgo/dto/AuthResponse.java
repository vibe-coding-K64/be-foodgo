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
@Schema(description = "Phan hoi dang nhap thanh cong, chua token JWT")
public class AuthResponse {

    @Schema(description = "Token JWT truy cap", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyXzAwMSJ9...")
    private String token;

    @Schema(description = "Loai token (luon la Bearer)")
    private String tokenType;

    @Schema(description = "Thoi gian het han cua token (miliseconds)", example = "86400000")
    private Long expiresIn;

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
}
