package com.example.be_foodgo.controller;

import com.example.be_foodgo.dto.DeliveryLocationUpdateRequest;
import com.example.be_foodgo.dto.DeliveryProfileDTO;
import com.example.be_foodgo.dto.DeliveryProfileRequest;
import com.example.be_foodgo.dto.DeliveryStatusRequest;
import com.example.be_foodgo.dto.DeliveryVehicleRequest;
import com.example.be_foodgo.exception.ApiResponse;
import com.example.be_foodgo.exception.BusinessException;
import com.example.be_foodgo.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/drivers")
@Tag(name = "Delivery", description = "API quan ly tai xe giao hang")
@SecurityRequirement(name = "bearerAuth")
public class DeliveryController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(DeliveryController.class);

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        super(log);
        this.deliveryService = deliveryService;
    }

    @GetMapping("/profile")
    @Operation(
            summary = "Lay thong tin ho so tai xe",
            description = "Lay thong tin ho so tai xe hien tai, bao gom thong tin ca nhan, phuong tien, va thong ke giao hang."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lay ho so tai xe thanh cong",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chua xac thuc"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Khong tim thay ho so tai xe")
    })
    public ResponseEntity<?> getProfile(HttpServletRequest httpRequest) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            DeliveryProfileDTO profile = deliveryService.getDriverProfile(holder.userId);
            return ResponseEntity.ok(ApiResponse.thatSuccess(profile, "Lay ho so tai xe thanh cong."));
        } catch (Exception e) {
            log.error("Loi khi lay ho so tai xe: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon. Vui long thu lai sau."));
        }
    }

    @PutMapping("/profile")
    @Operation(
            summary = "Cap nhat thong tin ho so tai xe",
            description = "Cap nhat thong tin ca nhan (ho ten, so dien thoai, anh dai dien) va thong tin phuong tien (bien so, loai xe, bang lai)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Cap nhat ho so tai xe thanh cong",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Du lieu khong hop le"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chua xac thuc"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Khong tim thay ho so tai xe")
    })
    public ResponseEntity<?> updateProfile(
            HttpServletRequest httpRequest,
            @Valid @RequestBody DeliveryProfileRequest request) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            DeliveryProfileDTO profile = deliveryService.updateDriverProfile(holder.userId, request);
            return ResponseEntity.ok(ApiResponse.thatSuccess(profile, "Cap nhat ho so tai xe thanh cong."));
        } catch (Exception e) {
            log.error("Loi khi cap nhat ho so tai xe: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon. Vui long thu lai sau."));
        }
    }

    @PutMapping("/status")
    @Operation(
            summary = "Cap nhat trang thai nhan don",
            description = "Bat/tat trang thai san sang nhan don cua tai xe. Khi bat (isActive=true), bat buoc phai gui kem vi tri GPS (lat, lng)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Cap nhat trang thai thanh cong",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Du lieu khong hop le"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chua xac thuc"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Khong tim thay ho so tai xe")
    })
    public ResponseEntity<?> updateStatus(
            HttpServletRequest httpRequest,
            @Valid @RequestBody DeliveryStatusRequest request) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            DeliveryProfileDTO profile;
            if (Boolean.TRUE.equals(request.getIsActive())) {
                profile = deliveryService.updateDriverStatus(holder.userId, request.getIsActive(), request);
            } else {
                profile = deliveryService.updateDriverStatus(holder.userId, request.getIsActive());
            }
            return ResponseEntity.ok(ApiResponse.thatSuccess(profile, "Cap nhat trang thai nhan don thanh cong."));
        } catch (BusinessException e) {
            log.warn("Loi business khi cap nhat trang thai nhan don: {}", e.getMessage());
            return ResponseEntity.status(e.getStatus().value()).body(
                    ApiResponse.thatError(e.getStatus().value(), e.getMessage()));
        } catch (Exception e) {
            log.error("Loi khi cap nhat trang thai nhan don: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon. Vui long thu lai sau."));
        }
    }

    @PutMapping("/vehicle")
    @Operation(
            summary = "Cap nhat thong tin phuong tien",
            description = "Cap nhat thong tin phuong tien cua tai xe, bao gom bien so xe, loai phuong tien, va bang lai xe."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Cap nhat phuong tien thanh cong",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Du lieu khong hop le"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chua xac thuc"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Khong tim thay ho so tai xe")
    })
    public ResponseEntity<?> updateVehicle(
            HttpServletRequest httpRequest,
            @Valid @RequestBody DeliveryVehicleRequest request) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            DeliveryProfileDTO profile = deliveryService.updateDriverVehicle(holder.userId, request);
            return ResponseEntity.ok(ApiResponse.thatSuccess(profile, "Cap nhat phuong tien thanh cong."));
        } catch (Exception e) {
            log.error("Loi khi cap nhat phuong tien: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon. Vui long thu lai sau."));
        }
    }

    @PostMapping("/location")
    @Operation(
            summary = "Cap nhat vi tri GPS",
            description = "Tai xe gui vi tri GPS hien tai len Realtime Database. Tai xe phai dang online moi duoc phep cap nhat vi tri."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Cap nhat vi tri thanh cong",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Du lieu GPS khong hop le hoac tai xe chua bat trang thai online"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chua xac thuc")
    })
    public ResponseEntity<?> updateLocation(
            HttpServletRequest httpRequest,
            @Valid @RequestBody DeliveryLocationUpdateRequest request) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            DeliveryProfileDTO profile = deliveryService.getDriverProfile(holder.userId);
            if (!Boolean.TRUE.equals(profile.getIsActive())) {
                return ResponseEntity.badRequest().body(
                        ApiResponse.thatError(400, "Tai xe chua bat trang thai hoat dong. Vui long bat trang thai online truoc."));
            }

            deliveryService.updateDriverLocation(
                    holder.userId,
                    request.getLat(),
                    request.getLng(),
                    request.getHeading(),
                    request.getSpeed()
            );
            return ResponseEntity.ok(ApiResponse.thatSuccess(null, "Cap nhat vi tri thanh cong."));
        } catch (Exception e) {
            log.error("Loi khi cap nhat vi tri GPS: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khi cap nhat vi tri."));
        }
    }
}
