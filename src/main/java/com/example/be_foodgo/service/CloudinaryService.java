package com.example.be_foodgo.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryService.class);
    private static final String AVATARS_FOLDER = "avatars";
    private static final String REVIEWS_FOLDER = "reviews";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final long MAX_REVIEW_IMAGE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] ALLOWED_CONTENT_TYPES = {
            "image/jpeg", "image/png", "image/gif", "image/webp"
    };

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @SuppressWarnings("unchecked")
    public String uploadAvatar(MultipartFile file, String userId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File avatar khong duoc de trong.");
        }

        validateFile(file);

        String publicId = AVATARS_FOLDER + "/" + userId + "_" + UUID.randomUUID();

        Map<String, Object> params = ObjectUtils.asMap(
                "public_id", publicId,
                "overwrite", true,
                "folder", AVATARS_FOLDER,
                "transformation", "w_500,h_500,c_fill,g_face,q_auto,f_auto"
        );

        Map<String, Object> result = cloudinary.uploader().upload(
                file.getBytes(), params);

        String url = (String) result.get("secure_url");
        log.info("Upload avatar thanh cong cho userId: {}. URL: {}", userId, url);
        return url;
    }

    @SuppressWarnings("unchecked")
    public String uploadReviewImage(MultipartFile file, String orderId, int itemIndex, int imageIndex) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File ảnh review không được để trống.");
        }

        validateReviewImageFile(file);

        String publicId = REVIEWS_FOLDER + "/review_" + orderId + "_item" + itemIndex + "_img" + imageIndex
                + "_" + UUID.randomUUID();

        Map<String, Object> params = ObjectUtils.asMap(
                "public_id", publicId,
                "overwrite", false,
                "folder", REVIEWS_FOLDER,
                "transformation", "q_auto,f_auto"
        );

        Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), params);
        String url = (String) result.get("secure_url");
        log.info("Upload review image thanh cong. OrderId: {}, ItemIndex: {}, ImageIndex: {}, URL: {}",
                orderId, itemIndex, imageIndex, url);
        return url;
    }

    @SuppressWarnings("unchecked")
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File khong duoc de trong.");
        }

        validateFile(file);

        String cleanFolder = (folder == null || folder.isBlank()) ? "general" : folder.trim();
        String suffix = UUID.randomUUID().toString();
        String publicId = cleanFolder + "/" + suffix;

        Map<String, Object> params = ObjectUtils.asMap(
                "public_id", publicId,
                "overwrite", true,
                "folder", cleanFolder,
                "transformation", "q_auto,f_auto"
        );

        Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), params);
        String secureUrl = (String) result.get("secure_url");
        log.info("Upload anh len Cloudinary thanh cong. URL: {}", secureUrl);
        return secureUrl;
    }

    private void validateReviewImageFile(MultipartFile file) {
        if (file.getSize() > MAX_REVIEW_IMAGE_SIZE) {
            throw new IllegalArgumentException("Kích thước ảnh vượt quá giới hạn 10MB.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !isAllowedContentType(contentType)) {
            throw new IllegalArgumentException("Chỉ chấp nhận các định dạng ảnh: JPEG, PNG, WEBP.");
        }
    }

    @SuppressWarnings("unchecked")
    public void deleteAvatar(String photoUrl) {
        if (photoUrl == null || photoUrl.isBlank()) {
            return;
        }

        try {
            String publicId = extractPublicIdFromUrl(photoUrl);
            if (publicId == null) {
                log.warn("Khong the trich xuat publicId tu URL: {}", photoUrl);
                return;
            }

            Map<String, Object> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            String resultStr = (String) result.get("result");
            if ("ok".equals(resultStr)) {
                log.info("Xoa avatar thanh cong: {}", publicId);
            } else {
                log.warn("Xoa avatar that bai hoac khong tim thay: {}", publicId);
            }
        } catch (Exception e) {
            log.warn("Khong the xoa avatar cu: {}", e.getMessage());
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

    private String extractPublicIdFromUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }

        try {
            String marker = "/upload/";
            int idx = url.indexOf(marker);
            if (idx < 0) {
                return null;
            }
            int start = idx + marker.length();

            String afterUpload = url.substring(start);
            if (afterUpload.matches("^v\\d+/.*")) {
                int slashIdx = afterUpload.indexOf('/');
                if (slashIdx > 0) {
                    afterUpload = afterUpload.substring(slashIdx + 1);
                }
            }

            String[] parts = afterUpload.split("/");
            StringBuilder sb = new StringBuilder();
            for (String part : parts) {
                if (part.contains("_") || part.contains(",") || part.matches("^[a-z]+$")) {
                    continue;
                }
                sb.append(part).append("/");
            }

            String result = sb.toString();
            if (result.endsWith(".jpg/") || result.endsWith(".jpeg/") ||
                    result.endsWith(".png/") || result.endsWith(".gif/") ||
                    result.endsWith(".webp/")) {
                result = result.substring(0, result.length() - 1);
                int lastSlash = result.lastIndexOf('/');
                if (lastSlash > 0) {
                    result = result.substring(0, lastSlash);
                }
            } else if (result.endsWith("/")) {
                result = result.substring(0, result.length() - 1);
            }

            if (result.startsWith(AVATARS_FOLDER + "/")) {
                result = result.substring(AVATARS_FOLDER.length() + 1);
            }

            return result.isBlank() ? null : result;
        } catch (Exception e) {
            log.warn("Loi khi trich xuat publicId tu URL: {}", e.getMessage());
            return null;
        }
    }
}
