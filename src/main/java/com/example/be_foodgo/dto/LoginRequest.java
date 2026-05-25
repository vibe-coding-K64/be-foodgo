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
@Schema(description = "Yeu cau dang nhap")
public class LoginRequest {

    @NotBlank(message = "Email khong duoc de trong")
    @Email(message = "Email khong dung dinh dang")
    @Schema(description = "Dia chi email cua tai khoan", example = "nguoidung@gmail.com")
    private String email;

    @NotBlank(message = "Mat khau khong duoc de trong")
    @Schema(description = "Mat khau dang nhap", example = "password123")
    private String password;
}
