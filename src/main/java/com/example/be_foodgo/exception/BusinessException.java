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

    public static BusinessException donHangKhongTimThay(String orderId) {
        return new BusinessException(
                HttpStatus.NOT_FOUND,
                "ORDER_NOT_FOUND",
                "Không tìm thấy đơn hàng với ID [" + orderId + "]."
        );
    }

    public static BusinessException khongPhaiChuDonHang(String orderId) {
        return new BusinessException(
                HttpStatus.FORBIDDEN,
                "FORBIDDEN",
                "Bạn không có quyền hủy đơn hàng [" + orderId + "]. Chỉ chủ nhân của đơn hàng mới được phép hủy."
        );
    }

    public static BusinessException trangThaiKhongTheHuy(String orderId, int status) {
        String tenTrangThai;
        if (status == 0) {
            tenTrangThai = "Chờ xác nhận";
        } else if (status == 1) {
            tenTrangThai = "Đang chuẩn bị";
        } else if (status == 2) {
            tenTrangThai = "Đang giao";
        } else if (status == 3) {
            tenTrangThai = "Hoàn thành";
        } else if (status == 4) {
            tenTrangThai = "Đã hủy";
        } else {
            tenTrangThai = "Không xác định";
        }
        return new BusinessException(
                HttpStatus.BAD_REQUEST,
                "ORDER_STATUS_CANNOT_CANCEL",
                "Không thể hủy đơn hàng [" + orderId + "] vì đơn đang ở trạng thái [" + tenTrangThai + "]. Chỉ có thể hủy đơn hàng đang ở trạng thái [Chờ xác nhận]."
        );
    }

    public static BusinessException trangThaiDonHangKhongChoPhepDanhGia(String orderId, int status) {
        String tenTrangThai;
        if (status == 0) {
            tenTrangThai = "Chờ xác nhận";
        } else if (status == 1) {
            tenTrangThai = "Đang chuẩn bị";
        } else if (status == 2) {
            tenTrangThai = "Đang giao";
        } else if (status == 3) {
            tenTrangThai = "Hoàn thành";
        } else if (status == 4) {
            tenTrangThai = "Đã hủy";
        } else {
            tenTrangThai = "Không xác định";
        }
        return new BusinessException(
                HttpStatus.BAD_REQUEST,
                "ORDER_STATUS_CANNOT_REVIEW",
                "Không thể đánh giá đơn hàng [" + orderId + "] vì đơn đang ở trạng thái [" + tenTrangThai + "]. Chỉ có thể đánh giá đơn hàng đang ở trạng thái [Hoàn thành] (status = 3)."
        );
    }

    public static BusinessException donHangDaDuocDanhGia(String orderId) {
        return new BusinessException(
                HttpStatus.BAD_REQUEST,
                "ORDER_ALREADY_REVIEWED",
                "Đơn hàng [" + orderId + "] đã được đánh giá trước đó. Mỗi đơn hàng chỉ được phép đánh giá một lần."
        );
    }

    public static BusinessException phuongThucThanhToanKhongTimThay(String paymentMethodId) {
        return new BusinessException(
                HttpStatus.NOT_FOUND,
                "PAYMENT_METHOD_NOT_FOUND",
                "Không tìm thấy phương thức thanh toán với ID [" + paymentMethodId + "]."
        );
    }

    public static BusinessException diemKhongDu(int diemHienTai, int diemCan) {
        return new BusinessException(
                HttpStatus.BAD_REQUEST,
                "NOT_ENOUGH_POINTS",
                String.format("Diem hien tai cua ban la %d, can it nhat %d diem de doi voucher nay.", diemHienTai, diemCan)
        );
    }

    public static BusinessException voucherDaDuocDoi(String voucherId) {
        return new BusinessException(
                HttpStatus.BAD_REQUEST,
                "VOUCHER_ALREADY_EXCHANGED",
                "Ban da doi voucher [" + voucherId + "] roi."
        );
    }

    public static BusinessException voucherInactive(String voucherId) {
        return new BusinessException(
                HttpStatus.BAD_REQUEST,
                "VOUCHER_INACTIVE",
                "Voucher [" + voucherId + "] khong con kich hoat."
        );
    }

    public static BusinessException diemKhongTimThay() {
        return new BusinessException(
                HttpStatus.BAD_REQUEST,
                "POINTS_NOT_FOUND",
                "Khong tim thay diem cua tai khoan nay."
        );
    }

    public static BusinessException thongBaoKhongTimThay(String notifId) {
        return new BusinessException(
                HttpStatus.NOT_FOUND,
                "NOTIFICATION_NOT_FOUND",
                "Không tìm thấy thông báo với ID [" + notifId + "]."
        );
    }

    public static BusinessException soDuKhongDu(double soDu, double soTienRut) {
        return new BusinessException(
                HttpStatus.BAD_REQUEST,
                "INSUFFICIENT_BALANCE",
                String.format("So du hien tai %.0f VND khong du de rut %.0f VND.", soDu, soTienRut)
        );
    }

    public static BusinessException hoSoTaiXeChuaTonTai(String userId) {
        return new BusinessException(
                HttpStatus.NOT_FOUND,
                "DRIVER_PROFILE_NOT_FOUND",
                "Không tìm thấy hồ sơ tài xế với user ID [" + userId + "]."
        );
    }

    public static BusinessException trangThaiDonHangKhongHopLe(String orderId, int status, String hanhDong) {
        String tenTrangThai;
        if (status == 0) {
            tenTrangThai = "Chờ xác nhận";
        } else if (status == 1) {
            tenTrangThai = "Đang chuẩn bị";
        } else if (status == 2) {
            tenTrangThai = "Đang giao";
        } else if (status == 3) {
            tenTrangThai = "Hoàn thành";
        } else if (status == 4) {
            tenTrangThai = "Đã hủy";
        } else {
            tenTrangThai = "Không xác định";
        }
        return new BusinessException(
                HttpStatus.BAD_REQUEST,
                "ORDER_STATUS_INVALID",
                "Không thể " + hanhDong + " đơn hàng [" + orderId + "] vì đơn đang ở trạng thái [" + tenTrangThai + "]."
        );
    }

    public static BusinessException donHangDaCoTaiXe(String orderId) {
        return new BusinessException(
                HttpStatus.BAD_REQUEST,
                "ORDER_ALREADY_ASSIGNED",
                "Đơn hàng [" + orderId + "] đã được assign cho tài xế khác."
        );
    }

    public static BusinessException taiXeKhongTimThay(String userId) {
        return new BusinessException(
                HttpStatus.NOT_FOUND,
                "DRIVER_NOT_FOUND",
                "Không tìm thấy tài xế với user ID [" + userId + "]."
        );
    }

    public static BusinessException viKhongTonTai(String userId) {
        return new BusinessException(
                HttpStatus.NOT_FOUND,
                "WALLET_NOT_FOUND",
                "Không tìm thấy ví tài xế với user ID [" + userId + "]."
        );
    }

    public static BusinessException voucherKhongTheDoiDiem(String voucherId) {
        return new BusinessException(
                HttpStatus.BAD_REQUEST,
                "VOUCHER_CANNOT_EXCHANGE",
                "Voucher [" + voucherId + "] không hỗ trợ đổi điểm. Voucher này chỉ sử dụng trực tiếp khi đặt hàng."
        );
    }

    public static BusinessException vuotGioiHanRutTien(String message) {
        return new BusinessException(
                HttpStatus.BAD_REQUEST,
                "WITHDRAWAL_LIMIT_EXCEEDED",
                message
        );
    }
}
