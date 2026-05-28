package com.example.be_foodgo.controller;

import com.example.be_foodgo.dto.StoreDTO;
import com.example.be_foodgo.service.OrderAssignmentService;
import com.example.be_foodgo.service.StoreService;
import com.example.be_foodgo.model.Address;
import com.example.be_foodgo.repository.AddressRepository;
import com.example.be_foodgo.repository.OrderRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stores")
@CrossOrigin(origins = "*")
public class StoreController {

    @Autowired
    private StoreService storeService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private OrderAssignmentService orderAssignmentService;

    // Lấy thông tin quán theo id
    @GetMapping("/{id}")
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
    public ResponseEntity<?> createStoreForMerchant(@PathVariable String uid, @RequestBody StoreDTO storeDTO) {
        try {
            return ResponseEntity.ok(storeService.createMerchantStore(uid, storeDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi tạo quán: " + e.getMessage());
        }
    }

    @GetMapping("/nearby")
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

    @PostMapping("/{storeId}/orders/{orderId}/confirm")
    public ResponseEntity<?> confirmOrder(
            @PathVariable String storeId,
            @PathVariable String orderId) {
        try {
            var orderData = orderRepository.findById(orderId);
            if (orderData == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Don hang khong ton tai."));
            }

            String orderStoreId = orderData.getStoreId();
            if (orderStoreId == null || !orderStoreId.equals(storeId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Don hang nay khong thuoc ve cua hang nay."));
            }

            int statusValue = orderData.getStatusValue();
            if (statusValue != 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message",
                                "Khong the xac nhan don hang. Trang thai hien tai: " + statusValue));
            }

            var store = storeService.getStoreById(storeId);
            if (store == null || store.getLat() == null || store.getLng() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Thong tin cua hang khong co toa do."));
            }

            Double deliveryLat = orderData.getDeliveryLat();
            Double deliveryLng = orderData.getDeliveryLng();
            if (deliveryLat == null || deliveryLng == null) {
                Address addr = addressRepository.layMotDiaChi(orderData.getUserId(), orderData.getAddressId());
                if (addr != null) {
                    deliveryLat = addr.getLat();
                    deliveryLng = addr.getLng();
                }
            }

            Double deliveryHeading = null;
            if (deliveryLat != null && deliveryLng != null) {
                deliveryHeading = tinhHeading(store.getLat(), store.getLng(), deliveryLat, deliveryLng);
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("status", 1);
            updates.put("deliveryHeading", deliveryHeading);
            updates.put("deliveryLat", deliveryLat != null ? deliveryLat : 0.0);
            updates.put("deliveryLng", deliveryLng != null ? deliveryLng : 0.0);
            updates.put("updatedAt", new java.util.Date());
            orderRepository.updateFields(orderId, updates);

            orderAssignmentService.batDauGánDon(
                    orderId,
                    store.getLat(),
                    store.getLng(),
                    deliveryLat,
                    deliveryLng,
                    deliveryHeading
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Xac nhan don hang thanh cong. Dang tim tai xe..."
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
