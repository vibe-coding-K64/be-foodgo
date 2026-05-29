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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@Tag(name = "Wallet", description = "API quan ly vi tien cua tai xe")
public class WalletController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(WalletController.class);

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        super(log);
        this.walletService = walletService;
    }

    @GetMapping("/wallet")
    @Operation(
            summary = "Lay thong tin vi tien",
            description = "Lay thong tin vi tien cua tai xe hien tai, bao gom so du, tong thu nhap, tong da rut, va so du cho."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lay thong tin vi thanh cong",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chua xac thuc - Token khong hop le hoac chua dang nhap")
    })
    public ResponseEntity<?> getWallet(HttpServletRequest httpRequest) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            WalletDTO wallet = walletService.getDriverWallet(holder.userId);
            return ResponseEntity.ok(ApiResponse.thatSuccess(wallet, "Lay thong tin vi thanh cong."));
        } catch (Exception e) {
            log.error("Loi khi lay thong tin vi: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon. Vui long thu lai sau."));
        }
    }

    @GetMapping("/transactions")
    @Operation(
            summary = "Lay lich su giao dich",
            description = "Lay danh sach giao dich thu nhap giao hang (type == delivery_income) cua tai xe, co phan trang."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lay lich su giao dich thanh cong",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chua xac thuc - Token khong hop le hoac chua dang nhap")
    })
    public ResponseEntity<?> getTransactions(
            HttpServletRequest httpRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            List<TransactionDTO> transactions = walletService.getDriverTransactions(holder.userId, page, size);
            return ResponseEntity.ok(ApiResponse.thatSuccess(transactions, "Lay lich su giao dich thanh cong."));
        } catch (Exception e) {
            log.error("Loi khi lay lich su giao dich: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon. Vui long thu lai sau."));
        }
    }

    @PostMapping("/withdraw")
    @Operation(
            summary = "Yeu cau rut tien",
            description = "Tai xe yeu cau rut tien tu vi. He thong kiem tra so du, gioi han rut tien (min/max), va tao giao dich pending."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Yeu cau rut tien thanh cong",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "So du khong du hoac vuot gioi han rut tien"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chua xac thuc"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Khong tim thay vi cua tai xe")
    })
    public ResponseEntity<?> withdraw(
            HttpServletRequest httpRequest,
            @Valid @RequestBody WithdrawRequest request) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            TransactionDTO transaction = walletService.requestWithdrawal(holder.userId, request.getAmount());
            return ResponseEntity.ok(ApiResponse.thatSuccess(transaction, "Yeu cau rut tien thanh cong. Vui long cho he thong xu ly."));
        } catch (BusinessException e) {
            log.warn("Loi business khi rut tien: {}", e.getMessage());
            return ResponseEntity.status(e.getStatus().value()).body(
                    ApiResponse.thatError(e.getStatus().value(), e.getMessage()));
        } catch (Exception e) {
            log.error("Loi khi rut tien: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon. Vui long thu lai sau."));
        }
    }
}
