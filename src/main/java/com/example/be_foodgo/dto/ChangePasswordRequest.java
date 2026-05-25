package com.example.be_foodgo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yeu cau doi mat khau chu dong")
public class ChangePasswordRequest {

    @NotBlank(message = "Mat khau cu khong duoc de trong")
    @Schema(description = "Mat khau cu cua nguoi dung", example = "matkhaucu123")
    private String oldPassword;

    @NotBlank(message = "Mat khau moi khong duoc de trong")
    @Size(min = 6, message = "Mat khau moi phai co it nhat 6 ky tu")
    @Schema(description = "Mat khau moi", example = "matkhaumoi123")
    private String newPassword;
}
