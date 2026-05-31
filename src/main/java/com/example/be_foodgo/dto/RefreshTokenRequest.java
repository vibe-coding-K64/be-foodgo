package com.example.be_foodgo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yeu cau lam moi access token bang refresh token")
public class RefreshTokenRequest {

    @NotBlank(message = "refreshToken khong duoc de trong")
    @Schema(description = "Refresh token nhan duoc luc dang nhap/dang ky", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;
}
