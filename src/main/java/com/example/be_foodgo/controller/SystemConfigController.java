package com.example.be_foodgo.controller;

import com.example.be_foodgo.exception.ApiResponse;
import com.example.be_foodgo.repository.WalletRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/system-configs")
@Tag(name = "Admin System Configs", description = "API cau hinh he thong danh cho Admin")
@SecurityRequirement(name = "bearerAuth")
public class SystemConfigController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(SystemConfigController.class);

    private final WalletRepository walletRepository;

    public SystemConfigController(WalletRepository walletRepository) {
        super(log);
        this.walletRepository = walletRepository;
    }

    @GetMapping
    @Operation(
            summary = "Lay cau hinh he thong",
            description = "Tra ve tai lieu cau hinh he thong duy nhat dang duoc luu tru trong Firestore."
    )
    public ResponseEntity<?> getSystemConfig(HttpServletRequest httpRequest) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            Map<String, Object> config = walletRepository.findSystemConfig();
            if (config != null) {
                return ResponseEntity.ok(ApiResponse.thatSuccess(config, "Lay cau hinh he thong thanh cong."));
            } else {
                return ResponseEntity.ok(ApiResponse.thatError(404, "Khong tim thay tai lieu cau hinh."));
            }
        } catch (Exception e) {
            log.error("Loi khi lay cau hinh he thong: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon."));
        }
    }

    @PutMapping
    @Operation(
            summary = "Cap nhat cau hinh he thong",
            description = "Ghi de hoac tao moi tai lieu cau hinh he thong duy nhat trong Firestore."
    )
    public ResponseEntity<?> updateSystemConfig(
            HttpServletRequest httpRequest,
            @RequestBody Map<String, Object> newConfig) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            com.google.cloud.firestore.Firestore firestore = walletRepository.getFirestore();
            com.google.cloud.firestore.QuerySnapshot snapshots = firestore.collection("system_configs")
                    .limit(1)
                    .get()
                    .get();

            if (snapshots.isEmpty()) {
                firestore.collection("system_configs").add(newConfig).get();
            } else {
                String docId = snapshots.getDocuments().get(0).getId();
                firestore.collection("system_configs").document(docId).set(newConfig).get();
            }

            log.info("Admin da cap nhat cau hinh he thong thanh cong.");
            // Tra ve cau hinh da cap nhat trong truong data
            return ResponseEntity.ok(ApiResponse.thatSuccess(newConfig, "Cap nhat cau hinh he thong thanh cong."));
        } catch (Exception e) {
            log.error("Loi khi cap nhat cau hinh he thong: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon."));
        }
    }
}
