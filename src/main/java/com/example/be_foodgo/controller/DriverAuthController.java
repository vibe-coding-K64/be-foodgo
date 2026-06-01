package com.example.be_foodgo.controller;

import com.example.be_foodgo.dto.*;
import com.example.be_foodgo.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Dang ky tai xe (Driver Auth)", description = "API dang ky tai khoan tai xe")
public class DriverAuthController {

    private final AuthService authService;

    public DriverAuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register-driver/send-otp")
    @Operation(
            summary = "Gui OTP xac thuc email de dang ky tai xe (buoc 1)",
            description = "Gui ma OTP 6 chu so den email de xac thuc. Co gioi han gui lai 1 lan moi 60 giay."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ma OTP da duoc gui",
                    content = @Content(schema = @Schema(implementation = OtpSendResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Email/SĐT da ton tai hoac cho cooldown")
    })
    public ResponseEntity<?> sendOtpDangKyTaiXe(@Valid @RequestBody RegisterDriverRequest request) {
        try {
            OtpSendResponse response = authService.guiOtpDangKyTaiXe(
                    request.getEmail(), request.getPassword(), request.getFullName(),
                    request.getPhoneNumber());
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(
                    com.example.be_foodgo.exception.ApiResponse.thatError(429, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    com.example.be_foodgo.exception.ApiResponse.thatError(400, e.getMessage()));
        }
    }

    @PostMapping("/register-driver/complete")
    @Operation(
            summary = "Hoan tat dang ky tai xe (buoc 2)",
            description = "Xac thuc ma OTP nhan duoc. Neu dung, tai khoan se duoc tao voi role=2 (tai xe), " +
                    "tao driver_profile va wallet, tra ve JWT token."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dang ky thanh cong, tra ve JWT token",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Ma OTP khong dung hoac da het han")
    })
    public ResponseEntity<?> completeRegisterTaiXe(@Valid @RequestBody RegisterDriverCompleteRequest request) {
        try {
            AuthResponse response = authService.xacThucDangKyTaiXe(
                    request.getEmail().toLowerCase().trim(), request.getOtpCode());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    com.example.be_foodgo.exception.ApiResponse.thatError(400, e.getMessage()));
        }
    }
}
