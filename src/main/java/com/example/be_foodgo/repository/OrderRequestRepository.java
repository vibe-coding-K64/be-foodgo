package com.example.be_foodgo.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Repository
public class OrderRequestRepository {

    private static final String COLLECTION_ORDER_REQUESTS = "order_requests";

    private final Firestore firestore;

    public OrderRequestRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public String save(Map<String, Object> data) throws ExecutionException, InterruptedException {
        ApiFuture<DocumentReference> ref = firestore.collection(COLLECTION_ORDER_REQUESTS).add(data);
        return ref.get().getId();
    }

    public void updateFields(String orderId, Map<String, Object> fields) throws ExecutionException, InterruptedException {
        if (fields == null || fields.isEmpty()) return;
        firestore.collection(COLLECTION_ORDER_REQUESTS)
                .document(orderId)
                .update(fields)
                .get();
    }

    public Map<String, Object> findByOrderId(String orderId) throws ExecutionException, InterruptedException {
        QuerySnapshot snapshot = firestore.collection(COLLECTION_ORDER_REQUESTS)
                .whereEqualTo("orderId", orderId)
                .limit(1)
                .get()
                .get();
        if (snapshot.isEmpty()) return null;
        DocumentSnapshot doc = snapshot.getDocuments().get(0);
        Map<String, Object> data = doc.getData();
        if (data == null) return null;
        data.put("id", doc.getId());
        return data;
    }

    public List<QueryDocumentSnapshot> findExpiredPendingRequests(Instant now) throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION_ORDER_REQUESTS)
                .whereEqualTo("status", "pending")
                .whereLessThan("expiresAt", now)
                .get()
                .get()
                .getDocuments();
    }

    public List<QueryDocumentSnapshot> findPendingRequests() throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION_ORDER_REQUESTS)
                .whereEqualTo("status", "pending")
                .get()
                .get()
                .getDocuments();
    }

    public void updateFieldsByDocId(String docId, Map<String, Object> fields) throws ExecutionException, InterruptedException {
        if (fields == null || fields.isEmpty()) return;
        firestore.collection(COLLECTION_ORDER_REQUESTS)
                .document(docId)
                .update(fields)
                .get();
    }

    public void deleteByOrderId(String orderId) throws ExecutionException, InterruptedException {
        QuerySnapshot snapshot = firestore.collection(COLLECTION_ORDER_REQUESTS)
                .whereEqualTo("orderId", orderId)
                .get()
                .get();
        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            doc.getReference().delete().get();
        }
    }
}
