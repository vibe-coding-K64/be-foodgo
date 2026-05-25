package com.example.be_foodgo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yeu cau cap nhat thong tin ho so khach hang")
public class UpdateProfileRequest {

    @Size(max = 100, message = "Ho ten khong duoc vuot qua 100 ky tu")
    @Schema(description = "Ho va ten day du cua nguoi dung", example = "Nguyen Van A")
    private String fullName;

    @Schema(description = "Duong dan URL anh dai dien cua nguoi dung", example = "https://example.com/avatar/user001.jpg")
    private String avatarUrl;
}
