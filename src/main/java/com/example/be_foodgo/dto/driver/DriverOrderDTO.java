package com.example.be_foodgo.dto.driver;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Chi tiet don hang tai xe nhan/xem")
public class DriverOrderDTO {

    @Schema(description = "ID don hang", example = "order_001")
    private String id;

    @Schema(description = "ID nguoi dat hang", example = "user_001")
    private String userId;

    @Schema(description = "ID quan", example = "store_001")
    private String storeId;

    @Schema(description = "Ten quan", example = "Com tam Phuc Loc Tho")
    private String storeName;

    @Schema(description = "Dia chi quan", example = "123 Le Van Viet, TP. Thu Duc")
    private String storeAddress;

    @Schema(description = "Vi do quan", example = "10.8500")
    private Double storeLat;

    @Schema(description = "Kinh do quan", example = "106.7900")
    private Double storeLng;

    @Schema(description = "Danh sach mon an trong don")
    private List<OrderItemData> items;

    @Schema(description = "Tong tien don hang (VND)", example = "140000.0")
    private Double totalAmount;

    @Schema(description = "Phi giao hang (VND)", example = "15000.0")
    private Double deliveryFee;

    @Schema(description = "Trang thai don hang (0-4)", example = "2")
    private Integer status;

    @Schema(description = "Dia chi giao hang", example = "Ky tuc xa UTC2, Quan 9, TP.HCM")
    private String deliveryAddress;

    @Schema(description = "Vi do dia chi giao hang", example = "10.8455")
    private Double deliveryLat;

    @Schema(description = "Kinh do dia chi giao hang", example = "106.7939")
    private Double deliveryLng;

    @Schema(description = "Phuong thuc thanh toan (cash, momo, zalo, card)", example = "momo")
    private String paymentMethod;

    @Schema(description = "ID tai xe nhan don", example = "user_001")
    private String driverId;

    @Schema(description = "Ten tai xe", example = "Le Van B")
    private String driverName;

    @Schema(description = "So dien thoai tai xe", example = "0912345678")
    private String driverPhone;

    @Schema(description = "Bien so xe", example = "59A-123.45")
    private String vehiclePlate;

    @Schema(description = "Thoi diem tao don")
    private Instant createdAt;

    @Schema(description = "Thoi diem cap nhat gan nhat")
    private Instant updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Thong tin mot mon an trong don hang")
    public static class OrderItemData {

        @Schema(description = "ID mon an", example = "prod_001")
        private String foodId;

        @Schema(description = "Ten mon an", example = "Com tam suon bi cha")
        private String name;

        @Schema(description = "Don gia (VND)", example = "45000.0")
        private Double price;

        @Schema(description = "So luong", example = "2")
        private Integer quantity;

        @Schema(description = "URL anh mon an")
        private String imageUrl;

        @Schema(description = "Cac tuy chon da chon (size, topping)")
        private List<OptionData> options;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Tuy chon cua mon an (size, topping)")
    public static class OptionData {

        @Schema(description = "Ten tuy chon", example = "Tran chau")
        private String name;

        @Schema(description = "Gia cua tuy chon (VND)", example = "5000.0")
        private Double price;
    }
}
