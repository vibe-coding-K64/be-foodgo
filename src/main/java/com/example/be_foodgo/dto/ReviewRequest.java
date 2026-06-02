package com.example.be_foodgo.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class ReviewRequest {

    @NotBlank(message = "ID đơn hàng không được để trống")
    private String orderId;

    @NotBlank(message = "ID item không được để trống")
    private String itemId;

    @NotBlank(message = "ID sản phẩm không được để trống")
    private String foodId;

    @NotBlank(message = "ID cửa hàng không được để trống")
    private String storeId;

    @NotBlank(message = "ID người dùng không được để trống")
    private String userId;

    @NotBlank(message = "Tên người dùng không được để trống")
    private String userName;

    private String userAvatarUrl;

    @NotNull(message = "Số sao đánh giá không được để trống")
    @Min(value = 1, message = "Số sao đánh giá phải từ 1 đến 5")
    @Max(value = 5, message = "Số sao đánh giá phải từ 1 đến 5")
    private Integer starRating;

    @NotBlank(message = "Nội dung bình luận không được để trống")
    private String comment;

    private List<String> imageUrls;
}
