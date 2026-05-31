package com.example.be_foodgo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Yeu cau gui ma OTP xac thuc email")
public class VerifyEmailRequest {

    @NotBlank(message = "Email khong duoc de trong")
    @Email(message = "Email khong dung dinh dang")
    @Schema(description = "Email can xac thuc", example = "nguoidung@gmail.com")
    private String email;
}
