package com.example.be_foodgo.controller;

import com.example.be_foodgo.dto.DriverProfileResponse;
import com.example.be_foodgo.dto.UpdateDriverProfileRequest;
import com.example.be_foodgo.exception.ApiResponse;
import com.example.be_foodgo.security.JwtTokenProvider;
import com.example.be_foodgo.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/drivers")
@Tag(name = "Ho so tai xe (Driver Profile)", description = "API quan ly ho so tai xe: xem va cap nhat thong tin ca nhan")
@SecurityRequirement(name = "bearerAuth")
public class DriverProfileController {

    private static final Logger log = LoggerFactory.getLogger(DriverProfileController.class);

    private final StoreService storeService;
    private final JwtTokenProvider jwtTokenProvider;

    public DriverProfileController(StoreService storeService, JwtTokenProvider jwtTokenProvider) {
        this.storeService = storeService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    private String trichXuatUserIdTuHeader(HttpServletRequest request) {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return auth.getName();
        }
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        try {
            return jwtTokenProvider.layUserIdTuToken(token);
        } catch (Exception e) {
            log.warn("Loi trich xuat userId tu token: {}", e.getMessage());
            return null;
        }
    }

    @PutMapping(value = "/profile/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Cap nhat anh dai dien tai xe",
            description = "Upload anh dai dien tai xe len Cloudinary va cap nhat driver_profile."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Cap nhat anh thanh cong",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "File khong hop le"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chua xac thuc"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Khong tim thay tai xe")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> updateDriverAvatar(
            HttpServletRequest httpRequest,
            @RequestParam("avatar") MultipartFile avatar) {
        try {
            String userId = trichXuatUserIdTuHeader(httpRequest);
            if (userId == null) {
                log.warn("Token xac thuc khong hop le hoac khong co token");
                return ResponseEntity.status(401).body(
                        ApiResponse.thatError(401, "Chua xac thuc. Vui long dang nhap de tiep tuc."));
            }

            log.info("Yeu cau cap nhat avatar tai xe tu userId: {}", userId);

            String newPhotoUrl = storeService.uploadDriverAvatar(userId, avatar);

            Map<String, Object> result = new HashMap<>();
            result.put("photoUrl", newPhotoUrl);
            return ResponseEntity.ok(ApiResponse.thatSuccess(result, "Cap nhat anh dai dien thanh cong."));
        } catch (IllegalArgumentException e) {
            log.warn("Loi khi cap nhat avatar tai xe: {}", e.getMessage());
            return ResponseEntity.status(404).body(
                    ApiResponse.thatError(404, e.getMessage()));
        } catch (Exception e) {
            log.error("Loi he thong khi cap nhat avatar tai xe: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon. Vui long thu lai sau."));
        }
    }
}
