package com.example.be_foodgo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Yeu cau xac thuc email bang ma OTP")
public class VerifyEmailOtpRequest {

    @NotBlank(message = "Email khong duoc de trong")
    @Schema(description = "Email can xac thuc", example = "nguoidung@gmail.com")
    private String email;

    @NotBlank(message = "Ma OTP khong duoc de trong")
    @Schema(description = "Ma OTP 6 chu so", example = "123456")
    private String otpCode;
}
