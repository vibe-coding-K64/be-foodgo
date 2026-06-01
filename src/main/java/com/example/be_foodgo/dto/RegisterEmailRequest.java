package com.example.be_foodgo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yeu cau dang ky tai khoan (buoc 1: gui OTP xac thuc email)")
public class RegisterEmailRequest {

    @NotBlank(message = "Email khong duoc de trong")
    @Email(message = "Email khong dung dinh dang")
    @Schema(description = "Email nguoi dung", example = "nguoidung@gmail.com")
    private String email;

    @NotBlank(message = "Mat khau khong duoc de trong")
    @Size(min = 6, message = "Mat khau phai it nhat 6 ky tu")
    @Schema(description = "Mat khau", example = "Matkhau123@")
    private String password;

    @NotBlank(message = "Ho ten khong duoc de trong")
    @Schema(description = "Ho va ten nguoi dung", example = "Nguyen Van A")
    private String fullName;

    @NotBlank(message = "So dien thoai khong duoc de trong")
    @Pattern(regexp = "^0\\d{9,10}$", message = "So dien thoai khong dung dinh dang (VD: 0912345678)")
    @Schema(description = "So dien thoai", example = "0912345678")
    private String phoneNumber;
}
