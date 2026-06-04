package com.example.be_foodgo.controller;

import com.example.be_foodgo.exception.ApiResponse;
import com.example.be_foodgo.service.StatsService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin Stats", description = "API thong ke he thong danh cho Admin")
@SecurityRequirement(name = "bearerAuth")
public class AdminStatsController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(AdminStatsController.class);

    private final StatsService statsService;

    public AdminStatsController(StatsService statsService) {
        super(log);
        this.statsService = statsService;
    }

    @GetMapping("/stats")
    @Operation(
            summary = "Lay thong ke he thong toan san",
            description = "Lay thong ke tong hop doanh thu, don hang, cua hang, tai xe va khach hang dang ky danh cho Admin."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lay thong ke thanh cong",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chua xac thuc"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Khong co quyen truy cap (khong phai Admin)")
    })
    public ResponseEntity<?> getSystemStats(
            HttpServletRequest httpRequest,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            Map<String, Object> stats;
            if (period != null && !period.isBlank()) {
                stats = statsService.getSystemStatsByPeriod(period, from, to);
            } else {
                stats = statsService.getSystemStats();
            }
            return ResponseEntity.ok(ApiResponse.thatSuccess(stats, "Lay thong ke he thong thanh cong."));
        } catch (Exception e) {
            log.error("Loi khi lay thong ke he thong: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon. Vui long thu lai sau."));
        }
    }

    @GetMapping("/stats/period")
    @Operation(
            summary = "Lay thong ke he thong theo khoang thoi gian",
            description = "Lay thong ke doanh thu, don hang theo period (today/week/month/custom) va so sanh voi ky truoc."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lay thong ke thanh cong",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chua xac thuc"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Khong co quyen truy cap (khong phai Admin)")
    })
    public ResponseEntity<?> getStatsByPeriod(
            HttpServletRequest httpRequest,
            @RequestParam(defaultValue = "week") String period,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            Map<String, Object> stats = statsService.getSystemStatsByPeriod(period, from, to);
            return ResponseEntity.ok(ApiResponse.thatSuccess(stats, "Lay thong ke theo period thanh cong."));
        } catch (Exception e) {
            log.error("Loi khi lay thong ke theo period: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon. Vui long thu lai sau."));
        }
    }
}
