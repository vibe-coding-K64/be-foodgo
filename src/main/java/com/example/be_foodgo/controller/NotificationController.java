package com.example.be_foodgo.controller;

import com.example.be_foodgo.dto.NotificationDTO;
import com.example.be_foodgo.exception.ApiResponse;
import com.example.be_foodgo.exception.BusinessException;
import com.example.be_foodgo.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/drivers/notifications")
@Tag(name = "Notifications", description = "API quan ly thong bao cua tai xe")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController extends BaseController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        super(LoggerFactory.getLogger(NotificationController.class));
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(
            summary = "Lay danh sach thong bao",
            description = "Lay danh sach tat ca thong bao cua tai xe hien tai. Ho tro loc theo query param 'type'."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lay danh sach thong bao thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chua xac thuc")
    })
    public ResponseEntity<?> getNotifications(
            HttpServletRequest httpRequest,
            @Parameter(description = "Loai thong bao: 11=Yeu cau nhan don, 12=Thong bao giao hang, 13=Don da duoc giao tai xe khac")
            @RequestParam(required = false) Integer type) {

        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            List<NotificationDTO> notifications =
                    notificationService.getNotifications(holder.userId, type);
            return ResponseEntity.ok(
                    ApiResponse.thatSuccess(notifications, "Lay danh sach thong bao thanh cong."));
        } catch (Exception e) {
            log.error("Loi khi lay danh sach thong bao: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon. Vui long thu lai sau."));
        }
    }

    @PutMapping("/{id}/read")
    @Operation(
            summary = "Danh dau da doc 1 thong bao",
            description = "Danh dau 1 thong bao cu the la da doc (isRead = true)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Danh dau da doc thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chua xac thuc"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Khong tim thay thong bao")
    })
    public ResponseEntity<?> markAsRead(
            HttpServletRequest httpRequest,
            @Parameter(description = "ID thong bao", required = true)
            @PathVariable("id") String notifId) {

        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            NotificationDTO notification =
                    notificationService.markAsRead(holder.userId, notifId);
            return ResponseEntity.ok(
                    ApiResponse.thatSuccess(notification, "Danh dau da doc thong bao thanh cong."));
        } catch (BusinessException e) {
            log.warn("Loi business khi danh dau da doc: {}", e.getMessage());
            return ResponseEntity.status(e.getStatus().value()).body(
                    ApiResponse.thatError(e.getStatus().value(), e.getMessage()));
        } catch (Exception e) {
            log.error("Loi khi danh dau da doc thong bao: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon. Vui long thu lai sau."));
        }
    }

    @PutMapping("/read-all")
    @Operation(
            summary = "Danh dau tat ca thong bao da doc",
            description = "Danh dau tat ca thong bao cua tai xe hien tai thanh da doc (isRead = true)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Danh dau tat ca thanh cong",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chua xac thuc")
    })
    public ResponseEntity<?> markAllAsRead(HttpServletRequest httpRequest) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            int count = notificationService.markAllAsRead(holder.userId);
            return ResponseEntity.ok(
                    ApiResponse.thatSuccess(count, "Danh dau tat ca thong bao thanh cong. So thong bao duoc cap nhat: " + count + "."));
        } catch (Exception e) {
            log.error("Loi khi danh dau tat ca thong bao da doc: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon. Vui long thu lai sau."));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Xoa 1 thong bao",
            description = "Xoa 1 thong bao cua tai xe khoi Firestore."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Xoa thong bao thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chua xac thuc"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Khong tim thay thong bao")
    })
    public ResponseEntity<?> deleteNotification(
            HttpServletRequest httpRequest,
            @Parameter(description = "ID thong bao", required = true)
            @PathVariable("id") String notifId) {

        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            notificationService.deleteNotification(holder.userId, notifId);
            return ResponseEntity.ok(
                    ApiResponse.thatSuccess(null, "Xoa thong bao thanh cong."));
        } catch (BusinessException e) {
            log.warn("Loi business khi xoa thong bao: {}", e.getMessage());
            return ResponseEntity.status(e.getStatus().value()).body(
                    ApiResponse.thatError(e.getStatus().value(), e.getMessage()));
        } catch (Exception e) {
            log.error("Loi khi xoa thong bao: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon. Vui long thu lai sau."));
        }
    }
}
