package com.example.be_foodgo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.be_foodgo.dto.StoreDTO;
import com.example.be_foodgo.repository.OrderRepository;
import com.example.be_foodgo.security.JwtTokenProvider;
import com.example.be_foodgo.service.OrderAssignmentService;
import com.example.be_foodgo.service.StoreService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/stores")
@CrossOrigin(origins = "*")
@Tag(name = "Store Management", description = "Quản lý thông tin cửa hàng")
public class StoreController {

    private static final Logger log = LoggerFactory.getLogger(StoreController.class);

    @Autowired
    private StoreService storeService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private OrderAssignmentService orderAssignmentService;

    private String trichXuatUserIdTuHeader(HttpServletRequest request) {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            String name = auth.getName();
            if (name.startsWith("firebase:")) {
                return name.substring(9);
            }
            return name;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        try {
            return jwtTokenProvider.layUserIdTuToken(token);
        } catch (Exception e) {
            return null;
        }
    }

    // Lấy toàn bộ danh sách cửa hàng
    @GetMapping
    @Operation(summary = "Lấy toàn bộ danh sách cửa hàng", description = "Trả về danh sách tất cả các cửa hàng trên hệ thống")
    public ResponseEntity<?> getAllStores() {
        try {
            return ResponseEntity.ok(storeService.getAllStores());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }

    // Lấy thông tin quán theo id
    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin cửa hàng", description = "Trả về thông tin chi tiết của cửa hàng dựa trên ID")
    public ResponseEntity<StoreDTO> getStore(@PathVariable String id) {
        try {
            StoreDTO store = storeService.getStoreById(id);
            return ResponseEntity.ok(store);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Cập nhật thông tin quán
    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cập nhật thông tin cửa hàng", description = "Cập nhật các thông tin cơ bản của cửa hàng")
    public ResponseEntity<?> updateStore(@PathVariable String id, @Valid @RequestBody StoreDTO storeDTO) {
        try {
            StoreDTO updatedStore = storeService.updateStore(id, storeDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cập nhật thông tin quán thành công");
            response.put("data", updatedStore);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/merchant/{uid}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Tạo cửa hàng mới cho merchant", description = "Tạo một cửa hàng mới và gán cho tài khoản merchant")
    public ResponseEntity<?> createStoreForMerchant(@PathVariable String uid, @RequestBody StoreDTO storeDTO) {
        try {
            return ResponseEntity.ok(storeService.createMerchantStore(uid, storeDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi tạo quán: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/approve")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Duyệt cửa hàng", description = "Admin duyệt cửa hàng, cập nhật approvalStatus = approved và mở cửa hàng")
    public ResponseEntity<?> approveStore(@PathVariable String id) {
        try {
            storeService.approveStore(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Da duyet cua hang thanh cong.", "storeId", id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Loi khi duyet cua hang {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Da xay ra loi khi duyet cua hang."));
        }
    }

    @PostMapping("/{id}/reject")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Từ chối cửa hàng", description = "Admin từ chối cửa hàng kèm lý do, cập nhật approvalStatus = rejected")
    public ResponseEntity<?> rejectStore(@PathVariable String id, @RequestBody Map<String, String> body) {
        try {
            String reason = body != null ? body.get("reason") : null;
            storeService.rejectStore(id, reason);
            return ResponseEntity.ok(Map.of("success", true, "message", "Da tu choi cua hang.", "storeId", id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Loi khi tu choi cua hang {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Da xay ra loi khi tu choi cua hang."));
        }
    }

    @PostMapping("/{id}/lock")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Tạm khóa cửa hàng", description = "Admin tạm khóa cửa hàng kèm lý do")
    public ResponseEntity<?> lockStore(@PathVariable String id, @RequestBody Map<String, String> body) {
        try {
            String reason = body != null ? body.get("reason") : null;
            storeService.lockStore(id, reason);
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã tạm khóa cửa hàng.", "storeId", id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Loi khi tam khoa cua hang {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Đã xảy ra lỗi khi tạm khóa cửa hàng."));
        }
    }

    @PostMapping("/{id}/unlock")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Mở khóa cửa hàng", description = "Admin mở khóa cửa hàng")
    public ResponseEntity<?> unlockStore(@PathVariable String id) {
        try {
            storeService.unlockStore(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã mở khóa cửa hàng thành công.", "storeId", id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Loi khi mo khoa cua hang {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Đã xảy ra lỗi khi mở khóa cửa hàng."));
        }
    }

    @GetMapping("/nearby")
    @Operation(summary = "Lấy danh sách cửa hàng lân cận", description = "Tìm các cửa hàng trong bán kính cho trước")
    public ResponseEntity<Map<String, Object>> getNearbyStores(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5000") double radius,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String categoryId) {
        try {
            Map<String, Object> response = storeService.getNearbyStores(lat, lng, radius, limit, categoryId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/popular")
    @Operation(summary = "Lấy danh sách cửa hàng phổ biến", description = "Lấy danh sách cửa hàng có đánh giá cao")
    public ResponseEntity<Map<String, Object>> getPopularStores(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String categoryId,
            @RequestParam(defaultValue = "0") double minRating,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng) {
        try {
            Map<String, Object> response = storeService.getPopularStores(limit, categoryId, minRating, lat, lng);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/orders/{orderId}/confirm")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Xác nhận đơn hàng", description = "Merchant xác nhận đơn hàng của cửa hàng thông qua token")
    public ResponseEntity<?> confirmOrder(
            HttpServletRequest httpRequest,
            @PathVariable String orderId) {
        try {
            String userId = trichXuatUserIdTuHeader(httpRequest);
            if (userId == null) {
                log.warn("Token xac thuc khong hop le hoac khong co token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Chua xac thuc. Vui long dang nhap de tiep tuc."));
            }

            List<String> storeIds = storeService.getStoreIdsByMerchantId(userId);
            if (storeIds.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Tai khoan nay chua co cua hang nao."));
            }

            var orderData = orderRepository.findById(orderId);
            if (orderData == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Don hang khong ton tai."));
            }

            String orderStoreId = orderData.getStoreId();
            if (orderStoreId == null || !storeIds.contains(orderStoreId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Don hang nay khong thuoc ve cua hang cua ban."));
            }

            int statusValue = orderData.getStatusValue();
            if (statusValue != 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message",
                                "Khong the xac nhan don hang. Trang thai hien tai: " + statusValue));
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("status", 1);
            updates.put("updatedAt", new java.util.Date());
            orderRepository.updateFields(orderId, updates);
            orderAssignmentService.triggerAssignmentForOrder(orderId);

            log.info("Merchant {} xac nhan don hang {} thanh cong. Store: {}", userId, orderId, orderStoreId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Xac nhan don hang thanh cong.",
                    "orderId", orderId,
                    "storeId", orderStoreId,
                    "status", 1
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

}
