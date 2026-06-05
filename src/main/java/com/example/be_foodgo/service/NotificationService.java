package com.example.be_foodgo.service;

import com.example.be_foodgo.dto.NotificationDTO;
import com.example.be_foodgo.exception.BusinessException;
import com.example.be_foodgo.repository.NotificationRepository;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.WriteBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final Firestore firestore;

    public NotificationService(NotificationRepository notificationRepository, Firestore firestore) {
        this.notificationRepository = notificationRepository;
        this.firestore = firestore;
    }

    public List<NotificationDTO> getNotifications(String driverId, Integer type) throws Exception {
        return getNotificationsByProfile("driver_profiles", driverId, type);
    }

    public List<NotificationDTO> getNotificationsByProfile(String profileCollection, String profileId, Integer type) throws Exception {
        Query query = firestore.collection(profileCollection)
                .document(profileId)
                .collection("notifications");

        if (type != null) {
            query = query.whereEqualTo("type", type);
        }

        query = query.orderBy("createdAt", Query.Direction.DESCENDING).limit(50);

        List<QueryDocumentSnapshot> docs = query.get().get().getDocuments();
        List<NotificationDTO> notifications = new ArrayList<>();
        for (QueryDocumentSnapshot doc : docs) {
            notifications.add(mapToDTO(doc.getId(), doc.getData()));
        }

        log.info("Tìm thấy {} thông báo cho profileId={}", notifications.size(), profileId);
        return notifications;
    }

    public NotificationDTO markAsRead(String userId, String notifId) {
        log.info("Bắt đầu đánh dấu đã đọc thông báo: userId={}, notifId={}", userId, notifId);
        try {
            com.google.cloud.firestore.DocumentSnapshot doc =
                    notificationRepository.findNotificationById(userId, notifId);

            if (!doc.exists()) {
                throw BusinessException.thongBaoKhongTimThay(notifId);
            }

            notificationRepository.updateNotificationRead(userId, notifId);

            Map<String, Object> updatedData = doc.getData();
            if (updatedData == null) {
                updatedData = new HashMap<>();
            }
            updatedData.put("isRead", true);

            log.info("Đánh dấu đã đọc thông báo thành công: userId={}, notifId={}", userId, notifId);
            return mapToDTO(doc.getId(), updatedData);
        } catch (BusinessException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Lỗi khi đánh dấu đã đọc thông báo: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        } catch (java.util.concurrent.ExecutionException e) {
            log.error("Lỗi khi đánh dấu đã đọc thông báo: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        }
    }

    public NotificationDTO markAsReadByProfile(String profileCollection, String profileId, String notifId) {
        log.info("Bắt đầu đánh dấu đã đọc thông báo: collection={}, profileId={}, notifId={}", profileCollection, profileId, notifId);
        try {
            com.google.cloud.firestore.DocumentReference docRef = firestore
                    .collection(profileCollection)
                    .document(profileId)
                    .collection("notifications")
                    .document(notifId);
                    
            com.google.cloud.firestore.DocumentSnapshot doc = docRef.get().get();

            if (!doc.exists()) {
                throw BusinessException.thongBaoKhongTimThay(notifId);
            }

            docRef.update("isRead", true).get();

            Map<String, Object> updatedData = doc.getData();
            if (updatedData == null) {
                updatedData = new HashMap<>();
            }
            updatedData.put("isRead", true);
            
            log.info("Đánh dấu đã đọc thông báo thành công: profileId={}, notifId={}", profileId, notifId);
            return mapToDTO(doc.getId(), updatedData);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Lỗi khi đánh dấu đã đọc thông báo: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        }
    }

    public int markAllAsRead(String driverId) throws Exception {
        return markAllAsReadByProfile("driver_profiles", driverId);
    }

    public int markAllAsReadByProfile(String profileCollection, String profileId) throws Exception {
        CollectionReference notifsRef = firestore.collection(profileCollection)
                .document(profileId)
                .collection("notifications");

        Query query = notifsRef.whereEqualTo("isRead", false);
        List<QueryDocumentSnapshot> documents = query.get().get().getDocuments();

        if (documents.isEmpty()) {
            return 0;
        }

        WriteBatch batch = firestore.batch();
        for (QueryDocumentSnapshot doc : documents) {
            batch.update(doc.getReference(), "isRead", true);
        }

        batch.commit().get();
        return documents.size();
    }

    public void deleteNotification(String userId, String notifId) {
        log.info("Bắt đầu xóa thông báo: userId={}, notifId={}", userId, notifId);
        try {
            com.google.cloud.firestore.DocumentSnapshot doc =
                    notificationRepository.findNotificationById(userId, notifId);

            if (!doc.exists()) {
                throw BusinessException.thongBaoKhongTimThay(notifId);
            }

            notificationRepository.deleteNotification(userId, notifId);
            log.info("Xóa thông báo thành công: userId={}, notifId={}", userId, notifId);
        } catch (BusinessException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Lỗi khi xóa thông báo: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        } catch (java.util.concurrent.ExecutionException e) {
            log.error("Lỗi khi xóa thông báo: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        }
    }

    public void deleteNotificationByProfile(String profileCollection, String profileId, String notifId) {
        log.info("Bắt đầu xóa thông báo: collection={}, profileId={}, notifId={}", profileCollection, profileId, notifId);
        try {
            com.google.cloud.firestore.DocumentReference docRef = firestore
                    .collection(profileCollection)
                    .document(profileId)
                    .collection("notifications")
                    .document(notifId);
                    
            com.google.cloud.firestore.DocumentSnapshot doc = docRef.get().get();

            if (!doc.exists()) {
                throw BusinessException.thongBaoKhongTimThay(notifId);
            }

            docRef.delete().get();
            log.info("Xóa thông báo thành công: profileId={}, notifId={}", profileId, notifId);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Lỗi khi xóa thông báo: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        }
    }

    private NotificationDTO mapToDTO(String notifId, Map<String, Object> data) {
        if (data == null) {
            return NotificationDTO.builder().id(notifId).build();
        }

        return NotificationDTO.builder()
                .id(notifId)
                .type(toInteger(data.get("type")))
                .title((String) data.get("title"))
                .body((String) data.get("body"))
                .orderId((String) data.get("orderId"))
                .referenceId((String) data.get("referenceId"))
                .isRead((Boolean) data.get("isRead"))
                .imageUrl((String) data.get("imageUrl"))
                .createdAt(toInstant(data.get("createdAt")))
                .build();
    }

    private Integer toInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Number num) return num.intValue();
        return null;
    }

    private Instant toInstant(Object value) {
        if (value == null) return null;
        if (value instanceof Timestamp ts) return ts.toDate().toInstant();
        if (value instanceof java.util.Date date) return date.toInstant();
        if (value instanceof Long millis) return Instant.ofEpochMilli(millis);
        return null;
    }

    public void createNotification(String profileCollection, String profileId, NotificationDTO dto) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("title", dto.getTitle());
            data.put("body", dto.getBody());
            data.put("type", dto.getType());
            data.put("isRead", false);
            data.put("createdAt", com.google.cloud.Timestamp.now());
            if (dto.getOrderId() != null) data.put("orderId", dto.getOrderId());
            if (dto.getReferenceId() != null) data.put("referenceId", dto.getReferenceId());
            if (dto.getImageUrl() != null) data.put("imageUrl", dto.getImageUrl());
            
            notificationRepository.addNotificationToCollection(profileCollection, profileId, data);
        } catch (Exception e) {
            log.error("Lỗi khi tạo thông báo cho {} ({}): {}", profileCollection, profileId, e.getMessage());
        }
    }
    @SuppressWarnings("unchecked")
    public void notifyMerchantByStoreId(String storeId, NotificationDTO dto) {
        try {
            log.info("notifyMerchantByStoreId - Tìm merchant với storeId: {}", storeId);
            
            // Cách 1: Query whereArrayContains
            List<com.google.cloud.firestore.QueryDocumentSnapshot> docs = notificationRepository.getFirestore()
                    .collection("merchant_profiles")
                    .whereArrayContains("storeIds", storeId)
                    .limit(1)
                    .get().get().getDocuments();
            if (!docs.isEmpty()) {
                String merchantId = docs.get(0).getId();
                log.info("notifyMerchantByStoreId - Tìm thấy merchant (qua whereArrayContains): {}", merchantId);
                createNotification("merchant_profiles", merchantId, dto);
                return;
            }
            
            // Cách 2: Duyệt tất cả merchant_profiles, check storeIds thủ công
            log.warn("notifyMerchantByStoreId - whereArrayContains không tìm thấy, duyệt toàn bộ merchant_profiles");
            List<com.google.cloud.firestore.QueryDocumentSnapshot> allDocs = notificationRepository.getFirestore()
                    .collection("merchant_profiles")
                    .get().get().getDocuments();
            for (com.google.cloud.firestore.QueryDocumentSnapshot doc : allDocs) {
                Object storeIdsObj = doc.get("storeIds");
                if (storeIdsObj instanceof List) {
                    List<String> storeIds = (List<String>) storeIdsObj;
                    if (storeIds.contains(storeId)) {
                        String merchantId = doc.getId();
                        log.info("notifyMerchantByStoreId - Tìm thấy merchant (qua duyệt thủ công): {}", merchantId);
                        createNotification("merchant_profiles", merchantId, dto);
                        return;
                    }
                }
            }
            
            log.warn("notifyMerchantByStoreId - Không tìm thấy merchant nào cho storeId: {}", storeId);
        } catch (Exception e) {
            log.error("Lỗi khi tìm merchant bằng storeId {}: {}", storeId, e.getMessage(), e);
        }
    }

    public void notifyAdmins(NotificationDTO dto) {
        try {
            List<com.google.cloud.firestore.QueryDocumentSnapshot> docs = notificationRepository.getFirestore()
                    .collection("admin_profiles")
                    .get().get().getDocuments();
            for (com.google.cloud.firestore.QueryDocumentSnapshot doc : docs) {
                String adminId = doc.getId();
                createNotification("admin_profiles", adminId, dto);
            }
            log.info("Đã gửi thông báo hệ thống tới {} quản trị viên.", docs.size());
        } catch (Exception e) {
            log.error("Lỗi khi gửi thông báo tới các admin: {}", e.getMessage());
        }
    }
}
