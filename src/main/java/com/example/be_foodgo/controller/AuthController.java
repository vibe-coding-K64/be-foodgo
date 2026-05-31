package com.example.be_foodgo.controller;

import com.example.be_foodgo.dto.*;
import com.example.be_foodgo.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Xac thuc (Auth)", description = "API xac thuc tai khoan, dang nhap, dang ky, va khoi phuc mat khau")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(
            summary = "Dang ky tai khoan khach hang moi",
            description = "Tao tai khoan khach hang moi voi email, mat khau (ma hoa BCrypt), ho ten va so dien thoai. Mac dinh roles = [1] (Khach hang)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dang ky thanh cong, tra ve JWT token",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Email hoac so dien thoai da ton tai")
    })
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    com.example.be_foodgo.exception.ApiResponse.thatError(
                            400, e.getMessage()));
        }
    }

    @PostMapping("/login")
    @Operation(
            summary = "Dang nhap",
            description = "Dang nhap bang email va mat khau. Neu thanh cong, tra ve JWT token chua userId."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dang nhap thanh cong, tra ve JWT token",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Email hoac mat khau khong dung")
    })
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    com.example.be_foodgo.exception.ApiResponse.thatError(
                            400, e.getMessage()));
        }
    }

    @PostMapping("/send-otp")
    @Operation(
            summary = "Gui ma OTP",
            description = "Gui ma OTP 6 chu so den email hoac so dien thoai de khoi phuc mat khau. " +
                    "Ma OTP co hieu luc 5 phut. Trong moi truong dev/demo, ma OTP se in ra console."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ma OTP da duoc gui",
                    content = @Content(schema = @Schema(implementation = OtpSendResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Khong tim thay tai khoan")
    })
    public ResponseEntity<?> sendOtp(@Valid @RequestBody OtpSendRequest request) {
        try {
            OtpSendResponse response = authService.guiOtp(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    com.example.be_foodgo.exception.ApiResponse.thatError(
                            400, e.getMessage()));
        }
    }

    @PostMapping("/verify-otp")
    @Operation(
            summary = "Xac thuc ma OTP",
            description = "Xac thuc ma OTP nhan duoc. Neu dung, tra ve token tam thoi (hieu luc 5 phut) " +
                    "de su dung cho reset-password."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xac thuc thanh cong, tra ve token tam thoi",
                    content = @Content(schema = @Schema(implementation = OtpVerifyResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Ma OTP khong dung hoac da het han")
    })
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        try {
            OtpVerifyResponse response = authService.xacThucOtp(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    com.example.be_foodgo.exception.ApiResponse.thatError(
                            400, e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Dat lai mat khau",
            description = "Dat lai mat khau moi sau khi xac thuc OTP thanh cong. Token tam thoi " +
                    "co hieu luc 5 phut sau khi xac thuc OTP."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dat lai mat khau thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Token khong hop le hoac da het han")
    })
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            authService.datLaiMatKhau(request);
            return ResponseEntity.ok(
                    com.example.be_foodgo.exception.ApiResponse.thatSuccess(
                            null, "Dat lai mat khau thanh cong."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    com.example.be_foodgo.exception.ApiResponse.thatError(
                            400, e.getMessage()));
        }
    }

    @PostMapping("/register-merchant")
    @Operation(
            summary = "Dang ky tai khoan nguoi ban",
            description = "Tao tai khoan nguoi ban moi voi roles = [3]. Su dung de tich hop voi quy trinh " +
                    "dang ky nguoi ban cua he thong."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dang ky thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Email hoac so dien thoai da ton tai")
    })
    public ResponseEntity<?> registerMerchant(@RequestBody AuthRequestDTO request) {
        try {
            return ResponseEntity.ok(authService.registerMerchant(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    com.example.be_foodgo.exception.ApiResponse.thatError(
                            400, e.getMessage()));
        }
    }

    @GetMapping("/check-merchant")
    @Operation(
            summary = "Kiem tra quyen nguoi ban",
            description = "Kiem tra xem tai khoan co quyen nguoi ban (role = 3) hay khong."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ket qua kiem tra quyen")
    })
    public ResponseEntity<?> checkMerchantRole(
            @Parameter(description = "ID tai khoan nguoi dung", example = "user_001")
            @RequestParam String uid) {
        try {
            return ResponseEntity.ok(authService.checkMerchantProfile(uid));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    com.example.be_foodgo.exception.ApiResponse.thatError(
                            400, "Loi kiem tra quyen: " + e.getMessage()));
        }
    }

    @PostMapping("/refresh-token")
    @Operation(
            summary = "Lam moi access token",
            description = "Dung refresh token de nhan access token moi. Refresh token cu se bi thu hoi va mot refresh token moi se duoc tra ve. Refresh token co hieu luc 30 ngay."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lam moi thanh cong, tra ve access token moi va refresh token moi",
                    content = @Content(schema = @Schema(implementation = RefreshTokenResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Refresh token khong hop le hoac da bi thu hoi")
    })
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            RefreshTokenResponse response = authService.refreshToken(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    com.example.be_foodgo.exception.ApiResponse.thatError(
                            400, e.getMessage()));
        }
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Dang xuat",
            description = "Thu hoi tat ca refresh token cua nguoi dung hien tai. Access token se tu dong het hieu luc sau 3 gio."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dang xuat thanh cong")
    })
    public ResponseEntity<?> logout(
            @Parameter(description = "Header Authorization chua access token", example = "Bearer eyJhbGciOiJIUzI1NiJ9...")
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh token can thu hoi",
                    content = @Content(schema = @Schema(implementation = RefreshTokenRequest.class)))
            @RequestBody(required = false) RefreshTokenRequest request) {
        try {
            String refreshToken = (request != null) ? request.getRefreshToken() : null;
            authService.logout(authHeader, refreshToken);
            return ResponseEntity.ok(
                    com.example.be_foodgo.exception.ApiResponse.thatSuccess(
                            null, "Dang xuat thanh cong."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    com.example.be_foodgo.exception.ApiResponse.thatError(
                            400, e.getMessage()));
        }
    }

    @PostMapping("/firebase/link")
    @Operation(
            summary = "Lien ket tai khoan Firebase (Email/Password)",
            description = "Lien ket tai khoan Firebase Authentication (email/password) voi tai khoan backend. " +
                    "Neu tai khoan Firebase chua ton tai trong he thong, se tu dong tao moi. " +
                    "Neu da ton tai, se tra ve JWT cho tai khoan do. " +
                    "Hoac su dung header X-Firebase-Token de xac thuc truc tiep bang Firebase ID token."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lien ket xac thuc thanh cong, tra ve JWT",
                    content = @Content(schema = @Schema(implementation = FirebaseAuthResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Firebase token khong hop le")
    })
    public ResponseEntity<?> linkFirebaseAccount(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Firebase ID token hoac email/password de lien ket",
                    content = @Content(schema = @Schema(implementation = FirebaseLinkRequest.class)))
            @RequestBody(required = false) FirebaseLinkRequest request,
            @Parameter(description = "Firebase ID token (thay the request body)", example = "eyJhbGci...")
            @RequestHeader(value = "X-Firebase-Token", required = false) String firebaseToken) {
        try {
            FirebaseAuthResponse response;
            if (firebaseToken != null && !firebaseToken.isBlank()) {
                response = authService.linkFirebaseToken(firebaseToken, null, null);
            } else if (request != null) {
                response = authService.linkFirebaseToken(request.getIdToken(), request.getEmail(), request.getPassword());
            } else {
                return ResponseEntity.badRequest().body(
                        com.example.be_foodgo.exception.ApiResponse.thatError(
                                400, "Vui long cung cap Firebase ID token qua header X-Firebase-Token hoac request body."));
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    com.example.be_foodgo.exception.ApiResponse.thatError(
                            400, e.getMessage()));
        }
    }
}
