package com.example.be_foodgo.controller;

import com.example.be_foodgo.service.FirebaseResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/firebase")
@Tag(name = "Firebase Dev", description = "API dev cho Firebase - chi su dung trong moi truong phat trien")
public class FirebaseResetController {

    @Autowired
    private FirebaseResetService firebaseResetService;

    @PostMapping("/reset")
    @Operation(
            summary = "Reset Firebase data",
            description = "Xoa tat ca du lieu hien tai tren Firebase va seed lai tu dau. Chi nen su dung trong moi truong dev."
    )
    public ResponseEntity<Map<String, Object>> resetFirebase() {
        Map<String, Object> result = firebaseResetService.resetFirebase();
        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.internalServerError().body(result);
    }

    @DeleteMapping("/data")
    @Operation(
            summary = "Delete all Firebase data",
            description = "Xoa tat ca du lieu hien tai tren Firebase (khong reseed). Chi nen su dung trong moi truong dev."
    )
    public ResponseEntity<Map<String, Object>> deleteAllData() {
        Map<String, Object> result = firebaseResetService.clearAllCollectionsOnly();
        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.internalServerError().body(result);
    }
}
