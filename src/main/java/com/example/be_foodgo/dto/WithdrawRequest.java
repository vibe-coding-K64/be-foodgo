package com.example.be_foodgo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yeu cau rut tien")
public class WithdrawRequest {

    @NotNull(message = "So tien rut khong duoc de trong")
    @Positive(message = "So tien rut phai lon hon 0")
    @Schema(description = "So tien muon rut (VND)", example = "500000.0")
    private Double amount;

    @NotNull(message = "Tên ngân hàng không được để trống")
    @Schema(description = "Ten ngan hang", example = "Vietcombank")
    private String bankName;

    @NotNull(message = "Số tài khoản không được để trống")
    @Pattern(regexp = "^[0-9]+$", message = "Số tài khoản chỉ được chứa chữ số")
    @Size(min = 6, max = 20, message = "Số tài khoản phải từ 6 đến 20 số")
    @Schema(description = "So tai khoan", example = "012345678901")
    private String bankAccountNumber;

    @NotNull(message = "Tên chủ tài khoản không được để trống")
    @Pattern(regexp = "^[a-zA-ZÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠàáâãèéêìíòóôõùúăđĩũơƯĂẠẢẤẦẨẪẬẮẰẲẴẶẸẺẼỀỀỂưăạảấầẩẫậắằẳẵặẹẻẽềềểếỄỆỈỊỌỎỐỒỔỖỘỚỜỞỠỢỤỦỨỪễệỉịọỏốồổỗộớờởỡợụủứừỮỰỲỴÝỶỸửữựỳỵỷỹ\\s]+$", message = "Tên chủ thẻ chỉ được chứa chữ cái và khoảng trắng")
    @Schema(description = "Ten nguoi thu huong", example = "NGUYEN VAN A")
    private String bankAccountName;
}
