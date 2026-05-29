package com.example.be_foodgo.repository;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.WriteBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class NotificationRepository {

    private static final Logger log = LoggerFactory.getLogger(NotificationRepository.class);

    private static final String COLLECTION_DRIVER_PROFILES = "driver_profiles";
    private static final String SUB_COLLECTION_NOTIFICATIONS = "notifications";

    private final Firestore firestore;

    public NotificationRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    private CollectionReference getNotificationsCollection(String userId) {
        return firestore
                .collection(COLLECTION_DRIVER_PROFILES)
                .document(userId)
                .collection(SUB_COLLECTION_NOTIFICATIONS);
    }

    private DocumentReference getNotificationDocument(String userId, String notifId) {
        return getNotificationsCollection(userId).document(notifId);
    }

    public List<QueryDocumentSnapshot> findNotifications(String userId, Integer type)
            throws ExecutionException, InterruptedException {
        CollectionReference notifCol = getNotificationsCollection(userId);

        Query query = notifCol.orderBy("createdAt", Query.Direction.DESCENDING);

        if (type != null) {
            query = query.whereEqualTo("type", type);
        }

        return query.get().get().getDocuments();
    }

    public List<QueryDocumentSnapshot> findAllNotifications(String userId)
            throws ExecutionException, InterruptedException {
        return getNotificationsCollection(userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .get()
                .getDocuments();
    }

    public DocumentSnapshot findNotificationById(String userId, String notifId)
            throws ExecutionException, InterruptedException {
        return getNotificationDocument(userId, notifId)
                .get()
                .get();
    }

    public void updateNotificationRead(String userId, String notifId)
            throws ExecutionException, InterruptedException {
        getNotificationDocument(userId, notifId)
                .update("isRead", true)
                .get();
        log.info("Da danh dau da doc thong bao: userId={}, notifId={}", userId, notifId);
    }

    public void updateAllNotificationsRead(String userId, List<String> notifIds)
            throws ExecutionException, InterruptedException {
        if (notifIds == null || notifIds.isEmpty()) {
            log.info("Khong co thong bao nao de danh dau da doc: userId={}", userId);
            return;
        }

        CollectionReference notifCol = getNotificationsCollection(userId);
        WriteBatch batch = firestore.batch();

        for (String notifId : notifIds) {
            DocumentReference docRef = notifCol.document(notifId);
            batch.update(docRef, "isRead", true);
        }

        batch.commit().get();
        log.info("Da danh dau da doc {} thong bao cho userId={}", notifIds.size(), userId);
    }

    public void deleteNotification(String userId, String notifId)
            throws ExecutionException, InterruptedException {
        getNotificationDocument(userId, notifId)
                .delete()
                .get();
        log.info("Da xoa thong bao: userId={}, notifId={}", userId, notifId);
    }

    public List<String> findUnreadNotificationIds(String userId)
            throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> docs = getNotificationsCollection(userId)
                .whereEqualTo("isRead", false)
                .get()
                .get()
                .getDocuments();

        List<String> ids = new ArrayList<>();
        for (QueryDocumentSnapshot doc : docs) {
            ids.add(doc.getId());
        }
        return ids;
    }
}
