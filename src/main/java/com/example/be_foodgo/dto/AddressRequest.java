package com.example.be_foodgo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yeu cau them hoac cap nhat dia chi giao hang")
public class AddressRequest {

    @NotBlank(message = "userId khong duoc de trong")
    @Schema(description = "ID nguoi dung khach hang", example = "user_001")
    private String userId;

    @NotBlank(message = "Ten dia chi (nhan) khong duoc de trong")
    @Schema(description = "Nhan dia chi (VD: 'Nha rieng', 'Cong ty')", example = "Nha rieng")
    private String name;

    @NotBlank(message = "Dia chi chi tiet khong duoc de trong")
    @Schema(description = "Dia chi chi tiet day du", example = "Ky tuc xa UTC2, Quan 9, TP.HCM")
    private String address;

    @NotBlank(message = "Ho ten nguoi nhan khong duoc de trong")
    @Schema(description = "Ho ten nguoi nhan hang", example = "Khoi")
    private String receiverName;

    @NotBlank(message = "So dien thoai nguoi nhan khong duoc de trong")
    @Pattern(regexp = "^0[0-9]{9,10}$", message = "So dien thoai khong dung dinh dang (bat dau bang 0, 10-11 chu so)")
    @Schema(description = "So dien thoai nguoi nhan (bat dau bang 0, 10-11 chu so)", example = "0123456789")
    private String receiverPhone;

    @NotNull(message = "Toa do vi do (lat) khong duoc de trong")
    @Schema(description = "Toa do vi do (latitude)", example = "10.8455")
    private Double lat;

    @NotNull(message = "Toa do kin do (lng) khong duoc de trong")
    @Schema(description = "Toa do kin do (longitude)", example = "106.7939")
    private Double lng;

    @Schema(description = "Co phai dia chi mac dinh khong. Neu true, cac dia chi cu se bi bo mac dinh.", example = "false")
    private Boolean isDefault;
}
