package com.example.be_foodgo.controller;

import com.example.be_foodgo.dto.driver.DriverLocationUpdateRequest;
import com.example.be_foodgo.dto.driver.DriverProfileDTO;
import com.example.be_foodgo.dto.driver.DriverUpdateProfileRequest;
import com.example.be_foodgo.dto.driver.DriverUpdateStatusRequest;
import com.example.be_foodgo.dto.driver.DriverUpdateVehicleRequest;
import com.example.be_foodgo.exception.ApiResponse;
import com.example.be_foodgo.service.DriverProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Drivers", description = "API quan ly tai xe (Driver)")
public class DriverController extends BaseDriverController {

    private static final Logger log = LoggerFactory.getLogger(DriverController.class);

    private final DriverProfileService driverProfileService;

    public DriverController(DriverProfileService driverProfileService) {
        super(log);
        this.driverProfileService = driverProfileService;
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
                    description = "Chua xac thuc - Token khong hop le hoac chua dang nhap"),
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
            DriverProfileDTO profile = driverProfileService.getDriverProfile(holder.userId);
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
                    description = "Chua xac thuc - Token khong hop le hoac chua dang nhap"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Khong tim thay ho so tai xe")
    })
    public ResponseEntity<?> updateProfile(
            HttpServletRequest httpRequest,
            @Valid @RequestBody DriverUpdateProfileRequest request) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            DriverProfileDTO profile = driverProfileService.updateDriverProfile(holder.userId, request);
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
            description = "Bat/tat trang thai san sang nhan don cua tai xe. Khi tat (isActive=false), tai xe se bi xoa khoi danh sach active_drivers trong Realtime Database."
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
                    description = "Chua xac thuc - Token khong hop le hoac chua dang nhap"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Khong tim thay ho so tai xe")
    })
    public ResponseEntity<?> updateStatus(
            HttpServletRequest httpRequest,
            @Valid @RequestBody DriverUpdateStatusRequest request) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            DriverProfileDTO profile = driverProfileService.updateDriverStatus(holder.userId, request.getIsActive());
            return ResponseEntity.ok(ApiResponse.thatSuccess(profile, "Cap nhat trang thai nhan don thanh cong."));
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
                    description = "Chua xac thuc - Token khong hop le hoac chua dang nhap"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Khong tim thay ho so tai xe")
    })
    public ResponseEntity<?> updateVehicle(
            HttpServletRequest httpRequest,
            @Valid @RequestBody DriverUpdateVehicleRequest request) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            DriverProfileDTO profile = driverProfileService.updateDriverVehicle(holder.userId, request);
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
            description = "Tai xe gui vi tri GPS hien tai len Realtime Database. Duoc goi lien tuc moi 3-5 giay khi dang giao hang. Khong blocking - tra ket qua ngay khi du lieu duoc queue, khong cho doi xac nhan tu Realtime Database."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Cap nhat vi tri thanh cong",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Du lieu GPS khong hop le"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chua xac thuc - Token khong hop le hoac chua dang nhap")
    })
    public ResponseEntity<?> updateLocation(
            HttpServletRequest httpRequest,
            @Valid @RequestBody DriverLocationUpdateRequest request) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            driverProfileService.updateDriverLocation(
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
