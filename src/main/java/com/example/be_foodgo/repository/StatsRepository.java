package com.example.be_foodgo.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Repository
public class StatsRepository {

    private final Firestore firestore;

    public StatsRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    private static final String COLLECTION_ORDERS = "orders";
    private static final String COLLECTION_STORES = "stores";
    private static final String COLLECTION_DRIVER_PROFILES = "driver_profiles";

    public Map<String, Object> findOrderRawById(String orderId) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = firestore.collection(COLLECTION_ORDERS)
                .document(orderId)
                .get()
                .get();
        return doc.exists() ? doc.getData() : null;
    }

    public ApiFuture<DocumentSnapshot> getOrderAsync(String orderId) {
        return firestore.collection(COLLECTION_ORDERS).document(orderId).get();
    }

    public void updateOrderFields(String orderId, Map<String, Object> fields) throws ExecutionException, InterruptedException {
        if (fields == null || fields.isEmpty()) return;
        firestore.collection(COLLECTION_ORDERS)
                .document(orderId)
                .update(fields)
                .get();
    }

    public List<QueryDocumentSnapshot> findAvailableOrders() throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION_ORDERS)
                .whereEqualTo("status", 1)
                .whereEqualTo("driverId", null)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .get()
                .getDocuments();
    }

    public List<QueryDocumentSnapshot> findByDriverIdAndStatus(String driverId, int status)
            throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION_ORDERS)
                .whereEqualTo("driverId", driverId)
                .whereEqualTo("status", status)
                .get()
                .get()
                .getDocuments();
    }

    public List<QueryDocumentSnapshot> findByDriverIdAndStatusOrderByCreatedAt(
            String driverId, int status, Query.Direction direction)
            throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION_ORDERS)
                .whereEqualTo("driverId", driverId)
                .whereEqualTo("status", status)
                .orderBy("createdAt", direction)
                .get()
                .get()
                .getDocuments();
    }

    public Map<String, Object> findStoreById(String storeId) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = firestore.collection(COLLECTION_STORES)
                .document(storeId)
                .get()
                .get();
        return doc.exists() ? doc.getData() : null;
    }

    public Map<String, Object> findDriverProfileById(String userId) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = firestore.collection(COLLECTION_DRIVER_PROFILES)
                .document(userId)
                .get()
                .get();
        return doc.exists() ? doc.getData() : null;
    }

    public Map<String, Object> findUserById(String userId) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = firestore.collection("users")
                .document(userId)
                .get()
                .get();
        return doc.exists() ? doc.getData() : null;
    }

    public void updateDriverProfileFields(String userId, Map<String, Object> fields)
            throws ExecutionException, InterruptedException {
        if (fields == null || fields.isEmpty()) return;
        firestore.collection(COLLECTION_DRIVER_PROFILES)
                .document(userId)
                .update(fields)
                .get();
    }

    public String saveNotification(String userId, Map<String, Object> data) throws ExecutionException, InterruptedException {
        ApiFuture<DocumentReference> ref = firestore
                .collection("driver_profiles")
                .document(userId)
                .collection("notifications")
                .add(data);
        return ref.get().getId();
    }

    public void saveDriverNotification(String userId, Map<String, Object> data) throws ExecutionException, InterruptedException {
        firestore.collection("driver_profiles")
                .document(userId)
                .collection("notifications")
                .add(data)
                .get();
    }

    public List<QueryDocumentSnapshot> findDriverNotificationsByTypeAndOrderId(
            String userId, int type, String orderId) throws ExecutionException, InterruptedException {
        return firestore.collection("driver_profiles")
                .document(userId)
                .collection("notifications")
                .whereEqualTo("type", type)
                .whereEqualTo("orderId", orderId)
                .get()
                .get()
                .getDocuments();
    }

    public void deleteNotification(String notificationId) throws ExecutionException, InterruptedException {
        String[] parts = notificationId.split("/", 2);
        if (parts.length == 2) {
            firestore.collection(parts[0])
                    .document(parts[1])
                    .delete()
                    .get();
        }
    }

    public void saveCustomerNotification(String userId, Map<String, Object> data)
            throws ExecutionException, InterruptedException {
        firestore.collection("customer_profiles")
                .document(userId)
                .collection("notifications")
                .add(data)
                .get();
    }

    public void updateDriverTotalEarnings(String userId, double amount) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION_DRIVER_PROFILES)
                .document(userId)
                .update("totalEarnings", com.google.cloud.firestore.FieldValue.increment(amount))
                .get();
    }

    public void acceptOrderInTransaction(
            String orderId,
            String userId,
            String driverName,
            String driverPhone,
            String vehiclePlate) throws ExecutionException, InterruptedException {

        DocumentReference orderRef = firestore.collection(COLLECTION_ORDERS).document(orderId);
        DocumentReference driverProfileRef = firestore.collection(COLLECTION_DRIVER_PROFILES).document(userId);

        firestore.runTransaction(transaction -> {
            DocumentSnapshot orderDoc = transaction.get(orderRef).get();

            if (!orderDoc.exists()) {
                throw new IllegalStateException("ORDER_NOT_FOUND:" + orderId);
            }

            Object statusObj = orderDoc.get("status");
            int currentStatus = 0;
            if (statusObj instanceof Number) {
                currentStatus = ((Number) statusObj).intValue();
            }

            Object driverIdField = orderDoc.getData().get("driverId");
            if (currentStatus != 1) {
                throw new IllegalStateException("ORDER_STATUS_INVALID");
            }
            if (driverIdField != null && !driverIdField.toString().isEmpty()) {
                throw new IllegalStateException("ORDER_ALREADY_ASSIGNED");
            }

            Map<String, Object> orderUpdates = new HashMap<>();
            orderUpdates.put("status", 2);
            orderUpdates.put("driverId", userId);
            orderUpdates.put("driverName", driverName);
            orderUpdates.put("driverPhone", driverPhone);
            orderUpdates.put("vehiclePlate", vehiclePlate);
            orderUpdates.put("updatedAt", Instant.now());
            transaction.update(orderRef, orderUpdates);

            Map<String, Object> driverUpdates = new HashMap<>();
            driverUpdates.put("currentOrderId", orderId);
            driverUpdates.put("isAvailable", false);
            driverUpdates.put("updatedAt", Instant.now());
            transaction.update(driverProfileRef, driverUpdates);

            return null;
        }).get();
    }
}
