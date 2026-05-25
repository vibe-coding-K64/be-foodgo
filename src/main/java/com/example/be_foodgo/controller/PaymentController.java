package com.example.be_foodgo.controller;

import com.example.be_foodgo.dto.PaymentRequest;
import com.example.be_foodgo.dto.PaymentResponse;
import com.example.be_foodgo.exception.ApiResponse;
import com.example.be_foodgo.security.JwtTokenProvider;
import com.example.be_foodgo.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Thanh toan (Payment)", description = "API quan ly phuong thuc thanh toan cho phan he Khach hang")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;
    private final JwtTokenProvider jwtTokenProvider;

    public PaymentController(PaymentService paymentService, JwtTokenProvider jwtTokenProvider) {
        this.paymentService = paymentService;
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

    @GetMapping
    @Operation(
            summary = "Lay danh sach phuong thuc thanh toan",
            description = "Tra ve danh sach tat ca phuong thuc thanh toan da dang ky cua nguoi dung hien tai"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lay danh sach thanh cong",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chua xac thuc - Token khong hop le hoac chua dang nhap")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> layTatCaPhuongThuc(HttpServletRequest httpRequest) {
        try {
            String userId = trichXuatUserIdTuHeader(httpRequest);
            if (userId == null) {
                log.warn("Token xac thuc khong hop le hoac khong co token");
                return ResponseEntity.status(401).body(
                        ApiResponse.thatError(401, "Chua xac thuc. Vui long dang nhap de tiep tuc."));
            }

            log.info("Yeu cau lay danh sach phuong thuc thanh toan tu userId: {}", userId);
            List<PaymentResponse> paymentMethods = paymentService.layTatCaPhuongThuc(userId);
            return ResponseEntity.ok(ApiResponse.thatSuccess(paymentMethods, "Lay danh sach phuong thuc thanh toan thanh cong."));
        } catch (Exception e) {
            log.error("Loi he thong khi lay danh sach phuong thuc thanh toan: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon. Vui long thu lai sau."));
        }
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Lay mot phuong thuc thanh toan",
            description = "Tra ve thong tin chi tiet cua mot phuong thuc thanh toan theo ID"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lay phuong thuc thanh cong",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chua xac thuc - Token khong hop le hoac chua dang nhap"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Khong tim thay phuong thuc thanh toan")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> layMotPhuongThuc(
            HttpServletRequest httpRequest,
            @PathVariable("id") String paymentMethodId) {
        try {
            String userId = trichXuatUserIdTuHeader(httpRequest);
            if (userId == null) {
                log.warn("Token xac thuc khong hop le hoac khong co token");
                return ResponseEntity.status(401).body(
                        ApiResponse.thatError(401, "Chua xac thuc. Vui long dang nhap de tiep tuc."));
            }

            log.info("Yeu cau lay phuong thuc thanh toan [{}] tu userId: {}", paymentMethodId, userId);
            PaymentResponse paymentMethod = paymentService.layMotPhuongThuc(userId, paymentMethodId);
            return ResponseEntity.ok(ApiResponse.thatSuccess(paymentMethod, "Lay phuong thuc thanh toan thanh cong."));
        } catch (Exception e) {
            log.error("Loi he thong khi lay phuong thuc thanh toan [{}]: {}", paymentMethodId, e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon. Vui long thu lai sau."));
        }
    }

    @PostMapping
    @Operation(
            summary = "Them phuong thuc thanh toan moi",
            description = "Them mot phuong thuc thanh toan moi cho nguoi dung. Neu isDefault=true, cac phuong thuc mac dinh cu se bi bo danh dau bang WriteBatch (atomic)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Them phuong thuc thanh cong",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Du lieu khong hop le hoac loai phuong thuc thanh toan khong hop le"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chua xac thuc - Token khong hop le hoac chua dang nhap")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> themPhuongThuc(
            HttpServletRequest httpRequest,
            @Valid @RequestBody PaymentRequest request) {
        try {
            String userId = trichXuatUserIdTuHeader(httpRequest);
            if (userId == null) {
                log.warn("Token xac thuc khong hop le hoac khong co token");
                return ResponseEntity.status(401).body(
                        ApiResponse.thatError(401, "Chua xac thuc. Vui long dang nhap de tiep tuc."));
            }

            log.info("Yeu cau them phuong thuc thanh toan moi tu userId: {}, type: {}, isDefault: {}",
                    userId, request.getType(), request.getIsDefault());
            PaymentResponse created = paymentService.themPhuongThuc(userId, request);
            return ResponseEntity.ok(ApiResponse.thatSuccess(created, "Them phuong thuc thanh toan thanh cong."));
        } catch (Exception e) {
            log.error("Loi he thong khi them phuong thuc thanh toan: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon. Vui long thu lai sau."));
        }
    }

    @PutMapping("/{id}/default")
    @Operation(
            summary = "Dat phuong thuc thanh toan mac dinh",
            description = "Dat mot phuong thuc thanh toan lam phuong thuc mac dinh. Su dung WriteBatch (atomic) de dong thoi bo danh dau cu va dat danh dau moi."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Dat phuong thuc mac dinh thanh cong",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chua xac thuc - Token khong hop le hoac chua dang nhap"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Khong tim thay phuong thuc thanh toan")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> datPhuongThucMacDinh(
            HttpServletRequest httpRequest,
            @PathVariable("id") String paymentMethodId) {
        try {
            String userId = trichXuatUserIdTuHeader(httpRequest);
            if (userId == null) {
                log.warn("Token xac thuc khong hop le hoac khong co token");
                return ResponseEntity.status(401).body(
                        ApiResponse.thatError(401, "Chua xac thuc. Vui long dang nhap de tiep tuc."));
            }

            log.info("Yeu cau dat phuong thuc thanh toan [{}] lam mac dinh tu userId: {}", paymentMethodId, userId);
            paymentService.datPhuongThucMacDinh(userId, paymentMethodId);
            return ResponseEntity.ok(ApiResponse.thatSuccess(null, "Dat phuong thuc thanh toan mac dinh thanh cong."));
        } catch (Exception e) {
            log.error("Loi he thong khi dat phuong thuc thanh toan mac dinh [{}]: {}", paymentMethodId, e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon. Vui long thu lai sau."));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Xoa phuong thuc thanh toan",
            description = "Xoa mot phuong thuc thanh toan. Tra ve 200 OK ngay ca khi phuong thuc khong ton tai."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Xoa phuong thuc thanh cong (hoac khong ton tai)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chua xac thuc - Token khong hop le hoac chua dang nhap")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> xoaPhuongThuc(
            HttpServletRequest httpRequest,
            @PathVariable("id") String paymentMethodId) {
        try {
            String userId = trichXuatUserIdTuHeader(httpRequest);
            if (userId == null) {
                log.warn("Token xac thuc khong hop le hoac khong co token");
                return ResponseEntity.status(401).body(
                        ApiResponse.thatError(401, "Chua xac thuc. Vui long dang nhap de tiep tuc."));
            }

            log.info("Yeu cau xoa phuong thuc thanh toan [{}] tu userId: {}", paymentMethodId, userId);
            paymentService.xoaPhuongThuc(userId, paymentMethodId);
            return ResponseEntity.ok(ApiResponse.thatSuccess(null, "Xoa phuong thuc thanh toan thanh cong."));
        } catch (Exception e) {
            log.error("Loi he thong khi xoa phuong thuc thanh toan [{}]: {}", paymentMethodId, e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon. Vui long thu lai sau."));
        }
    }
}
