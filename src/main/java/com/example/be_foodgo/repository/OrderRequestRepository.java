package com.example.be_foodgo.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Date;
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
        Map<String, Object> dataToSave = convertInstants(data);
        ApiFuture<DocumentReference> ref = firestore.collection(COLLECTION_ORDER_REQUESTS).add(dataToSave);
        return ref.get().getId();
    }

    public String savePerDriver(String driverId, Map<String, Object> data) throws ExecutionException, InterruptedException {
        Map<String, Object> dataToSave = convertInstants(data);
        ApiFuture<DocumentReference> ref = firestore
                .collection(COLLECTION_ORDER_REQUESTS)
                .document(driverId)
                .collection("requests")
                .add(dataToSave);
        return ref.get().getId();
    }

    public Map<String, Object> findDriverRequestById(String driverId, String requestId) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = firestore.collection(COLLECTION_ORDER_REQUESTS)
                .document(driverId)
                .collection("requests")
                .document(requestId)
                .get()
                .get();
        if (!doc.exists()) return null;
        return convertDocData(doc.getData(), doc.getId());
    }

    public void deleteDriverRequest(String driverId, String requestId) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION_ORDER_REQUESTS)
                .document(driverId)
                .collection("requests")
                .document(requestId)
                .delete()
                .get();
    }

    public void updateFields(String orderId, Map<String, Object> fields) throws ExecutionException, InterruptedException {
        if (fields == null || fields.isEmpty()) return;
        Map<String, Object> converted = convertInstants(fields);
        firestore.collection(COLLECTION_ORDER_REQUESTS)
                .document(orderId)
                .update(converted)
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

    public List<Map<String, Object>> findExpiredPendingRequests(Instant now) throws ExecutionException, InterruptedException {
        Date nowDate = Date.from(now);
        List<QueryDocumentSnapshot> docs = firestore.collection(COLLECTION_ORDER_REQUESTS)
                .whereEqualTo("status", "pending")
                .whereLessThan("expiresAt", nowDate)
                .get()
                .get()
                .getDocuments();
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (QueryDocumentSnapshot doc : docs) {
            result.add(convertDocData(doc.getData(), doc.getId()));
        }
        return result;
    }

    public List<Map<String, Object>> findPendingRequests() throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> docs = firestore.collection(COLLECTION_ORDER_REQUESTS)
                .whereEqualTo("status", "pending")
                .get()
                .get()
                .getDocuments();
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (QueryDocumentSnapshot doc : docs) {
            result.add(convertDocData(doc.getData(), doc.getId()));
        }
        return result;
    }

    private Map<String, Object> convertDocData(Map<String, Object> data, String docId) {
        if (data == null) return null;
        Map<String, Object> result = new HashMap<>(data);
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Timestamp ts) {
                result.put(entry.getKey(), Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos()));
            } else if (value instanceof Date date) {
                result.put(entry.getKey(), date.toInstant());
            } else if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nested = (Map<String, Object>) value;
                result.put(entry.getKey(), convertDocData(nested, null));
            } else if (value instanceof List) {
                result.put(entry.getKey(), convertListData((List<?>) value));
            }
        }
        result.put("id", docId);
        return result;
    }

    private List<Object> convertListData(List<?> list) {
        List<Object> result = new java.util.ArrayList<>(list);
        for (int i = 0; i < list.size(); i++) {
            Object value = list.get(i);
            if (value instanceof Timestamp ts) {
                result.set(i, Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos()));
            } else if (value instanceof Date date) {
                result.set(i, date.toInstant());
            } else if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nested = (Map<String, Object>) value;
                result.set(i, convertDocData(nested, null));
            }
        }
        return result;
    }

    public void updateFieldsByDocId(String docId, Map<String, Object> fields) throws ExecutionException, InterruptedException {
        if (fields == null || fields.isEmpty()) return;
        Map<String, Object> converted = convertInstants(fields);
        firestore.collection(COLLECTION_ORDER_REQUESTS)
                .document(docId)
                .update(converted)
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

    private Map<String, Object> convertInstants(Map<String, Object> original) {
        Map<String, Object> result = new HashMap<>(original);
        for (Map.Entry<String, Object> entry : original.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Instant instant) {
                result.put(entry.getKey(), Date.from(instant));
            } else if (value instanceof Map<?, ?> nestedMap) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nested = (Map<String, Object>) nestedMap;
                result.put(entry.getKey(), convertInstants(nested));
            } else if (value instanceof List<?> nestedList) {
                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) nestedList;
                result.put(entry.getKey(), convertInstantsInList(list));
            }
        }
        return result;
    }

    private List<Object> convertInstantsInList(List<Object> original) {
        List<Object> result = new java.util.ArrayList<>(original);
        for (int i = 0; i < original.size(); i++) {
            Object value = original.get(i);
            if (value instanceof Instant instant) {
                result.set(i, Date.from(instant));
            } else if (value instanceof Map<?, ?> nestedMap) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nested = (Map<String, Object>) nestedMap;
                result.set(i, convertInstants(nested));
            }
        }
        return result;
    }
}
