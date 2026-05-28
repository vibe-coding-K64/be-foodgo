package com.example.be_foodgo.controller;

import com.example.be_foodgo.dto.SystemVoucherResponse;
import com.example.be_foodgo.dto.VoucherExchangeRequest;
import com.example.be_foodgo.dto.VoucherExchangeResponse;
import com.example.be_foodgo.exception.ApiResponse;
import com.example.be_foodgo.model.MyVoucher;
import com.example.be_foodgo.security.JwtTokenProvider;
import com.example.be_foodgo.service.VoucherExchangeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
@Tag(name = "Doi diem thanh voucher", description = "API doi diem thanh voucher tu system_vouchers")
public class VoucherExchangeController {

    private final VoucherExchangeService voucherExchangeService;
    private final JwtTokenProvider jwtTokenProvider;

    public VoucherExchangeController(VoucherExchangeService voucherExchangeService, JwtTokenProvider jwtTokenProvider) {
        this.voucherExchangeService = voucherExchangeService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    private String trichXuatUserIdTuHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        return jwtTokenProvider.layUserIdTuToken(token);
    }

    @GetMapping("/system")
    @Operation(summary = "Lay danh sach voucher co the doi")
    public ResponseEntity<ApiResponse<List<SystemVoucherResponse>>> getSystemVouchers(HttpServletRequest request) {
        String userId = trichXuatUserIdTuHeader(request);
        List<SystemVoucherResponse> vouchers = voucherExchangeService.getAllSystemVouchers(userId);
        return ResponseEntity.ok(ApiResponse.thatSuccess(vouchers, "Lay danh sach voucher thanh cong."));
    }

    @PostMapping("/exchange")
    @Operation(summary = "Doi diem thanh voucher")
    public ResponseEntity<ApiResponse<VoucherExchangeResponse>> exchangeVoucher(
            HttpServletRequest request,
            @Valid @RequestBody VoucherExchangeRequest exchangeRequest) {
        String userId = trichXuatUserIdTuHeader(request);
        if (userId == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.<VoucherExchangeResponse>thatError(401, "Chua xac thuc. Vui long dang nhap."));
        }
        VoucherExchangeResponse result = voucherExchangeService.doiVoucher(userId, exchangeRequest);
        return ResponseEntity.ok(ApiResponse.thatSuccess(result, "Doi voucher thanh cong!"));
    }

    @GetMapping("/my-vouchers")
    @Operation(summary = "Lay danh sach voucher cua toi")
    public ResponseEntity<ApiResponse<List<MyVoucher>>> getMyVouchers(HttpServletRequest request) {
        String userId = trichXuatUserIdTuHeader(request);
        if (userId == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.<List<MyVoucher>>thatError(401, "Chua xac thuc. Vui long dang nhap."));
        }
        List<MyVoucher> vouchers = voucherExchangeService.getMyVouchers(userId);
        return ResponseEntity.ok(ApiResponse.thatSuccess(vouchers, "Lay danh sach voucher thanh cong."));
    }
}
