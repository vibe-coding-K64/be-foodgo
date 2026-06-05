package com.example.be_foodgo.controller;

import com.example.be_foodgo.dto.FCMTokenRequest;
import com.example.be_foodgo.exception.ApiResponse;
import com.example.be_foodgo.service.FCMService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fcm")
@Tag(name = "FCM Notification", description = "API đăng ký và cấu hình Firebase Cloud Messaging")
@SecurityRequirement(name = "bearerAuth")
public class FCMController extends BaseController {

    private final FCMService fcmService;

    public FCMController(FCMService fcmService) {
        super(LoggerFactory.getLogger(FCMController.class));
        this.fcmService = fcmService;
    }

    @PostMapping("/register")
    @Operation(summary = "Đăng ký hoặc cập nhật FCM token cho tài khoản hiện tại")
    public ResponseEntity<?> registerToken(
            HttpServletRequest httpRequest,
            @Valid @RequestBody FCMTokenRequest request) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) return ResponseEntity.status(401).body(holder.errorResponse);

        try {
            fcmService.registerFCMToken(holder.userId, request.getFcmToken());
            return ResponseEntity.ok(ApiResponse.thatSuccess(null, "Đăng ký FCM token thành công."));
        } catch (Exception e) {
            log.error("Lỗi khi đăng ký FCM token cho userId={}: {}", holder.userId, e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Đã xảy ra lỗi khi đăng ký FCM token."));
        }
    }
}
