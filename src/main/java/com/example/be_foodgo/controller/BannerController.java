package com.example.be_foodgo.controller;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/banners")
@CrossOrigin(origins = "*")
public class BannerController {

    @Autowired
    private Firestore firestore;

    @GetMapping
    public ResponseEntity<?> getBanners() {
        try {
            QuerySnapshot snapshots = firestore.collection("banners")
                    .get()
                    .get();
            List<Map<String, Object>> result = new ArrayList<>();
            for (QueryDocumentSnapshot doc : snapshots.getDocuments()) {
                Map<String, Object> data = new HashMap<>(doc.getData());
                data.put("id", doc.getId());
                cleanTimestamps(data);
                result.add(data);
            }
            // Sắp xếp theo order tăng dần
            result.sort((a, b) -> {
                int oa = toInt(a.get("order"));
                int ob = toInt(b.get("order"));
                return Integer.compare(oa, ob);
            });
            return ResponseEntity.ok(com.example.be_foodgo.exception.ApiResponse.thatSuccess(result, "Lay danh sach banner thanh cong"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(com.example.be_foodgo.exception.ApiResponse.thatError(500, e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBannerById(@PathVariable String id) {
        try {
            DocumentSnapshot doc = firestore.collection("banners").document(id).get().get();
            if (!doc.exists()) {
                return ResponseEntity.status(404).body(com.example.be_foodgo.exception.ApiResponse.thatError(404, "Banner not found"));
            }
            Map<String, Object> data = new HashMap<>(doc.getData());
            data.put("id", doc.getId());
            cleanTimestamps(data);
            return ResponseEntity.ok(com.example.be_foodgo.exception.ApiResponse.thatSuccess(data, "Lay chi tiet banner thanh cong"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(com.example.be_foodgo.exception.ApiResponse.thatError(500, e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createBanner(@RequestBody Map<String, Object> bannerData) {
        try {
            String id = (String) bannerData.get("id");
            if (id == null || id.trim().isEmpty()) {
                id = UUID.randomUUID().toString();
                bannerData.put("id", id);
            }
            bannerData.put("createdAt", com.google.cloud.Timestamp.now());
            bannerData.put("updatedAt", com.google.cloud.Timestamp.now());
            firestore.collection("banners").document(id).set(bannerData).get();
            return ResponseEntity.status(201).body(com.example.be_foodgo.exception.ApiResponse.thatSuccess(id, "Tao banner thanh cong"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(com.example.be_foodgo.exception.ApiResponse.thatError(500, e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBanner(@PathVariable String id, @RequestBody Map<String, Object> bannerData) {
        try {
            bannerData.put("id", id);
            bannerData.put("updatedAt", com.google.cloud.Timestamp.now());
            
            // Loại bỏ createdAt để tránh update đè
            bannerData.remove("createdAt");
            
            firestore.collection("banners").document(id).update(bannerData).get();
            return ResponseEntity.ok(com.example.be_foodgo.exception.ApiResponse.thatSuccess(true, "Cap nhat banner thanh cong"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(com.example.be_foodgo.exception.ApiResponse.thatError(500, e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBanner(@PathVariable String id) {
        try {
            firestore.collection("banners").document(id).delete().get();
            return ResponseEntity.ok(com.example.be_foodgo.exception.ApiResponse.thatSuccess(true, "Xoa banner thanh cong"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(com.example.be_foodgo.exception.ApiResponse.thatError(500, e.getMessage()));
        }
    }

    private void cleanTimestamps(Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof com.google.cloud.Timestamp) {
                entry.setValue(((com.google.cloud.Timestamp) entry.getValue()).toDate().toInstant().toString());
            } else if (entry.getValue() instanceof java.util.Date) {
                entry.setValue(((java.util.Date) entry.getValue()).toInstant().toString());
            }
        }
    }

    private int toInt(Object val) {
        if (val == null) return 0;
        if (val instanceof Number) return ((Number) val).intValue();
        if (val instanceof String) {
            try {
                return Integer.parseInt((String) val);
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }
}
