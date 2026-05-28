package com.example.be_foodgo.controller;

import com.example.be_foodgo.dto.driver.DriverDailyStatsDTO;
import com.example.be_foodgo.dto.driver.DriverStatsDTO;
import com.example.be_foodgo.exception.ApiResponse;
import com.example.be_foodgo.service.DriverStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@Tag(name = "Driver Stats", description = "API thong ke cua tai xe")
public class DriverStatsController extends BaseDriverController {

    private static final Logger log = LoggerFactory.getLogger(DriverStatsController.class);

    private final DriverStatsService driverStatsService;

    public DriverStatsController(DriverStatsService driverStatsService) {
        super(log);
        this.driverStatsService = driverStatsService;
    }

    @GetMapping("/stats")
    @Operation(
            summary = "Lay thong ke tong quan",
            description = "Lay thong ke tong quan cua tai xe: tong thu nhap, so du, tong chuyen, danh gia trung binh, thu nhap va so chuyen hom nay/thang nay."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lay thong ke thanh cong",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chua xac thuc - Token khong hop le hoac chua dang nhap")
    })
    public ResponseEntity<?> getStats(HttpServletRequest httpRequest) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            DriverStatsDTO stats = driverStatsService.getDriverStats(holder.userId);
            return ResponseEntity.ok(ApiResponse.thatSuccess(stats, "Lay thong ke thanh cong."));
        } catch (Exception e) {
            log.error("Loi khi lay thong ke: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon. Vui long thu lai sau."));
        }
    }

    @GetMapping("/stats/daily")
    @Operation(
            summary = "Lay thong ke theo ngay/thang",
            description = "Lay thong ke thu nhap va so chuyen theo tung ngay. Neu period=day thi tra ve 1 ngay, neu period=month thi tra ve tat ca cac ngay trong thang."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lay thong ke theo ngay thanh cong",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Tham so khong hop le (period phai la 'day' hoac 'month')"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chua xac thuc - Token khong hop le hoac chua dang nhap")
    })
    public ResponseEntity<?> getDailyStats(
            HttpServletRequest httpRequest,
            @RequestParam String period,
            @RequestParam String date) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            List<DriverDailyStatsDTO> stats = driverStatsService.getDriverDailyStats(holder.userId, period, date);
            return ResponseEntity.ok(ApiResponse.thatSuccess(stats, "Lay thong ke theo ngay thanh cong."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.thatError(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Loi khi lay thong ke theo ngay: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon. Vui long thu lai sau."));
        }
    }
}
