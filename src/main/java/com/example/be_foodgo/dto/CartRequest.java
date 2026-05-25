package com.example.be_foodgo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "userId không được để trống")
    @Schema(description = "ID người dùng khách hàng", example = "user_001")
    private String userId;

    @NotBlank(message = "storeId không được để trống")
    @Schema(description = "ID cửa hàng chứa món ăn", example = "store_001")
    private String storeId;

    @NotBlank(message = "foodId không được để trống")
    @Schema(description = "ID sản phẩm (món ăn) cần thêm vào giỏ", example = "prod_001")
    private String foodId;

    @Schema(description = "Kích thước đã chọn (VD: M, L). Có thể là null nếu sản phẩm không có tùy chọn size.", example = "M")
    private String size;

    @Schema(description = "Danh sách topping đã chọn", example = "[{\"name\":\"Trân châu\",\"price\":5000.0}]")
    private List<ToppingOption> toppings;

    @Schema(description = "Ghi chú cho cửa hàng về món này", example = "Không thêm hành")
    private String note;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    @Schema(description = "Số lượng món thêm vào giỏ", example = "2")
    private Integer quantity;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Topping đã chọn kèm theo giá")
    public static class ToppingOption {

        @Schema(description = "Tên topping", example = "Trân châu")
        private String name;

        @Schema(description = "Giá của topping (VND)", example = "5000.0")
        private Double price;
    }
}
