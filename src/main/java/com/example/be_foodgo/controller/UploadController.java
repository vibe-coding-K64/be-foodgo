package com.example.be_foodgo.controller;

import com.example.be_foodgo.exception.ApiResponse;
import com.example.be_foodgo.service.FirebaseStorageService;
import com.example.be_foodgo.service.CloudinaryService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class UploadController extends BaseController {

    private final FirebaseStorageService firebaseStorageService;
    private final CloudinaryService cloudinaryService;

    public UploadController(FirebaseStorageService firebaseStorageService, CloudinaryService cloudinaryService) {
        super(LoggerFactory.getLogger(UploadController.class));
        this.firebaseStorageService = firebaseStorageService;
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping("/image")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "general") String folder,
            HttpServletRequest request) {

        // Validate user authentication
        ResponseHolder holder = layUserIdHoacTraLoiLoi(request);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body((ApiResponse<Map<String, String>>) holder.errorResponse);
        }

        try {
            String url;
            try {
                url = firebaseStorageService.uploadFile(file, folder);
            } catch (Exception e) {
                log.warn("Firebase Storage upload failed, trying Cloudinary fallback. Error: {}", e.getMessage());
                url = cloudinaryService.uploadImage(file, folder);
            }
            Map<String, String> responseData = new HashMap<>();
            responseData.put("url", url);
            return ResponseEntity.ok(ApiResponse.thatSuccess(responseData, "Upload anh thanh cong."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.thatError(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Loi khi upload anh: ", e);
            return ResponseEntity.internalServerError().body(ApiResponse.thatError(500, "Loi he thong khi upload anh: " + e.getMessage()));
        }
    }

    @PostMapping("/images")
    public ResponseEntity<ApiResponse<Map<String, List<String>>>> uploadImages(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "folder", defaultValue = "general") String folder,
            HttpServletRequest request) {

        ResponseHolder holder = layUserIdHoacTraLoiLoi(request);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body((ApiResponse<Map<String, List<String>>>) holder.errorResponse);
        }

        if (files == null || files.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.thatError(400, "Danh sach file khong duoc de trong."));
        }

        try {
            List<String> urls = new ArrayList<>();
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String url;
                    try {
                        url = firebaseStorageService.uploadFile(file, folder);
                    } catch (Exception e) {
                        log.warn("Firebase Storage upload failed for file {}, trying Cloudinary fallback. Error: {}", file.getOriginalFilename(), e.getMessage());
                        url = cloudinaryService.uploadImage(file, folder);
                    }
                    urls.add(url);
                }
            }
            Map<String, List<String>> responseData = new HashMap<>();
            responseData.put("urls", urls);
            return ResponseEntity.ok(ApiResponse.thatSuccess(responseData, "Upload danh sach anh thanh cong."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.thatError(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Loi khi upload nhieu anh: ", e);
            return ResponseEntity.internalServerError().body(ApiResponse.thatError(500, "Loi he thong khi upload anh: " + e.getMessage()));
        }
    }
}
