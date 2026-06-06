package com.example.be_foodgo.controller;

import com.example.be_foodgo.service.CloudinaryService;
import com.example.be_foodgo.exception.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final CloudinaryService cloudinaryService;

    public UploadController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<String>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "general") String folder) {
        try {
            String url = cloudinaryService.uploadGenericImage(file, folder);
            return ResponseEntity.ok(ApiResponse.thatSuccess(url, "Upload ảnh thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.thatError(400, "Lỗi upload: " + e.getMessage()));
        }
    }
}
