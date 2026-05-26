package com.example.be_foodgo.controller;

import com.example.be_foodgo.dto.StoreDTO;
import com.example.be_foodgo.service.StoreService;
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
}
