package com.example.be_foodgo.controller;

import com.example.be_foodgo.dto.NotificationDTO;
import com.example.be_foodgo.exception.ApiResponse;
import com.example.be_foodgo.exception.BusinessException;
import com.example.be_foodgo.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admins/notifications")
@Tag(name = "Admin Notifications", description = "API quản lý thông báo của quản trị viên")
@SecurityRequirement(name = "bearerAuth")
public class AdminNotificationController extends BaseController {

    private final NotificationService notificationService;

    public AdminNotificationController(NotificationService notificationService) {
        super(LoggerFactory.getLogger(AdminNotificationController.class));
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách thông báo của admin")
    public ResponseEntity<?> getNotifications(
            HttpServletRequest httpRequest,
            @RequestParam(required = false) Integer type) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) return ResponseEntity.status(401).body(holder.errorResponse);

        try {
            List<NotificationDTO> notifications =
                    notificationService.getNotificationsByProfile("admin_profiles", holder.userId, type);
            return ResponseEntity.ok(ApiResponse.thatSuccess(notifications, "Lấy danh sách thông báo thành công."));
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách thông báo: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Đã xảy ra lỗi không mong muốn."));
        }
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Đánh dấu đã đọc 1 thông báo của admin")
    public ResponseEntity<?> markAsRead(
            HttpServletRequest httpRequest,
            @PathVariable("id") String notifId) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) return ResponseEntity.status(401).body(holder.errorResponse);

        try {
            NotificationDTO notification =
                    notificationService.markAsReadByProfile("admin_profiles", holder.userId, notifId); 
            return ResponseEntity.ok(ApiResponse.thatSuccess(notification, "Thành công."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.thatError(500, e.getMessage()));
        }
    }

    @PutMapping("/read-all")
    @Operation(summary = "Đánh dấu tất cả thông báo của admin đã đọc")
    public ResponseEntity<?> markAllAsRead(HttpServletRequest httpRequest) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) return ResponseEntity.status(401).body(holder.errorResponse);

        try {
            int count = notificationService.markAllAsReadByProfile("admin_profiles", holder.userId);
            return ResponseEntity.ok(ApiResponse.thatSuccess(count, "Đánh dấu tất cả thông báo thành công."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.thatError(500, e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa một thông báo của admin")
    public ResponseEntity<?> deleteNotification(
            HttpServletRequest httpRequest,
            @PathVariable("id") String notifId) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) return ResponseEntity.status(401).body(holder.errorResponse);

        try {
            // notificationService.deleteNotification deletes from repository. Let's make sure it handles admin profiles or is general.
            // Wait, notificationRepository.deleteNotification uses driver_profiles inside? Let's check notificationRepository.
            // Let's check if we need deleteNotificationByProfile or if deleteNotification works.
            // We'll write a profile-aware delete if needed or implement it. Let's check NotificationRepository first.
            notificationService.deleteNotificationByProfile("admin_profiles", holder.userId, notifId);
            return ResponseEntity.ok(ApiResponse.thatSuccess(null, "Xóa thông báo thành công."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.thatError(500, e.getMessage()));
        }
    }
}
