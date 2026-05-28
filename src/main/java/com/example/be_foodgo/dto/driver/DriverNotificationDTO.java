package com.example.be_foodgo.dto.driver;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thong bao danh cho tai xe")
public class DriverNotificationDTO {

    @Schema(description = "ID thong bao", example = "dnotif_001")
    private String id;

    @Schema(description = "Loai thong bao: 11=Yeu cau nhan don moi, 12=Thong bao giao hang, 13=Don da duoc giao cho tai xe khac", example = "13")
    private Integer type;

    @Schema(description = "Tieu de thong bao", example = "Don hang da duoc giao cho tai xe khac")
    private String title;

    @Schema(description = "Noi dung thong bao", example = "Don hang order_001 da duoc tai xe khac nhan. Vui long cho don hang tiep theo.")
    private String body;

    @Schema(description = "ID don hang lien quan", example = "order_001")
    private String orderId;

    @Schema(description = "ID tham chieu (thuong trung voi orderId)", example = "order_001")
    private String referenceId;

    @Schema(description = "Da doc chua", example = "false")
    private Boolean isRead;

    @Schema(description = "URL hinh anh kem theo", example = "https://example.com/order.jpg")
    private String imageUrl;

    @Schema(description = "Thoi diem tao thong bao")
    private Instant createdAt;
}
