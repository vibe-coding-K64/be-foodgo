package com.example.be_foodgo.controller;

import com.example.be_foodgo.dto.VoucherDTO;
import com.example.be_foodgo.dto.VoucherListResponse;
import com.example.be_foodgo.exception.ApiResponse;
import com.example.be_foodgo.model.Voucher;
import com.example.be_foodgo.service.VoucherService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/vouchers")
@CrossOrigin(origins = "*")
@Tag(name = "Voucher Management", description = "Quản lý mã giảm giá")
public class VoucherController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(VoucherController.class);

    @Autowired
    private VoucherService voucherService;

    public VoucherController() {
        super(LoggerFactory.getLogger(VoucherController.class));
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Tạo mã giảm giá mới", description = "Thêm một mã giảm giá mới vào hệ thống")
    public ResponseEntity<ApiResponse<String>> createVoucher(@RequestBody VoucherDTO voucherDTO, HttpServletRequest request) {
        String userId = trichXuatUserIdTuHeader(request);
        if (userId == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.thatError(401, "Chua xac thuc. Vui long dang nhap de tiep tuc."));
        }
        try {
            String updateTime = voucherService.createVoucher(voucherDTO, userId);
            return ResponseEntity.ok(ApiResponse.thatSuccess(updateTime, "Tao voucher thanh cong"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.thatError(403, e.getMessage()));
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.thatError(500, "Loi khi tao voucher: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin mã giảm giá", description = "Lấy chi tiết mã giảm giá theo ID")
    public ResponseEntity<Voucher> getVoucher(@PathVariable String id) {
        try {
            Voucher voucher = voucherService.getVoucher(id);
            if (voucher != null) {
                return ResponseEntity.ok(voucher);
            }
            return ResponseEntity.notFound().build();
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách voucher", description = "Lấy danh sách voucher theo userId và storeId")
    public ResponseEntity<?> getVouchers(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String storeId) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                java.util.List<Voucher> data = voucherService.getAllVouchers(storeId);
                return ResponseEntity.ok(data);
            }
            VoucherListResponse data = voucherService.getAvailableVouchers(userId, storeId);
            return ResponseEntity.ok(ApiResponse.thatSuccess(data, "Lấy danh sách voucher thành công"));
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.thatError(500, "Lỗi khi lấy danh sách voucher: " + e.getMessage()));
        }
    }

    @GetMapping("/store/{storeId}")
    @Operation(summary = "Lấy danh sách voucher của gian hàng", description = "Lấy danh sách voucher theo storeId")
    public ResponseEntity<ApiResponse<java.util.List<Voucher>>> getVouchersByStore(
            @PathVariable String storeId) {
        try {
            java.util.List<Voucher> data = voucherService.getAllVouchers(storeId);
            return ResponseEntity.ok(ApiResponse.thatSuccess(data, "Lấy danh sách voucher thành công"));
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.thatError(500, "Lỗi khi lấy danh sách voucher: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cập nhật mã giảm giá", description = "Cập nhật thông tin mã giảm giá")
    public ResponseEntity<ApiResponse<String>> updateVoucher(@PathVariable String id, @RequestBody VoucherDTO voucherDTO, HttpServletRequest request) {
        String userId = trichXuatUserIdTuHeader(request);
        if (userId == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.thatError(401, "Chua xac thuc. Vui long dang nhap de tiep tuc."));
        }
        try {
            String updateTime = voucherService.updateVoucher(id, voucherDTO, userId);
            return ResponseEntity.ok(ApiResponse.thatSuccess(updateTime, "Cap nhat voucher thanh cong"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.thatError(403, e.getMessage()));
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.thatError(500, "Loi khi cap nhat voucher: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Xóa mã giảm giá", description = "Xóa mã giảm giá khỏi hệ thống")
    public ResponseEntity<ApiResponse<String>> deleteVoucher(@PathVariable String id, HttpServletRequest request) {
        String userId = trichXuatUserIdTuHeader(request);
        if (userId == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.thatError(401, "Chua xac thuc. Vui long dang nhap de tiep tuc."));
        }
        try {
            String result = voucherService.deleteVoucher(id, userId);
            if ("Voucher not found".equals(result) || result.startsWith("Khong co quyen")) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.thatError(403, result));
            }
            return ResponseEntity.ok(ApiResponse.thatSuccess(result, "Xoa voucher thanh cong"));
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.thatError(500, "Loi khi xoa voucher: " + e.getMessage()));
        }
    }
}
