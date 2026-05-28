package com.example.be_foodgo.repository;

import com.example.be_foodgo.model.Voucher;
import com.example.be_foodgo.model.MyVoucher;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Repository
public class VoucherRepository {

    private static final Logger log = LoggerFactory.getLogger(VoucherRepository.class);

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION_NAME = "vouchers";

    public String saveVoucher(Voucher voucher) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(voucher.getId());
        ApiFuture<WriteResult> collectionsApiFuture = docRef.set(voucher);
        return collectionsApiFuture.get().getUpdateTime().toString();
    }

    public List<String> getAllVoucherIds() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<String> ids = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            ids.add(doc.getId());
        }
        return ids;
    }

    public Voucher getVoucher(String id) throws ExecutionException, InterruptedException {
        DocumentReference documentReference = firestore.collection(COLLECTION_NAME).document(id);
        ApiFuture<DocumentSnapshot> future = documentReference.get();
        DocumentSnapshot document = future.get();
        log.info("Truy van voucher [{}] tu collection [{}] - exists: {}", id, COLLECTION_NAME, document.exists());
        if (document.exists()) {
            Voucher voucher = document.toObject(Voucher.class);
            if (voucher != null) {
                voucher.setId(document.getId());
            }
            log.info("Da doc voucher [{}] - title: {}, remaining: {}, minOrderValue: {}, type: {}, value: {}",
                    id, document.getString("title"), document.get("remaining"),
                    document.get("minOrderValue"), document.get("type"), document.get("value"));
            return voucher;
        }
        log.warn("Voucher [{}] khong ton tai trong collection [{}].", id, COLLECTION_NAME);
        return null;
    }

    public List<Voucher> getAllVouchers(String storeId) throws ExecutionException, InterruptedException {
        CollectionReference vouchersRef = firestore.collection(COLLECTION_NAME);
        Query query = vouchersRef;
        if (storeId != null && !storeId.isEmpty()) {
            query = query.whereEqualTo("storeId", storeId);
        }
        ApiFuture<QuerySnapshot> future = query.get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        List<Voucher> voucherList = new ArrayList<>();
        for (QueryDocumentSnapshot document : documents) {
            voucherList.add(document.toObject(Voucher.class));
        }
        return voucherList;
    }

    public String updateVoucher(Voucher voucher) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(voucher.getId());
        ApiFuture<WriteResult> collectionsApiFuture = docRef.set(voucher);
        return collectionsApiFuture.get().getUpdateTime().toString();
    }

    public String deleteVoucher(String id) {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
        docRef.delete();
        return "Voucher with ID " + id + " has been deleted";
    }

    public MyVoucher layVoucherCaNhan(String userId, String voucherId)
            throws ExecutionException, InterruptedException {
        String path = "customer_profiles/" + userId + "/my_vouchers/" + voucherId;
        DocumentReference docRef = firestore
                .collection("customer_profiles")
                .document(userId)
                .collection("my_vouchers")
                .document(voucherId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot doc = future.get();
        log.info("Truy van voucher ca nhan [{}] tu duong dan [{}] - exists: {}", voucherId, path, doc.exists());
        if (!doc.exists()) {
            log.warn("Voucher ca nhan [{}] khong ton tai tai [{}].", voucherId, path);
            return null;
        }
        MyVoucher mv = MyVoucher.builder()
                .id(doc.getId())
                .name(doc.getString("name"))
                .code(doc.getString("code"))
                .description(doc.getString("description"))
                .expiryDate(toInstant(doc.get("expiryDate")))
                .type(doc.get("type") != null ? doc.getLong("type").intValue() : 2)
                .value(doc.getDouble("value"))
                .minOrderValue(doc.getDouble("minOrderValue"))
                .createdAt(toInstant(doc.get("createdAt")))
                .updatedAt(toInstant(doc.get("updatedAt")))
                .build();
        log.info("Da doc voucher ca nhan [{}] - name: {}, type: {}, value: {}",
                voucherId, mv.getName(), mv.getType(), mv.getValue());
        return mv;
    }

    public void xoaVoucherCaNhan(String userId, String voucherId) {
        firestore.collection("customer_profiles")
                .document(userId)
                .collection("my_vouchers")
                .document(voucherId)
                .delete();
    }

    @SuppressWarnings("unchecked")
    private Instant toInstant(Object value) {
        if (value == null) return null;
        if (value instanceof com.google.protobuf.Timestamp) {
            com.google.protobuf.Timestamp ts = (com.google.protobuf.Timestamp) value;
            return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
        }
        if (value instanceof java.util.Date) {
            return ((java.util.Date) value).toInstant();
        }
        if (value instanceof String) {
            return Instant.parse((String) value);
        }
        if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            Object seconds = map.get("epochSecond");
            Object nanos = map.get("nano");
            if (seconds != null) {
                long sec = seconds instanceof Long ? (Long) seconds : ((Integer) seconds).longValue();
                int nano = nanos != null ? (nanos instanceof Long ? ((Long) nanos).intValue() : (Integer) nanos) : 0;
                return Instant.ofEpochSecond(sec, nano);
            }
            return null;
        }
        return null;
    }

    private Boolean toBoolean(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        return false;
    }

    public Voucher getSystemVoucher(String voucherId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection("system_vouchers").document(voucherId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot doc = future.get();
        if (!doc.exists()) {
            log.warn("System voucher [{}] khong ton tai.", voucherId);
            return null;
        }
        Voucher voucher = doc.toObject(Voucher.class);
        if (voucher != null) {
            voucher.setId(doc.getId());
        }
        log.info("Da doc system voucher [{}] - title: {}, pointsRequired: {}, remaining: {}",
                voucherId, voucher != null ? voucher.getTitle() : null,
                voucher != null ? voucher.getPointsRequired() : null,
                voucher != null ? voucher.getRemaining() : null);
        return voucher;
    }

    public List<Voucher> getAllSystemVouchers() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection("system_vouchers").get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<Voucher> list = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            Voucher v = doc.toObject(Voucher.class);
            if (v != null) {
                v.setId(doc.getId());
            }
            list.add(v);
        }
        return list;
    }

    public void luuMyVoucher(String userId, MyVoucher myVoucher) throws ExecutionException, InterruptedException {
        String path = "customer_profiles/" + userId + "/my_vouchers/" + myVoucher.getId();
        DocumentReference docRef = firestore
                .collection("customer_profiles")
                .document(userId)
                .collection("my_vouchers")
                .document(myVoucher.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("id", myVoucher.getId());
        data.put("name", myVoucher.getName());
        data.put("code", myVoucher.getCode());
        data.put("description", myVoucher.getDescription());
        data.put("expiryDate", myVoucher.getExpiryDate() != null ? myVoucher.getExpiryDate().toString() : null);
        data.put("type", myVoucher.getType());
        data.put("value", myVoucher.getValue());
        data.put("minOrderValue", myVoucher.getMinOrderValue());
        data.put("createdAt", myVoucher.getCreatedAt() != null ? myVoucher.getCreatedAt().toString() : null);
        data.put("updatedAt", myVoucher.getUpdatedAt() != null ? myVoucher.getUpdatedAt().toString() : null);
        ApiFuture<WriteResult> future = docRef.set(data);
        future.get();
        log.info("Da luu my_voucher [{}] tai [{}]", myVoucher.getId(), path);
    }

    public void giamRemainingSystemVoucher(String voucherId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection("system_vouchers").document(voucherId);
        ApiFuture<WriteResult> future = docRef.update("remaining", FieldValue.increment(-1));
        future.get();
        log.info("Da giam remaining cua system voucher [{}]", voucherId);
    }

    public Integer getLoyaltyPoints(String userId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection("customer_profiles").document(userId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot doc = future.get();
        if (!doc.exists()) {
            return null;
        }
        Object pointsObj = doc.get("loyaltyPoints");
        if (pointsObj instanceof Long) {
            return ((Long) pointsObj).intValue();
        }
        if (pointsObj instanceof Integer) {
            return (Integer) pointsObj;
        }
        return null;
    }

    public void truLoyaltyPoints(String userId, int soDiem) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection("customer_profiles").document(userId);
        ApiFuture<WriteResult> future = docRef.update("loyaltyPoints", FieldValue.increment(-soDiem));
        future.get();
        log.info("Da tru [{}] diem cua user [{}]", soDiem, userId);
    }

    public boolean kiemTraTonTaiMyVoucher(String userId, String voucherId)
            throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore
                .collection("customer_profiles")
                .document(userId)
                .collection("my_vouchers")
                .document(voucherId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        return future.get().exists();
    }

    public List<MyVoucher> getAllMyVouchers(String userId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore
                .collection("customer_profiles")
                .document(userId)
                .collection("my_vouchers")
                .get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<MyVoucher> list = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            MyVoucher mv = MyVoucher.builder()
                    .id(doc.getId())
                    .name(doc.getString("name"))
                    .code(doc.getString("code"))
                    .description(doc.getString("description"))
                    .expiryDate(toInstant(doc.get("expiryDate")))
                    .type(doc.get("type") != null ? doc.getLong("type").intValue() : 2)
                    .value(doc.getDouble("value"))
                    .minOrderValue(doc.getDouble("minOrderValue"))
                    .createdAt(toInstant(doc.get("createdAt")))
                    .updatedAt(toInstant(doc.get("updatedAt")))
                    .build();
            list.add(mv);
        }
        return list;
    }
}
