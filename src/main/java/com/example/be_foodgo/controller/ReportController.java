package com.example.be_foodgo.controller;

import com.example.be_foodgo.exception.ApiResponse;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * ReportController - Quan ly khieu nai / bao cao cua khach hang danh cho Admin.
 * Cac khieu nai duoc luu tren Firestore collection "reports".
 */
@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private static final Logger log = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION = "reports";

    // ===================== GET ALL =====================

    @GetMapping
    public ResponseEntity<?> getAllReports(
            @RequestParam(required = false) String status) {
        try {
            Query query = firestore.collection(COLLECTION);
            if (status != null && !status.isBlank()) {
                query = query.whereEqualTo("status", status);
            }
            QuerySnapshot snapshot = query.orderBy("createdAt", Query.Direction.DESCENDING).get().get();
            List<Map<String, Object>> result = new ArrayList<>();
            for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
                Map<String, Object> data = new HashMap<>(doc.getData());
                data.put("id", doc.getId());
                cleanTimestamps(data);
                result.add(data);
            }
            return ResponseEntity.ok(ApiResponse.thatSuccess(result, "Lay danh sach khieu nai thanh cong."));
        } catch (Exception e) {
            log.error("Loi khi lay danh sach khieu nai: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.thatError(500, e.getMessage()));
        }
    }

    // ===================== GET ONE =====================

    @GetMapping("/{id}")
    public ResponseEntity<?> getReportById(@PathVariable String id) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION).document(id).get().get();
            if (!doc.exists()) {
                return ResponseEntity.status(404).body(ApiResponse.thatError(404, "Khieu nai khong ton tai."));
            }
            Map<String, Object> data = new HashMap<>(doc.getData());
            data.put("id", doc.getId());
            cleanTimestamps(data);
            return ResponseEntity.ok(ApiResponse.thatSuccess(data, "Lay chi tiet khieu nai thanh cong."));
        } catch (Exception e) {
            log.error("Loi khi lay chi tiet khieu nai {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.thatError(500, e.getMessage()));
        }
    }

    // ===================== CREATE =====================

    @PostMapping
    public ResponseEntity<?> createReport(@RequestBody Map<String, Object> body) {
        try {
            String id = UUID.randomUUID().toString();
            body.put("id", id);
            body.put("status", "open");
            body.put("createdAt", com.google.cloud.Timestamp.now());
            body.put("updatedAt", com.google.cloud.Timestamp.now());
            firestore.collection(COLLECTION).document(id).set(body).get();
            return ResponseEntity.status(201).body(ApiResponse.thatSuccess(id, "Tao khieu nai thanh cong."));
        } catch (Exception e) {
            log.error("Loi khi tao khieu nai: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.thatError(500, e.getMessage()));
        }
    }

    // ===================== UPDATE STATUS =====================

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION).document(id).get().get();
            if (!doc.exists()) {
                return ResponseEntity.status(404).body(ApiResponse.thatError(404, "Khieu nai khong ton tai."));
            }
            String newStatus = body.get("status");
            if (newStatus == null || newStatus.isBlank()) {
                return ResponseEntity.badRequest().body(ApiResponse.thatError(400, "Truong status khong duoc de trong."));
            }
            String adminNote = body.getOrDefault("adminNote", "");
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", newStatus);
            updates.put("adminNote", adminNote);
            updates.put("updatedAt", com.google.cloud.Timestamp.now());
            firestore.collection(COLLECTION).document(id).update(updates).get();
            log.info("Admin da cap nhat trang thai khieu nai {} -> {}", id, newStatus);
            return ResponseEntity.ok(ApiResponse.thatSuccess(true, "Cap nhat trang thai khieu nai thanh cong."));
        } catch (Exception e) {
            log.error("Loi khi cap nhat trang thai khieu nai {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.thatError(500, e.getMessage()));
        }
    }

    // ===================== DELETE =====================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReport(@PathVariable String id) {
        try {
            firestore.collection(COLLECTION).document(id).delete().get();
            return ResponseEntity.ok(ApiResponse.thatSuccess(true, "Xoa khieu nai thanh cong."));
        } catch (Exception e) {
            log.error("Loi khi xoa khieu nai {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.thatError(500, e.getMessage()));
        }
    }

    // ===================== UTILS =====================

    private void cleanTimestamps(Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof com.google.cloud.Timestamp) {
                entry.setValue(((com.google.cloud.Timestamp) entry.getValue()).toDate().toInstant().toString());
            } else if (entry.getValue() instanceof java.util.Date) {
                entry.setValue(((java.util.Date) entry.getValue()).toInstant().toString());
            }
        }
    }
}
