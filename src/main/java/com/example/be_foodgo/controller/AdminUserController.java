package com.example.be_foodgo.controller;

import com.example.be_foodgo.dto.UserResponse;
import com.example.be_foodgo.dto.DeliveryProfileDTO;
import com.example.be_foodgo.exception.ApiResponse;
import com.example.be_foodgo.model.AdminProfile;
import com.example.be_foodgo.service.ProfileService;
import com.example.be_foodgo.service.DeliveryService;
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
    private final DeliveryService deliveryService;

    public AdminUserController(ProfileService profileService, DeliveryService deliveryService) {
        super(log);
        this.profileService = profileService;
        this.deliveryService = deliveryService;
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

    @PostMapping("/create-admin")
    @Operation(summary = "Tao tai khoan Admin moi", description = "Tao moi mot user co role Admin (4) va khoi tao AdminProfile.")
    public ResponseEntity<?> createAdmin(
            HttpServletRequest httpRequest,
            @RequestBody CreateAdminRequest request) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            UserResponse user = profileService.createAdminUser(
                    request.email,
                    request.password,
                    request.fullName,
                    request.phoneNumber,
                    request.department,
                    request.adminLevel,
                    request.permissions
            );
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.thatError(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Loi khi tao admin: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.thatError(500, "Da xay ra loi: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/roles")
    @Operation(summary = "Cap nhat vai tro (roles) cua nguoi dung", description = "Cap nhat danh sach roles cho nguoi dung.")
    public ResponseEntity<?> updateUserRoles(
            HttpServletRequest httpRequest,
            @PathVariable("id") String userId,
            @RequestBody UpdateUserRolesRequest request) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            profileService.updateUserRoles(userId, request.roles);
            return ResponseEntity.ok(ApiResponse.thatSuccess(null, "Cap nhat vai tro thanh cong."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.thatError(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Loi khi cap nhat vai tro: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.thatError(500, "Da xay ra loi."));
        }
    }

    @GetMapping("/{id}/admin-profile")
    @Operation(summary = "Lay thong tin ho so phan quyen Admin", description = "Lay thong tin AdminProfile cua nguoi dung.")
    public ResponseEntity<?> getAdminProfile(
            HttpServletRequest httpRequest,
            @PathVariable("id") String userId) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            AdminProfile profile = profileService.getAdminProfile(userId);
            return ResponseEntity.ok(profile);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.thatError(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Loi khi lay ho so admin: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.thatError(500, "Da xay ra loi."));
        }
    }

    @PutMapping("/{id}/admin-profile")
    @Operation(summary = "Cap nhat ho so phan quyen Admin", description = "Cap nhat thong tin AdminProfile.")
    public ResponseEntity<?> updateAdminProfile(
            HttpServletRequest httpRequest,
            @PathVariable("id") String userId,
            @RequestBody UpdateAdminProfileRequest request) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            profileService.updateAdminProfile(
                    userId,
                    request.department,
                    request.adminLevel,
                    request.permissions
            );
            return ResponseEntity.ok(ApiResponse.thatSuccess(null, "Cap nhat ho so phan quyen thanh cong."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.thatError(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Loi khi cap nhat ho so admin: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.thatError(500, "Da xay ra loi."));
        }
    }

    @GetMapping("/{id}/driver-profile")
    @Operation(summary = "Lay thong tin ho so tai xe", description = "Lay thong tin DriverProfile va thong tin phuong tien cua tai xe.")
    public ResponseEntity<?> getDriverProfile(
            HttpServletRequest httpRequest,
            @PathVariable("id") String userId) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            DeliveryProfileDTO profile = deliveryService.getDriverProfile(userId);
            return ResponseEntity.ok(profile);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.thatError(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Loi khi lay ho so tai xe {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.thatError(500, "Da xay ra loi."));
        }
    }

    public static class CreateAdminRequest {
        public String email;
        public String password;
        public String fullName;
        public String phoneNumber;
        public String department;
        public Integer adminLevel;
        public List<String> permissions;
    }

    public static class UpdateUserRolesRequest {
        public List<Integer> roles;
    }

    public static class UpdateAdminProfileRequest {
        public String department;
        public Integer adminLevel;
        public List<String> permissions;
    }
}
