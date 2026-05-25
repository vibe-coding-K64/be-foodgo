package com.example.be_foodgo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yeu cau gui ma OTP")
public class OtpSendRequest {

    @NotBlank(message = "Email hoac so dien thoai khong duoc de trong")
    @Schema(description = "Email hoac so dien thoai can khoi phuc mat khau", example = "nguoidung@gmail.com")
    private String emailOrPhone;
}
