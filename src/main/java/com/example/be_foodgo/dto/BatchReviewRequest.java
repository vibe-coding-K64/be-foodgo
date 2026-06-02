package com.example.be_foodgo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class BatchReviewRequest {

    @NotBlank(message = "ID đơn hàng không được để trống")
    private String orderId;

    @NotBlank(message = "ID cửa hàng không được để trống")
    private String storeId;

    @NotBlank(message = "ID người dùng không được để trống")
    private String userId;

    @NotBlank(message = "Tên người dùng không được để trống")
    private String userName;

    private String userAvatarUrl;

    @NotNull(message = "Danh sách đánh giá không được để trống")
    @NotEmpty(message = "Danh sách đánh giá không được để trống")
    @Size(max = 20, message = "Tối đa 20 món đánh giá mỗi request")
    @Valid
    private List<BatchReviewItem> items;

    @Data
    public static class BatchReviewItem {

        @NotBlank(message = "ID sản phẩm không được để trống")
        private String productId;

        @NotNull(message = "Số sao đánh giá không được để trống")
        @Min(value = 1, message = "starRating phải từ 1 đến 5")
        @Max(value = 5, message = "starRating phải từ 1 đến 5")
        private Integer starRating;

        private String comment;

        private List<String> imageUrls;
    }
}
