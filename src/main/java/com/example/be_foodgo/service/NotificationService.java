package com.example.be_foodgo.service;

import com.example.be_foodgo.dto.NotificationDTO;
import com.example.be_foodgo.exception.BusinessException;
import com.example.be_foodgo.repository.NotificationRepository;
import com.google.cloud.Timestamp;
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

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<NotificationDTO> getNotifications(String userId, Integer type) {
        log.info("Bat dau lay danh sach thong bao: userId={}, type={}", userId, type);
        try {
            List<com.google.cloud.firestore.QueryDocumentSnapshot> docs =
                    notificationRepository.findNotifications(userId, type);

            List<NotificationDTO> notifications = new ArrayList<>();
            for (com.google.cloud.firestore.QueryDocumentSnapshot doc : docs) {
                notifications.add(mapToDTO(doc.getId(), doc.getData()));
            }

            log.info("Tim thay {} thong bao cho userId={}", notifications.size(), userId);
            return notifications;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi lay danh sach thong bao: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        } catch (java.util.concurrent.ExecutionException e) {
            log.error("Loi khi lay danh sach thong bao: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        }
    }

    public NotificationDTO markAsRead(String userId, String notifId) {
        log.info("Bat dau danh dau da doc thong bao: userId={}, notifId={}", userId, notifId);
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

            log.info("Danh dau da doc thong bao thanh cong: userId={}, notifId={}", userId, notifId);
            return mapToDTO(doc.getId(), updatedData);
        } catch (BusinessException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi danh dau da doc thong bao: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        } catch (java.util.concurrent.ExecutionException e) {
            log.error("Loi khi danh dau da doc thong bao: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        }
    }

    public int markAllAsRead(String userId) {
        log.info("Bat dau danh dau tat ca thong bao da doc: userId={}", userId);
        try {
            List<String> unreadIds = notificationRepository.findUnreadNotificationIds(userId);

            if (unreadIds.isEmpty()) {
                log.info("Khong co thong bao nao chua doc cho userId={}", userId);
                return 0;
            }

            notificationRepository.updateAllNotificationsRead(userId, unreadIds);

            log.info("Danh dau {} thong bao thanh cong cho userId={}", unreadIds.size(), userId);
            return unreadIds.size();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi danh dau tat ca thong bao da doc: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        } catch (java.util.concurrent.ExecutionException e) {
            log.error("Loi khi danh dau tat ca thong bao da doc: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        }
    }

    public void deleteNotification(String userId, String notifId) {
        log.info("Bat dau xoa thong bao: userId={}, notifId={}", userId, notifId);
        try {
            com.google.cloud.firestore.DocumentSnapshot doc =
                    notificationRepository.findNotificationById(userId, notifId);

            if (!doc.exists()) {
                throw BusinessException.thongBaoKhongTimThay(notifId);
            }

            notificationRepository.deleteNotification(userId, notifId);
            log.info("Xoa thong bao thanh cong: userId={}, notifId={}", userId, notifId);
        } catch (BusinessException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi xoa thong bao: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        } catch (java.util.concurrent.ExecutionException e) {
            log.error("Loi khi xoa thong bao: {}", e.getMessage());
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
}
