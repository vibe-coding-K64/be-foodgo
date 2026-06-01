package com.example.be_foodgo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu thêm món vào giỏ hàng từ phía Flutter")
public class CartRequest {

    @Schema(description = "ID của cart item (null khi thêm mới, có giá trị khi cập nhật)", example = "cart_item_001")
    private String id;

    @NotBlank(message = "userId không được để trống")
    @Schema(description = "ID người dùng khách hàng", example = "user_001")
    private String userId;

    @NotBlank(message = "storeId không được để trống")
    @Schema(description = "ID cửa hàng chứa món ăn", example = "store_001")
    private String storeId;

    @NotBlank(message = "foodId không được để trống")
    @Schema(description = "ID sản phẩm (món ăn) cần thêm vào giỏ", example = "prod_001")
    private String foodId;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    @Schema(description = "Số lượng món thêm vào giỏ", example = "2")
    private Integer quantity;

    @Schema(description = "Danh sách các nhóm tùy chọn đã chọn (VD: Kich thuoc, Topping)")
    private List<SelectedOptionGroup> selectedOptions;

    @Schema(description = "Ghi chú cho cửa hàng về món này", example = "Ít đường")
    private String note;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Nhóm tùy chọn đã chọn (VD: Kich thuoc, Topping)")
    public static class SelectedOptionGroup {

        @NotBlank(message = "Tên nhóm tùy chọn không được để trống")
        @Schema(description = "Tên nhóm tùy chọn", example = "Kich thuoc")
        private String name;

        @NotEmpty(message = "Danh sách tùy chọn không được để trống")
        @Schema(description = "Danh sách các tùy chọn đã chọn trong nhóm")
        private List<SelectedOption> options;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Tùy chọn đã chọn trong một nhóm")
    public static class SelectedOption {

        @NotBlank(message = "Tên tùy chọn không được để trống")
        @Schema(description = "Tên tùy chọn", example = "Lon")
        private String name;
    }
}
