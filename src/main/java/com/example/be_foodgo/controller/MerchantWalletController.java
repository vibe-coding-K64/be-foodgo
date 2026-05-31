package com.example.be_foodgo.controller;

import com.example.be_foodgo.dto.TransactionDTO;
import com.example.be_foodgo.dto.WalletDTO;
import com.example.be_foodgo.dto.WithdrawRequest;
import com.example.be_foodgo.exception.ApiResponse;
import com.example.be_foodgo.exception.BusinessException;
import com.example.be_foodgo.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/merchants")
@Tag(name = "Merchant Wallet", description = "API quản lý ví tiền của gian hàng (merchant)")
@SecurityRequirement(name = "bearerAuth")
public class MerchantWalletController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(MerchantWalletController.class);
    private final WalletService walletService;

    public MerchantWalletController(WalletService walletService) {
        super(log);
        this.walletService = walletService;
    }

    @GetMapping("/wallet")
    @Operation(summary = "Lấy thông tin ví gian hàng", description = "Trả về số dư, tổng doanh thu, số dư đang chờ xử lý...")
    public ResponseEntity<?> getWallet(HttpServletRequest httpRequest) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) return ResponseEntity.status(401).body(holder.errorResponse);

        try {
            WalletDTO wallet = walletService.getMerchantWallet(holder.userId);
            return ResponseEntity.ok(ApiResponse.thatSuccess(wallet, "Lấy thông tin ví thành công."));
        } catch (Exception e) {
            log.error("Loi khi lay thong tin vi merchant: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Đã xảy ra lỗi không mong muốn. Vui lòng thử lại sau."));
        }
    }

    @GetMapping("/transactions")
    @Operation(summary = "Lấy lịch sử giao dịch", description = "Lấy lịch sử thanh toán đơn hàng và rút tiền")
    public ResponseEntity<?> getTransactions(
            HttpServletRequest httpRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) return ResponseEntity.status(401).body(holder.errorResponse);

        try {
            List<TransactionDTO> transactions = walletService.getMerchantTransactions(holder.userId, page, size);
            return ResponseEntity.ok(ApiResponse.thatSuccess(transactions, "Lấy lịch sử giao dịch thành công."));
        } catch (Exception e) {
            log.error("Loi khi lay lich su giao dich merchant: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Đã xảy ra lỗi không mong muốn. Vui lòng thử lại sau."));
        }
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Yêu cầu rút tiền", description = "Chủ gian hàng yêu cầu rút tiền từ ví")
    public ResponseEntity<?> withdraw(
            HttpServletRequest httpRequest,
            @Valid @RequestBody WithdrawRequest request) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) return ResponseEntity.status(401).body(holder.errorResponse);

        try {
            TransactionDTO transaction = walletService.requestMerchantWithdrawal(holder.userId, request.getAmount());
            return ResponseEntity.ok(ApiResponse.thatSuccess(transaction, "Yêu cầu rút tiền thành công. Vui lòng chờ hệ thống xử lý."));
        } catch (BusinessException e) {
            log.warn("Loi business khi rut tien: {}", e.getMessage());
            return ResponseEntity.status(e.getStatus().value()).body(
                    ApiResponse.thatError(e.getStatus().value(), e.getMessage()));
        } catch (Exception e) {
            log.error("Loi khi rut tien: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Đã xảy ra lỗi không mong muốn. Vui lòng thử lại sau."));
        }
    }
}
