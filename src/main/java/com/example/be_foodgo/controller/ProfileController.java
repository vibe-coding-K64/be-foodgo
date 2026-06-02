package com.example.be_foodgo.controller;

import com.example.be_foodgo.dto.ChangePasswordRequest;
import com.example.be_foodgo.dto.UpdateProfileMultipartRequest;
import com.example.be_foodgo.dto.UpdateProfileRequest;
import com.example.be_foodgo.dto.UserResponse;
import com.example.be_foodgo.exception.ApiResponse;
import com.example.be_foodgo.security.JwtTokenProvider;
import com.example.be_foodgo.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "Ho so (Profile)", description = "API quan ly ho so khach hang: cap nhat thong tin ca nhan va doi mat khau")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private static final Logger log = LoggerFactory.getLogger(ProfileController.class);

    private final ProfileService profileService;
    private final JwtTokenProvider jwtTokenProvider;

    public ProfileController(ProfileService profileService, JwtTokenProvider jwtTokenProvider) {
        this.profileService = profileService;
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

    @GetMapping("/profile")
    @Operation(
            summary = "Lay thong tin ho so",
            description = "Lay thong tin ho so cua tai khoan dang nhap hien tai."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lay ho so thanh cong",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chua xac thuc - Token khong hop le hoac chua dang nhap"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Khong tim thay tai khoan")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getProfile(HttpServletRequest httpRequest) {
        try {
            String userId = trichXuatUserIdTuHeader(httpRequest);
            if (userId == null) {
                log.warn("Token xac thuc khong hop le hoac khong co token");
                return ResponseEntity.status(401).body(
                        ApiResponse.thatError(401, "Chua xac thuc. Vui long dang nhap de tiep tuc."));
            }

            log.info("Yeu cau lay ho so tu userId: {}", userId);
            UserResponse user = profileService.getProfile(userId);
            return ResponseEntity.ok(ApiResponse.thatSuccess(user, "Lay ho so thanh cong."));
        } catch (IllegalArgumentException e) {
            log.warn("Loi khi lay ho so: {}", e.getMessage());
            return ResponseEntity.status(404).body(
                    ApiResponse.thatError(404, e.getMessage()));
        } catch (Exception e) {
            log.error("Loi he thong khi lay ho so: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon. Vui long thu lai sau."));
        }
    }

    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Cap nhat thong tin ho so (multipart)",
            description = "Cap nhat ho va ten, email, va anh dai dien. password bat buoc de xac thuc."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Cap nhat ho so thanh cong",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Mat khau xac thuc khong dung hoac email da ton tai"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chua xac thuc - Token khong hop le hoac chua dang nhap"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Khong tim thay tai khoan")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> updateProfileMultipart(
            HttpServletRequest httpRequest,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,
            @RequestParam(value = "fullName", required = false) String fullName,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "password") String password) {
        try {
            String userId = trichXuatUserIdTuHeader(httpRequest);
            if (userId == null) {
                log.warn("Token xac thuc khong hop le hoac khong co token");
                return ResponseEntity.status(401).body(
                        ApiResponse.thatError(401, "Chua xac thuc. Vui long dang nhap de tiep tuc."));
            }

            log.info("Yeu cau cap nhat ho so multipart tu userId: {}", userId);

            UpdateProfileMultipartRequest request = new UpdateProfileMultipartRequest();
            request.setFullName(fullName);
            request.setEmail(email);
            request.setPassword(password);

            UserResponse updatedUser = profileService.updateProfileMultipart(userId, request, avatar);
            return ResponseEntity.ok(ApiResponse.thatSuccess(updatedUser, "Cap nhat ho so thanh cong."));
        } catch (IllegalArgumentException e) {
            log.warn("Loi khi cap nhat ho so: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.thatError(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Loi he thong khi cap nhat ho so: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon. Vui long thu lai sau."));
        }
    }

    @PutMapping("/password")
    @Operation(
            summary = "Doi mat khau chu dong",
            description = "Doi mat khau cu sang mat khau moi. Yeu cau nhap dung mat khau cu de xac nhan."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Doi mat khau thanh cong",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Mat khau cu khong dung"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chua xac thuc - Token khong hop le hoac chua dang nhap"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Khong tim thay tai khoan")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> changePassword(
            HttpServletRequest httpRequest,
            @Valid @RequestBody ChangePasswordRequest request) {
        try {
            String userId = trichXuatUserIdTuHeader(httpRequest);
            if (userId == null) {
                log.warn("Token xac thuc khong hop le hoac khong co token");
                return ResponseEntity.status(401).body(
                        ApiResponse.thatError(401, "Chua xac thuc. Vui long dang nhap de tiep tuc."));
            }

            log.info("Yeu cau doi mat khau tu userId: {}", userId);
            profileService.changePassword(userId, request);
            return ResponseEntity.ok(ApiResponse.thatSuccess(null, "Doi mat khau thanh cong."));
        } catch (IllegalArgumentException e) {
            log.warn("Loi khi doi mat khau: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.thatError(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Loi he thong khi doi mat khau: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon. Vui long thu lai sau."));
        }
    }
}
