package com.example.be_foodgo.dto;

import com.example.be_foodgo.constant.DeliveryOrderStatus;
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
@Schema(description = "Chi tiet don hang giao hang")
public class DeliveryOrderDTO {

    @Schema(description = "ID don hang", example = "order_001")
    private String id;

    @Schema(description = "Ma hien thi ngan gon cua don hang", example = "FG12345")
    private String orderCode;

    @Schema(description = "ID nguoi dat hang", example = "user_001")
    private String userId;

    @Schema(description = "Ten khach hang dat don", example = "Tran Thi B")
    private String customerName;

    @Schema(description = "So dien thoai khach hang dat don", example = "0901234567")
    private String customerPhone;

    @Schema(description = "URL anh dai dien khach hang", example = "https://example.com/avatar.jpg")
    private String customerAvatarUrl;

    @Schema(description = "Ten nguoi nhan hang", example = "Tran Thi B")
    private String recipientName;

    @Schema(description = "So dien thoai nguoi nhan hang", example = "0901234567")
    private String recipientPhone;

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

    @Schema(description = "Tong tien mon va tuy chon truoc khi tru khuyen mai, chua gom phi giao hang (VND)", example = "100000.0")
    private Double totalAmount;

    @Schema(description = "Tong tien phan mon chinh, khong tinh option (VND)", example = "90000.0")
    private Double itemsSubtotal;

    @Schema(description = "Tong tien cac option/topping (VND)", example = "10000.0")
    private Double optionsSubtotal;

    @Schema(description = "Tong so tien giam gia ap vao don (VND)", example = "5000.0")
    private Double discountAmount;

    @Schema(description = "Phi giao hang (VND)", example = "15000.0")
    private Double deliveryFee;

    @Schema(description = "Tong tien khach can thanh toan sau khuyen mai va da gom phi giao hang (VND)", example = "110000.0")
    private Double finalAmount;

    @Schema(description = "So tien tai xe can thu/giu ho cho don nay (VND), phu thuoc phuong thuc thanh toan", example = "110000.0")
    private Double driverCollectAmount;

    @Schema(
            description = "Trang thai don hang. "
                    + DeliveryOrderStatus.PENDING_STORE_CONFIRMATION + " = " + "PENDING_STORE_CONFIRMATION (Chờ cửa hàng xác nhận), "
                    + DeliveryOrderStatus.WAITING_DRIVER + " = " + "WAITING_DRIVER (Chờ tài xế nhận), "
                    + DeliveryOrderStatus.DELIVERING + " = " + "DELIVERING (Đang giao), "
                    + DeliveryOrderStatus.COMPLETED + " = " + "COMPLETED (Hoàn thành), "
                    + DeliveryOrderStatus.CANCELLED + " = " + "CANCELLED (Đã hủy).",
            example = "2"
    )
    private Integer status;

    @Schema(description = "Ma enum string cua trang thai don hang", example = "DELIVERING")
    private String statusCode;

    @Schema(description = "Mo ta human-readable cua trang thai don hang", example = "Tài xế đã nhận đơn và đang trong quá trình giao.")
    private String statusDescription;

    @Schema(description = "Trang thai thanh toan (1 = Chua thanh toan, 2 = Da thanh toan)", example = "1")
    private Integer paymentStatus;

    @Schema(description = "Dia chi giao hang", example = "Ky tuc xa UTC2, Quan 9, TP.HCM")
    private String deliveryAddress;

    @Schema(description = "Vi do dia chi giao hang", example = "10.8455")
    private Double deliveryLat;

    @Schema(description = "Kinh do dia chi giao hang", example = "106.7939")
    private Double deliveryLng;

    @Schema(description = "Khoang cach tu quan den diem giao (km)", example = "3.5")
    private Double distance;

    @Schema(description = "Khoang cach tu tai xe den diem lay hang (km) neu backend tinh duoc", example = "1.2")
    private Double pickupDistanceKm;

    @Schema(description = "Khoang cach tu quan den diem giao (km), dong nghia ro rang hon cua distance", example = "3.5")
    private Double deliveryDistanceKm;

    @Schema(description = "Thoi gian uoc tinh hoan tat giao hang (phut) neu backend tinh duoc", example = "18")
    private Integer estimatedDurationMinutes;

    @Schema(description = "Phuong thuc thanh toan (1=Cash, 2=MoMo, 3=ZaloPay, 4=VNPay/Card)", example = "2")
    private int paymentMethod;

    @Schema(description = "ID tai xe nhan don", example = "user_001")
    private String driverId;

    @Schema(description = "Ten tai xe", example = "Le Van B")
    private String driverName;

    @Schema(description = "So dien thoai tai xe", example = "0912345678")
    private String driverPhone;

    @Schema(description = "Bien so xe", example = "59A-123.45")
    private String vehiclePlate;

    @Schema(description = "Thoi diem tai xe den diem lay hang, neu co")
    private Instant arrivedAtStoreAt;

    @Schema(description = "Thoi diem tai xe xac nhan da lay hang, neu co")
    private Instant pickedUpAt;

    @Schema(description = "Thoi diem giao hang thanh cong, neu co")
    private Instant deliveredAt;

    @Schema(description = "Buoc giao hang FE co the dung de render UI nhieu buoc", example = "ON_THE_WAY")
    private String deliveryStep;

    @Schema(description = "Thoi diem tao don")
    private Instant createdAt;

    @Schema(description = "Thoi diem cap nhat gan nhat")
    private Instant updatedAt;

    @Schema(description = "Ghi chu don hang", example = "Giao gap")
    private String note;

    @Schema(description = "ID request tam phuc vu popup realtime", example = "req_456")
    private String requestId;

    @Schema(description = "Thu nhap uoc tinh cua tai xe cho don nay", example = "15000.0")
    private Double estimatedEarning;

    @Schema(description = "Thoi diem het han popup nhan don realtime")
    private Instant expiresAt;

    @Schema(description = "So giay con lai den khi popup nhan don het han", example = "10")
    private Integer expiresInSeconds;

    @Schema(description = "Huong tu cua hang den diem giao (do), phuc vu UI/map", example = "120.0")
    private Double deliveryHeading;

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
