package com.example.be_foodgo.controller;

import com.example.be_foodgo.exception.ApiResponse;
import com.example.be_foodgo.service.StatsService;
import com.google.cloud.firestore.Firestore;
import io.swagger.v3.oas.annotations.Operation;
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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/merchants")
@Tag(name = "Merchant Stats", description = "API thong ke danh cho gian hang (merchant)")
@SecurityRequirement(name = "bearerAuth")
public class MerchantStatsController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(MerchantStatsController.class);

    private final StatsService statsService;
    private final Firestore firestore;

    public MerchantStatsController(StatsService statsService, Firestore firestore) {
        super(log);
        this.statsService = statsService;
        this.firestore = firestore;
    }

    @GetMapping("/stats")
    @Operation(
            summary = "Lay thong ke cua hang",
            description = "Lay thong ke doanh thu, don hang, san pham va danh gia cua cua hang."
    )
    public ResponseEntity<?> getMerchantStats(
            HttpServletRequest httpRequest,
            @RequestParam String storeId,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            // Kiem tra xem la Admin hay Merchant
            boolean isAdmin = firestore.collection("admin_profiles").document(holder.userId).get().get().exists();
            if (!isAdmin) {
                com.google.cloud.firestore.DocumentSnapshot merchantDoc = firestore.collection("merchant_profiles")
                        .document(holder.userId)
                        .get()
                        .get();
                if (!merchantDoc.exists()) {
                    return ResponseEntity.status(403).body(ApiResponse.thatError(403, "Khong tim thay ho so doi tac."));
                }
                List<String> storeIds = (List<String>) merchantDoc.get("storeIds");
                if (storeIds == null || !storeIds.contains(storeId)) {
                    return ResponseEntity.status(403).body(ApiResponse.thatError(403, "Ban khong co quyen truy cap cua hang nay."));
                }
            }

            Map<String, Object> stats;
            if (period != null && !period.isBlank()) {
                stats = statsService.getMerchantStatsByPeriod(storeId, period, from, to);
            } else {
                stats = statsService.getMerchantStats(storeId);
            }
            return ResponseEntity.ok(ApiResponse.thatSuccess(stats, "Lay thong ke cua hang thanh cong."));
        } catch (Exception e) {
            log.error("Loi khi lay thong ke merchant: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon. Vui long thu lai sau."));
        }
    }
}
