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
}
