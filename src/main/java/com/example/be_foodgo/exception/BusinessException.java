package com.example.be_foodgo.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public BusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.errorCode = "BUSINESS_ERROR";
    }

    public BusinessException(HttpStatus status, String errorCode, String message) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public static BusinessException monAnHetHang(String foodId) {
        return new BusinessException(
                HttpStatus.BAD_REQUEST,
                "OUT_OF_STOCK",
                "Món ăn với ID [" + foodId + "] đã hết hàng, vui lòng chọn món khác."
        );
    }

    public static BusinessException cuaHangKhongKhop(String existingStoreId, String newStoreId) {
        return new BusinessException(
                HttpStatus.BAD_REQUEST,
                "STORE_MISMATCH",
                "Giỏ hàng hiện tại thuộc cửa hàng [" + existingStoreId
                        + "]. Món mới thuộc cửa hàng [" + newStoreId
                        + "]. Vui lòng xác nhận xóa giỏ hàng cũ để tiếp tục."
        );
    }

    public static BusinessException sanPhamKhongTimThay(String foodId) {
        return new BusinessException(
                HttpStatus.NOT_FOUND,
                "PRODUCT_NOT_FOUND",
                "Không tìm thấy sản phẩm với ID [" + foodId + "]."
        );
    }

    public static BusinessException cartItemKhongTimThay(String itemId) {
        return new BusinessException(
                HttpStatus.NOT_FOUND,
                "CART_ITEM_NOT_FOUND",
                "Không tìm thấy món với ID [" + itemId + "] trong giỏ hàng."
        );
    }

    public static BusinessException loiHeThong(String message) {
        return new BusinessException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "SYSTEM_ERROR",
                "Lỗi hệ thống: " + message
        );
    }

    public static BusinessException diaChiKhongTimThay(String addressId) {
        return new BusinessException(
                HttpStatus.NOT_FOUND,
                "ADDRESS_NOT_FOUND",
                "Không tìm thấy địa chỉ với ID [" + addressId + "]."
        );
    }

    public static BusinessException gioHangRong() {
        return new BusinessException(
                HttpStatus.BAD_REQUEST,
                "CART_EMPTY",
                "Giỏ hàng hiện tại đang rỗng, vui lòng thêm món trước khi đặt hàng."
        );
    }

    public static BusinessException khoangCachVuotGioiHan(double khoangCach, double gioiHanKm) {
        return new BusinessException(
                HttpStatus.BAD_REQUEST,
                "DISTANCE_EXCEEDED",
                String.format("Khoảng cách từ cửa hàng đến địa chỉ giao hàng là %.1f km, vượt quá giới hạn %.1f km. Vui lòng chọn địa chỉ gần hơn.", khoangCach, gioiHanKm)
        );
    }

    public static BusinessException monAnHetHangTrongGio(String foodId) {
        return new BusinessException(
                HttpStatus.BAD_REQUEST,
                "ITEM_OUT_OF_STOCK",
                "Món ăn với ID [" + foodId + "] trong giỏ hàng đã hết hàng, vui lòng xóa khỏi giỏ hàng hoặc chọn món khác."
        );
    }

    public static BusinessException voucherDaHetHan(String voucherId) {
        return new BusinessException(
                HttpStatus.BAD_REQUEST,
                "VOUCHER_EXPIRED",
                "Voucher với ID [" + voucherId + "] đã hết hạn."
        );
    }

    public static BusinessException voucherDaHetSoLuong(String voucherId) {
        return new BusinessException(
                HttpStatus.BAD_REQUEST,
                "VOUCHER_EXHAUSTED",
                "Voucher với ID [" + voucherId + "] đã hết số lượng sử dụng."
        );
    }

    public static BusinessException voucherKhongDatDonToiThieu(String voucherId, double minOrderValue) {
        return new BusinessException(
                HttpStatus.BAD_REQUEST,
                "VOUCHER_MIN_ORDER_NOT_MET",
                String.format("Đơn hàng phải có giá trị tối thiểu %.0f VND để sử dụng voucher [%s].", minOrderValue, voucherId)
        );
    }

    public static BusinessException voucherKhongTimThay(String voucherId) {
        return new BusinessException(
                HttpStatus.NOT_FOUND,
                "VOUCHER_NOT_FOUND",
                "Không tìm thấy voucher với ID [" + voucherId + "]."
        );
    }
}
