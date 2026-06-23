package com.example.be_foodgo.constant;

public final class DeliveryOrderStatus {

    private DeliveryOrderStatus() {
    }

    public static final int PENDING_STORE_CONFIRMATION = 0;
    public static final int WAITING_DRIVER = 1;
    public static final int DELIVERING = 2;
    public static final int COMPLETED = 3;
    public static final int CANCELLED = 4;

    public static String getCode(int status) {
        return switch (status) {
            case PENDING_STORE_CONFIRMATION -> "PENDING_STORE_CONFIRMATION";
            case WAITING_DRIVER -> "WAITING_DRIVER";
            case DELIVERING -> "DELIVERING";
            case COMPLETED -> "COMPLETED";
            case CANCELLED -> "CANCELLED";
            default -> "UNKNOWN";
        };
    }

    public static String getDescription(int status) {
        return switch (status) {
            case PENDING_STORE_CONFIRMATION -> "Đơn mới tạo, đang chờ cửa hàng xác nhận.";
            case WAITING_DRIVER -> "Đơn đã sẵn sàng hoặc đang chờ tài xế nhận.";
            case DELIVERING -> "Tài xế đã nhận đơn và đang trong quá trình giao.";
            case COMPLETED -> "Đơn đã giao thành công.";
            case CANCELLED -> "Đơn đã bị hủy.";
            default -> "Không xác định.";
        };
    }

    public static String getDisplayName(int status) {
        return switch (status) {
            case PENDING_STORE_CONFIRMATION -> "Chờ cửa hàng xác nhận";
            case WAITING_DRIVER -> "Chờ tài xế nhận";
            case DELIVERING -> "Đang giao";
            case COMPLETED -> "Hoàn thành";
            case CANCELLED -> "Đã hủy";
            default -> "Không xác định";
        };
    }
}
