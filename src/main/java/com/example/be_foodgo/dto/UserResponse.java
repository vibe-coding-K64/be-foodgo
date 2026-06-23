package com.example.be_foodgo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thong tin nguoi dung tra ve sau khi dang nhap thanh cong")
public class UserResponse {

    @Schema(description = "ID tai khoan nguoi dung", example = "user_001")
    private String id;

    @Schema(description = "Dia chi email", example = "nguoidung@gmail.com")
    private String email;

    @Schema(description = "Ho va ten day du", example = "Nguyen Van A")
    private String fullName;

    @Schema(description = "So dien thoai di dong", example = "0123456789")
    private String phoneNumber;

    @Schema(description = "URL anh dai dien", example = "https://example.com/avatar/user001.jpg")
    private String photoUrl;

    @Schema(description = "Danh sach quyen: 1=Khach hang, 2=Tai xe, 3=Nguoi ban, 4=Admin")
    private List<Integer> roles;

    @Schema(description = "Email da duoc xac thuc chua", example = "false")
    private Boolean isEmailVerified;

    @Schema(description = "Trang thai hoat dong cua tai khoan", example = "true")
    private Boolean isActive;
}
