package com.example.be_foodgo.service;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Acl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class FirebaseStorageService {

    private static final Logger log = LoggerFactory.getLogger(FirebaseStorageService.class);
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_CONTENT_TYPES = {
            "image/jpeg", "image/png", "image/gif", "image/webp"
    };

    private final Storage storage;
    private final String bucketName;

    public FirebaseStorageService(Storage storage, @Qualifier("firebaseStorageBucket") String bucketName) {
        this.storage = storage;
        this.bucketName = bucketName;
    }

    public String uploadFile(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File khong duoc de trong.");
        }

        validateFile(file);

        if (bucketName == null || bucketName.isBlank()) {
            throw new IllegalStateException("Firebase Storage bucket chua duoc cau hinh.");
        }

        // Clean folder name
        String folderPath = (folder == null || folder.isBlank()) ? "uploads" : folder.trim().replaceAll("[^a-zA-Z0-9/_-]", "");
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        String fileName = folderPath + "/" + UUID.randomUUID() + extension;
        BlobId blobId = BlobId.of(bucketName, fileName);

        log.info("Bat dau upload file lên Firebase Storage. Bucket: {}, Path: {}", bucketName, fileName);

        // Upload to Cloud Storage
        BlobInfo blobInfo;
        try {
            // Option 1: Try uploading with public ACL (allUsers:READER)
            blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .setAcl(new ArrayList<>(List.of(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER))))
                    .build();
            storage.create(blobInfo, file.getBytes());
            
            // Standard public URL
            String publicUrl = "https://storage.googleapis.com/" + bucketName + "/" + fileName;
            log.info("Upload thành công voi public ACL. URL: {}", publicUrl);
            return publicUrl;
        } catch (Exception e) {
            log.warn("Khong the set public ACL (co the Uniform Bucket-level Access duoc bat). Thu fallback bang Signed URL. Loi: {}", e.getMessage());
            
            // Option 2: Fallback without ACL, and generate long-lived Signed URL (100 years)
            blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();
            storage.create(blobInfo, file.getBytes());
            
            URL signedUrl = storage.signUrl(blobInfo, 36500, TimeUnit.DAYS);
            String urlStr = signedUrl.toString();
            log.info("Upload thành công voi Signed URL. URL: {}", urlStr);
            return urlStr;
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Kich thuoc file vuot qua gioi han 5MB.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !isAllowedContentType(contentType)) {
            throw new IllegalArgumentException("Chi chap nhan cac dinh dang anh: JPEG, PNG, GIF, WEBP.");
        }
    }

    private boolean isAllowedContentType(String contentType) {
        for (String allowed : ALLOWED_CONTENT_TYPES) {
            if (allowed.equalsIgnoreCase(contentType)) {
                return true;
            }
        }
        return false;
    }
}
