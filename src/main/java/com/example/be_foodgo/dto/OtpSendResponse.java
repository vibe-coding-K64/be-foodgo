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
@Schema(description = "Phan hoi gui ma OTP thanh cong")
public class OtpSendResponse {

    @Schema(description = "Email hoac so dien thoai nhan ma OTP", example = "nguoidung@gmail.com")
    private String emailOrPhone;

    @Schema(description = "Thong bao ket qua", example = "Ma OTP da duoc gui. Vui long kiem tra email/so dien thoai.")
    private String message;

    @Schema(description = "Ma OTP (chi hien thi trong moi truong dev/demo)")
    private String otpCode;

    @Schema(description = "Thoi gian het han cua OTP (giay)", example = "300")
    private Integer expiresInSeconds;
}
