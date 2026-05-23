package com.example.be_foodgo.controller;

import com.example.be_foodgo.dto.AuthRequestDTO;
import com.example.be_foodgo.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register-merchant")
    public ResponseEntity<?> registerMerchant(@RequestBody AuthRequestDTO request) {
        try {
            return ResponseEntity.ok(authService.registerMerchant(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi đăng ký: " + e.getMessage());
        }
    }

    @GetMapping("/check-merchant")
    public ResponseEntity<?> checkMerchantRole(@RequestParam String uid) {
        try {
            return ResponseEntity.ok(authService.checkMerchantProfile(uid));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi kiểm tra quyền: " + e.getMessage());
        }
    }
}
