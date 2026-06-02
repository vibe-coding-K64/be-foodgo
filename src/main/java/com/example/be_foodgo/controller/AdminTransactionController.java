package com.example.be_foodgo.controller;

import com.example.be_foodgo.dto.TransactionDTO;
import com.example.be_foodgo.exception.ApiResponse;
import com.example.be_foodgo.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/transactions")
@Tag(name = "Admin Transaction Management", description = "API duyet yeu cau rut tien danh cho Admin")
@SecurityRequirement(name = "bearerAuth")
public class AdminTransactionController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(AdminTransactionController.class);

    private final WalletService walletService;

    public AdminTransactionController(WalletService walletService) {
        super(log);
        this.walletService = walletService;
    }

    @GetMapping("/withdrawals/pending")
    @Operation(
            summary = "Lay danh sach yeu cau rut tien dang cho",
            description = "Tra ve danh sach cac yeu cau rut tien dang cho duyet (status == 'pending')."
    )
    public ResponseEntity<?> getPendingWithdrawals(HttpServletRequest httpRequest) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            List<TransactionDTO> pending = walletService.getPendingWithdrawals();
            return ResponseEntity.ok(ApiResponse.thatSuccess(pending, "Lay danh sach yeu cau rut tien pending thanh cong."));
        } catch (Exception e) {
            log.error("Loi khi lay yeu cau rut tien pending: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon."));
        }
    }

    @PostMapping("/withdrawals/{id}/approve")
    @Operation(
            summary = "Phe duyet yeu cau rut tien",
            description = "Chuyen trang thai yeu cau rut tien sang 'completed' va tru khoi quy quy pending."
    )
    public ResponseEntity<?> approveWithdrawal(
            HttpServletRequest httpRequest,
            @PathVariable("id") String transactionId) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            walletService.approveWithdrawal(transactionId);
            return ResponseEntity.ok(ApiResponse.thatSuccess(true, "Phe duyet yeu cau rut tien thanh cong."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.thatError(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Loi khi phe duyet yeu cau rut tien: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon."));
        }
    }

    @PostMapping("/withdrawals/{id}/reject")
    @Operation(
            summary = "Tu choi yeu cau rut tien",
            description = "Tu choi yeu cau rut tien, chuyen trang thai sang 'failed' va hoan lai so du cho tai khoan."
    )
    public ResponseEntity<?> rejectWithdrawal(
            HttpServletRequest httpRequest,
            @PathVariable("id") String transactionId,
            @RequestParam(required = false, defaultValue = "Khong duoc duyet") String reason) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            walletService.rejectWithdrawal(transactionId, reason);
            return ResponseEntity.ok(ApiResponse.thatSuccess(true, "Tu choi yeu cau rut tien thanh cong."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.thatError(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Loi khi tu choi yeu cau rut tien: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon."));
        }
    }
}
