package com.example.be_foodgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {
    private String id;
    private String storeId;

    @NotBlank(message = "Tên danh mục không được để trống")
    @jakarta.validation.constraints.Size(min = 2, max = 50, message = "Tên danh mục phải từ 2 đến 50 ký tự")
    private String name;

    @NotBlank(message = "Mã icon không được để trống")
    private String icon;

    @NotNull(message = "Thứ tự không được để trống")
    @jakarta.validation.constraints.Min(value = 1, message = "Thứ tự ưu tiên phải lớn hơn hoặc bằng 1")
    private Integer order;

    @NotBlank(message = "Link ảnh không được để trống")
    @jakarta.validation.constraints.Pattern(regexp = "^(http|https)://.*", message = "Link ảnh phải bắt đầu bằng http hoặc https")
    private String imageUrl;
    
    // Convert từ Timestamp của Firestore sang Date cho FE dễ dùng
    private Date createdAt;
    private Date updatedAt;
}
