package com.example.be_foodgo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yeu cau cap nhat ho so khach hang (multipart/form-data)")
public class UpdateProfileMultipartRequest {

    @Schema(description = "File anh dai dien moi (JPEG, PNG, GIF, WEBP, toi da 5MB). Gui len neu muon doi anh.")
    private String avatar;

    @Size(max = 100, message = "Ho ten khong duoc vuot qua 100 ky tu")
    @Schema(description = "Ho va ten day du moi. Gui len neu muon doi ten.", example = "Nguyen Van A")
    private String fullName;

    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
            message = "Email khong dung dinh dang")
    @Schema(description = "Email moi. Gui len neu muon doi email.", example = "nguoidung@gmail.com")
    private String email;

    @NotBlank(message = "Mat khau xac thuc khong duoc de trong")
    @Schema(description = "Mat khau hien tai de xac thuc hanh dong doi thong tin (bat buoc)", example = "Matkhau123")
    private String password;
}
