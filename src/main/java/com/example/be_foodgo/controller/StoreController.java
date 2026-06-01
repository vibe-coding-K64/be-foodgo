package com.example.be_foodgo.controller;

import com.example.be_foodgo.dto.StoreDTO;
import com.example.be_foodgo.exception.ApiResponse;
import com.example.be_foodgo.security.JwtTokenProvider;
import com.example.be_foodgo.service.OrderAssignmentService;
import com.example.be_foodgo.service.StoreService;
import com.example.be_foodgo.model.Address;
import com.example.be_foodgo.repository.AddressRepository;
import com.example.be_foodgo.repository.OrderRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

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
    private AddressRepository addressRepository;

    @Autowired
    private OrderAssignmentService orderAssignmentService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String trichXuatUserIdTuHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        return jwtTokenProvider.layUserIdTuToken(token);
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
            @RequestParam(defaultValue = "0") double minRating) {
        try {
            Map<String, Object> response = storeService.getPopularStores(limit, categoryId, minRating);
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

    private double tinhHeading(double fromLat, double fromLng, double toLat, double toLng) {
        double dLng = Math.toRadians(toLng - fromLng);
        double lat1 = Math.toRadians(fromLat);
        double lat2 = Math.toRadians(toLat);
        double x = Math.sin(dLng) * Math.cos(lat2);
        double y = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLng);
        double heading = Math.toDegrees(Math.atan2(x, y));
        return (heading + 360.0) % 360.0;
    }
}
