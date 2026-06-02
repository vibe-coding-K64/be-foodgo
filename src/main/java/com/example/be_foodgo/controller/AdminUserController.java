package com.example.be_foodgo.controller;

import com.example.be_foodgo.dto.UserResponse;
import com.example.be_foodgo.exception.ApiResponse;
import com.example.be_foodgo.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "Admin User Management", description = "API quan ly nguoi dung danh cho Admin")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(AdminUserController.class);

    private final ProfileService profileService;

    public AdminUserController(ProfileService profileService) {
        super(log);
        this.profileService = profileService;
    }

    @GetMapping
    @Operation(
            summary = "Lay danh sach toan bo nguoi dung",
            description = "Tra ve danh sach toan bo nguoi dung trong he thong, ho tro filter theo role (1=Khach hang, 2=Tai xe, 3=Merchant, 4=Admin)."
    )
    public ResponseEntity<?> getAllUsers(
            HttpServletRequest httpRequest,
            @RequestParam(required = false) Integer role) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            List<UserResponse> users = profileService.timTatCaUsers(role);
            return ResponseEntity.ok(users); // Flutter mong muon nhan list truc tiep
        } catch (Exception e) {
            log.error("Loi khi lay danh sach nguoi dung: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon."));
        }
    }

    @PostMapping("/{id}/toggle-active")
    @Operation(
            summary = "Khoa hoac mo khoa tai khoan",
            description = "Dao nguoc trang thai active (isActive) cua nguoi dung."
    )
    public ResponseEntity<?> toggleUserActive(
            HttpServletRequest httpRequest,
            @PathVariable("id") String userId) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            boolean currentStatus = profileService.toggleUserActive(userId);
            return ResponseEntity.ok(currentStatus); // Tra ve boolean truc tiep nhu Flutter mong doi
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.thatError(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Loi khi thay doi trang thai active: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon."));
        }
    }
}
