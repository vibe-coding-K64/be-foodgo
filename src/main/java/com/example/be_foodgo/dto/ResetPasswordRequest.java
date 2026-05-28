package com.example.be_foodgo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yeu cau dat lai mat khau")
public class ResetPasswordRequest {

    @NotBlank(message = "Token tam thoi khong duoc de trong")
    @Schema(description = "Token tam thoi nhan duoc sau khi xac thuc OTP", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String tempToken;

    @NotBlank(message = "Mat khau moi khong duoc de trong")
    @Schema(description = "Mat khau moi (it nhat 6 ky tu)", example = "newpassword123")
    private String newPassword;
}
