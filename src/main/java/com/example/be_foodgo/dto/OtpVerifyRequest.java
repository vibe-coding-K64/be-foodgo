package com.example.be_foodgo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yeu cau xac thuc ma OTP")
public class OtpVerifyRequest {

    @NotBlank(message = "Email hoac so dien thoai khong duoc de trong")
    @Schema(description = "Email hoac so dien thoai da nhan ma OTP", example = "nguoidung@gmail.com")
    private String emailOrPhone;

    @NotBlank(message = "Ma OTP khong duoc de trong")
    @Schema(description = "Ma OTP 6 chu so", example = "123456")
    private String otpCode;
}
