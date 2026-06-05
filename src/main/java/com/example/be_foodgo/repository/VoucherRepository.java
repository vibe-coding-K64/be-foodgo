package com.example.be_foodgo.repository;

import com.example.be_foodgo.model.Voucher;
import com.example.be_foodgo.model.MyVoucher;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.google.cloud.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
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
        Map<String, Object> map = voucherToMap(voucher);
        map.put("expiryDate", FieldValue.serverTimestamp());
        map.put("createdAt", FieldValue.serverTimestamp());
        map.put("updatedAt", FieldValue.serverTimestamp());
        ApiFuture<WriteResult> collectionsApiFuture = docRef.set(map);
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
            Voucher voucher = parseVoucherFromDoc(document);
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
            voucherList.add(parseVoucherFromDoc(document));
        }
        return voucherList;
    }

    public List<Voucher> getVouchersByStoreId(String storeId) throws ExecutionException, InterruptedException {
        CollectionReference vouchersRef = firestore.collection(COLLECTION_NAME);
        QuerySnapshot snapshot;

        if (storeId == null || storeId.isEmpty()) {
            Query query = vouchersRef.whereEqualTo("isFreeship", false);
            snapshot = query.get().get();
        } else {
            Query query = vouchersRef
                    .whereEqualTo("isFreeship", false)
                    .whereEqualTo("storeId", storeId);
            snapshot = query.get().get();
        }

        List<Voucher> list = new ArrayList<>();
        for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
            Voucher v = parseVoucherFromDoc(doc);
            list.add(v);
        }
        return list;
    }

    public List<Voucher> getFreeshipVouchersByStoreId(String storeId) throws ExecutionException, InterruptedException {
        CollectionReference vouchersRef = firestore.collection(COLLECTION_NAME);
        QuerySnapshot snapshot;

        if (storeId == null || storeId.isEmpty()) {
            Query query = vouchersRef.whereEqualTo("isFreeship", true);
            snapshot = query.get().get();
        } else {
            Query query = vouchersRef
                    .whereEqualTo("isFreeship", true)
                    .whereEqualTo("storeId", storeId);
            snapshot = query.get().get();
        }

        List<Voucher> list = new ArrayList<>();
        for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
            Voucher v = parseVoucherFromDoc(doc);
            list.add(v);
        }
        return list;
    }

    public String updateVoucher(Voucher voucher) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(voucher.getId());
        Map<String, Object> map = voucherToMap(voucher);
        map.put("expiryDate", voucher.getExpiryDate() != null
                ? FieldValue.serverTimestamp()
                : null);
        map.put("updatedAt", FieldValue.serverTimestamp());
        ApiFuture<WriteResult> collectionsApiFuture = docRef.set(map);
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
                .title(doc.getString("title"))
                .subtitle(doc.getString("subtitle"))
                .code(doc.getString("code"))
                .description(doc.getString("description"))
                .expiryDate(toDate(doc.get("expiryDate")))
                .type(doc.get("type") != null ? doc.getLong("type").intValue() : 2)
                .value(doc.getDouble("value"))
                .imageUrl(doc.getString("imageUrl"))
                .terms(doc.getString("terms"))
                .minOrderValue(doc.getDouble("minOrderValue"))
                .isActive(doc.getBoolean("isActive") != null ? doc.getBoolean("isActive") : true)
                .isFreeship(doc.getBoolean("isFreeship") != null ? doc.getBoolean("isFreeship") : false)
                .createdAt(toDate(doc.get("createdAt")))
                .updatedAt(toDate(doc.get("updatedAt")))
                .build();
        log.info("Da doc voucher ca nhan [{}] - title: {}, type: {}, value: {}",
                voucherId, mv.getTitle(), mv.getType(), mv.getValue());
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

    private Date toDate(Object value) {
        if (value == null) return null;
        Instant instant = toInstant(value);
        return instant != null ? Date.from(instant) : null;
    }

    private Voucher parseVoucherFromDoc(DocumentSnapshot doc) {
        Voucher v = new Voucher();
        v.setId(doc.getId());
        v.setStoreId(doc.getString("storeId"));
        v.setTitle(doc.getString("title"));
        v.setSubtitle(doc.getString("subtitle"));
        v.setCode(doc.getString("code"));
        v.setType(doc.get("type") != null ? ((Number) doc.get("type")).intValue() : 0);
        v.setValue(doc.getDouble("value"));
        v.setImageUrl(doc.getString("imageUrl"));
        v.setTerms(doc.getString("terms"));
        v.setPointsRequired(doc.get("pointsRequired") != null ? ((Number) doc.get("pointsRequired")).intValue() : 0);
        v.setRemaining(doc.get("remaining") != null ? ((Number) doc.get("remaining")).intValue() : 0);
        v.setMinOrderValue(doc.getDouble("minOrderValue"));
        v.setLimitCount(doc.get("limitCount") != null ? ((Number) doc.get("limitCount")).intValue() : 0);
        v.setUsedCount(doc.get("usedCount") != null ? ((Number) doc.get("usedCount")).intValue() : 0);
        v.setExpiryDate(toDate(doc.get("expiryDate")));
        v.setIsActive(doc.getBoolean("isActive") != null ? doc.getBoolean("isActive") : false);
        v.setValidityDays(doc.get("validityDays") != null ? ((Number) doc.get("validityDays")).intValue() : 0);
        v.setIsFreeship(doc.getBoolean("isFreeship") != null ? doc.getBoolean("isFreeship") : false);
        v.setCreatedAt(toDate(doc.get("createdAt")));
        v.setUpdatedAt(toDate(doc.get("updatedAt")));
        return v;
    }

    private Boolean toBoolean(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        return false;
    }

    public Voucher getSystemVoucher(String voucherId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(voucherId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot doc = future.get();
        if (!doc.exists()) {
            log.warn("Voucher [{}] khong ton tai.", voucherId);
            return null;
        }
        String storeId = doc.getString("storeId");
        if (storeId != null) {
            log.warn("Voucher [{}] la voucher cua hang, khong phai voucher he thong.", voucherId);
            return null;
        }
        Voucher voucher = parseVoucherFromDoc(doc);
        if (voucher != null) {
            voucher.setId(doc.getId());
        }
        log.info("Da doc voucher [{}] - title: {}, pointsRequired: {}, remaining: {}",
                voucherId, voucher != null ? voucher.getTitle() : null,
                voucher != null ? voucher.getPointsRequired() : null,
                voucher != null ? voucher.getRemaining() : null);
        return voucher;
    }

    public List<Voucher> getAllSystemVouchers() throws ExecutionException, InterruptedException {
        CollectionReference vouchersRef = firestore.collection(COLLECTION_NAME);
        Query query = vouchersRef.whereEqualTo("storeId", null);
        ApiFuture<QuerySnapshot> future = query.get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<Voucher> list = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            Voucher v = parseVoucherFromDoc(doc);
            list.add(v);
        }
        return list;
    }

    public List<Voucher> getExchangeableVouchers() throws ExecutionException, InterruptedException {
        CollectionReference vouchersRef = firestore.collection(COLLECTION_NAME);
        Query query = vouchersRef
                .whereGreaterThan("pointsRequired", 0);
        ApiFuture<QuerySnapshot> future = query.get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<Voucher> list = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            Voucher v = parseVoucherFromDoc(doc);
            v.setId(doc.getId());
            if (v.getStoreId() == null) {
                list.add(v);
            }
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
        data.put("title", myVoucher.getTitle());
        data.put("subtitle", myVoucher.getSubtitle());
        data.put("code", myVoucher.getCode());
        data.put("description", myVoucher.getDescription());
        data.put("expiryDate", myVoucher.getExpiryDate() != null ? Timestamp.ofTimeSecondsAndNanos(myVoucher.getExpiryDate().toInstant().getEpochSecond(), 0) : null);
        data.put("type", myVoucher.getType());
        data.put("value", myVoucher.getValue());
        data.put("imageUrl", myVoucher.getImageUrl());
        data.put("terms", myVoucher.getTerms());
        data.put("minOrderValue", myVoucher.getMinOrderValue());
        data.put("isActive", myVoucher.isActive());
        data.put("isFreeship", myVoucher.isFreeship());
        data.put("createdAt", FieldValue.serverTimestamp());
        data.put("updatedAt", FieldValue.serverTimestamp());
        ApiFuture<WriteResult> future = docRef.set(data);
        future.get();
        log.info("Da luu my_voucher [{}] tai [{}]", myVoucher.getId(), path);
    }

    public void giamRemainingSystemVoucher(String voucherId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(voucherId);
        ApiFuture<WriteResult> future = docRef.update("remaining", FieldValue.increment(-1));
        future.get();
        log.info("Da giam remaining cua voucher [{}]", voucherId);
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

    public void congLoyaltyPoints(String userId, int soDiem) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection("customer_profiles").document(userId);
        ApiFuture<WriteResult> future = docRef.update("loyaltyPoints", FieldValue.increment(soDiem));
        future.get();
        log.info("Da cong [{}] diem loyalty cho user [{}]", soDiem, userId);
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
                    .title(doc.getString("title"))
                    .subtitle(doc.getString("subtitle"))
                    .code(doc.getString("code"))
                    .description(doc.getString("description"))
                    .expiryDate(toDate(doc.get("expiryDate")))
                    .type(doc.get("type") != null ? doc.getLong("type").intValue() : 2)
                    .value(doc.getDouble("value"))
                    .imageUrl(doc.getString("imageUrl"))
                    .terms(doc.getString("terms"))
                    .minOrderValue(doc.getDouble("minOrderValue"))
                    .isActive(doc.getBoolean("isActive") != null ? doc.getBoolean("isActive") : true)
                    .isFreeship(doc.getBoolean("isFreeship") != null ? doc.getBoolean("isFreeship") : false)
                    .createdAt(toDate(doc.get("createdAt")))
                    .updatedAt(toDate(doc.get("updatedAt")))
                    .build();
            list.add(mv);
        }
        return list;
    }

    private Map<String, Object> voucherToMap(Voucher v) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", v.getId());
        map.put("storeId", v.getStoreId());
        map.put("title", v.getTitle());
        map.put("subtitle", v.getSubtitle());
        map.put("code", v.getCode());
        map.put("type", v.getType());
        map.put("value", v.getValue());
        map.put("pointsRequired", v.getPointsRequired());
        map.put("imageUrl", v.getImageUrl());
        map.put("remaining", v.getRemaining());
        map.put("terms", v.getTerms());
        map.put("minOrderValue", v.getMinOrderValue());
        map.put("limitCount", v.getLimitCount());
        map.put("usedCount", v.getUsedCount());
        map.put("isActive", v.getIsActive());
        map.put("validityDays", v.getValidityDays());
        map.put("isFreeship", v.getIsFreeship());
        map.put("createdAt", v.getCreatedAt());
        return map;
    }
}
