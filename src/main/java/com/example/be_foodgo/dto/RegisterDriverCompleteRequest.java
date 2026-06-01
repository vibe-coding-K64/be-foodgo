package com.example.be_foodgo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yeu cau hoan tat dang ky tai xe (buoc 2)")
public class RegisterDriverCompleteRequest {

    @NotBlank(message = "Email khong duoc de trong")
    @Email(message = "Email khong dung dinh dang")
    @Schema(description = "Email da gui OTP", example = "taixe@gmail.com")
    private String email;

    @NotBlank(message = "Ma OTP khong duoc de trong")
    @Schema(description = "Ma OTP 6 chu so", example = "123456")
    private String otpCode;
}
