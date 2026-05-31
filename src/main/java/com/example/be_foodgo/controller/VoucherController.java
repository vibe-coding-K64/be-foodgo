package com.example.be_foodgo.controller;

import com.example.be_foodgo.dto.VoucherDTO;
import com.example.be_foodgo.dto.VoucherListResponse;
import com.example.be_foodgo.exception.ApiResponse;
import com.example.be_foodgo.model.Voucher;
import com.example.be_foodgo.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/vouchers")
@CrossOrigin(origins = "*") // Hỗ trợ frontend gọi API
@Tag(name = "Voucher Management", description = "Quản lý mã giảm giá")
public class VoucherController {

    @Autowired
    private VoucherService voucherService;

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Tạo mã giảm giá mới", description = "Thêm một mã giảm giá mới vào hệ thống")
    public ResponseEntity<String> createVoucher(@RequestBody VoucherDTO voucherDTO) {
        try {
            String updateTime = voucherService.createVoucher(voucherDTO);
            return ResponseEntity.ok(updateTime);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(500).body("Error creating voucher: " + e.getMessage());
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
    public ResponseEntity<ApiResponse<VoucherListResponse>> getVouchers(
            @RequestParam String userId,
            @RequestParam(required = false) String storeId) {
        try {
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
    public ResponseEntity<String> updateVoucher(@PathVariable String id, @RequestBody VoucherDTO voucherDTO) {
        try {
            String updateTime = voucherService.updateVoucher(id, voucherDTO);
            return ResponseEntity.ok(updateTime);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(500).body("Error updating voucher: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Xóa mã giảm giá", description = "Xóa mã giảm giá khỏi hệ thống")
    public ResponseEntity<String> deleteVoucher(@PathVariable String id) {
        String result = voucherService.deleteVoucher(id);
        return ResponseEntity.ok(result);
    }
}
