package com.example.be_foodgo.repository;

import com.example.be_foodgo.model.Address;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Repository
public class AddressRepository {

    private static final Logger log = LoggerFactory.getLogger(AddressRepository.class);

    private static final String CUSTOMER_PROFILES_COLLECTION = "customer_profiles";
    private static final String ADDRESSES_SUB_COLLECTION = "addresses";

    private final Firestore firestore;

    public AddressRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    private CollectionReference getAddressesCollection(String userId) {
        return firestore
                .collection(CUSTOMER_PROFILES_COLLECTION)
                .document(userId)
                .collection(ADDRESSES_SUB_COLLECTION);
    }

    private DocumentReference getAddressDocument(String userId, String addressId) {
        return getAddressesCollection(userId).document(addressId);
    }

    public List<Address> layTatCaDiaChi(String userId) throws ExecutionException, InterruptedException {
        log.info("Truy van tat ca dia chi cua nguoi dung: {}", userId);
        CollectionReference addressesRef = getAddressesCollection(userId);

        ApiFuture<QuerySnapshot> query = addressesRef.get();
        QuerySnapshot snapshot = query.get();

        List<Address> addresses = new ArrayList<>();
        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            Address addr = mapToAddress(doc.getId(), doc);
            addresses.add(addr);
        }

        log.info("Tim thay {} dia chi cua nguoi dung {}", addresses.size(), userId);
        return addresses;
    }

    public Address layMotDiaChi(String userId, String addressId) throws ExecutionException, InterruptedException {
        log.info("Truy van mot dia chi - userId: {}, addressId: {}", userId, addressId);
        DocumentReference docRef = getAddressDocument(userId, addressId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot doc = future.get();

        if (!doc.exists()) {
            log.warn("Khong tim thay dia chi [{}] cua nguoi dung {}", addressId, userId);
            return null;
        }

        Address addr = mapToAddress(doc.getId(), doc);
        log.info("Tim thay dia chi [{}] cua nguoi dung {}", addressId, userId);
        return addr;
    }

    public String taoDiaChi(String userId, Address address) {
        log.info("Tao dia chi moi cho nguoi dung {} - nhan: {}", userId, address.getName());
        CollectionReference addressesRef = getAddressesCollection(userId);
        String addressId = addressesRef.document().getId();

        Map<String, Object> data = new HashMap<>();
        data.put("name", address.getName() != null ? address.getName() : "");
        data.put("address", address.getAddress() != null ? address.getAddress() : "");
        data.put("receiverName", address.getReceiverName() != null ? address.getReceiverName() : "");
        data.put("receiverPhone", address.getReceiverPhone() != null ? address.getReceiverPhone() : "");
        data.put("lat", address.getLat() != null ? address.getLat() : 0.0);
        data.put("lng", address.getLng() != null ? address.getLng() : 0.0);
        data.put("isDefault", address.getIsDefault() != null ? address.getIsDefault() : false);
        data.put("createdAt", FieldValue.serverTimestamp());
        data.put("updatedAt", FieldValue.serverTimestamp());
        data.put("deletedAt", null);

        addressesRef.document(addressId).set(data);
        log.info("Da tao dia chi [{}] cho nguoi dung {} thanh cong", addressId, userId);
        return addressId;
    }

    public void capNhatDiaChi(String userId, String addressId, Address address) {
        log.info("Cap nhat dia chi [{}] cua nguoi dung {} - nhan: {}", addressId, userId, address.getName());
        DocumentReference docRef = getAddressDocument(userId, addressId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", address.getName() != null ? address.getName() : "");
        updates.put("address", address.getAddress() != null ? address.getAddress() : "");
        updates.put("receiverName", address.getReceiverName() != null ? address.getReceiverName() : "");
        updates.put("receiverPhone", address.getReceiverPhone() != null ? address.getReceiverPhone() : "");
        updates.put("lat", address.getLat() != null ? address.getLat() : 0.0);
        updates.put("lng", address.getLng() != null ? address.getLng() : 0.0);
        updates.put("isDefault", address.getIsDefault() != null ? address.getIsDefault() : false);
        updates.put("updatedAt", FieldValue.serverTimestamp());

        docRef.update(updates);

        log.info("Da cap nhat dia chi [{}] cua nguoi dung {} thanh cong", addressId, userId);
    }

    public void datDiaChiMacDinh(String userId, String addressId) {
        log.info("Dat dia chi [{}] lam dia chi mac dinh cho nguoi dung {}", addressId, userId);
        DocumentReference docRef = getAddressDocument(userId, addressId);
        docRef.update(
                "isDefault", true,
                "updatedAt", FieldValue.serverTimestamp()
        );
        log.info("Da dat dia chi [{}] lam dia chi mac dinh thanh cong", addressId);
    }

    public void boDiaChiMacDinh(String userId, String addressId) {
        log.info("Bo dia chi [{}] khoi vai tro mac dinh cho nguoi dung {}", addressId, userId);
        DocumentReference docRef = getAddressDocument(userId, addressId);
        docRef.update(
                "isDefault", false,
                "updatedAt", FieldValue.serverTimestamp()
        );
        log.info("Da bo dia chi [{}] khoi vai tro mac dinh thanh cong", addressId);
    }

    public void xoaDiaChi(String userId, String addressId) {
        log.info("Xoa dia chi [{}] cua nguoi dung {}", addressId, userId);
        DocumentReference docRef = getAddressDocument(userId, addressId);
        docRef.delete();
        log.info("Da xoa dia chi [{}] cua nguoi dung {} thanh cong", addressId, userId);
    }

    public void xoaTatCaDiaChiMacDinh(String userId, String exceptAddressId) throws ExecutionException, InterruptedException {
        log.info("Quet va bo tat ca dia chi mac dinh cua nguoi dung {}, tru ngoai dia chi [{}]", userId, exceptAddressId);
        CollectionReference addressesRef = getAddressesCollection(userId);

        ApiFuture<QuerySnapshot> query = addressesRef.whereEqualTo("isDefault", true).get();
        QuerySnapshot snapshot = query.get();

        if (snapshot.isEmpty()) {
            log.info("Khong co dia chi mac dinh nao cua nguoi dung {}", userId);
            return;
        }

        WriteBatch batch = firestore.batch();
        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            if (!doc.getId().equals(exceptAddressId)) {
                DocumentReference docRef = doc.getReference();
                batch.update(docRef,
                        "isDefault", false,
                        "updatedAt", FieldValue.serverTimestamp()
                );
                log.info("Bo mac dinh dia chi [{}]", doc.getId());
            }
        }

        batch.commit();
        log.info("Da cap nhat {} dia chi cu cua nguoi dung {} thanh cong", snapshot.size(), userId);
    }

    private Address mapToAddress(String addressId, DocumentSnapshot doc) {
        return Address.builder()
                .id(addressId)
                .name(doc.getString("name"))
                .address(doc.getString("address"))
                .receiverName(doc.getString("receiverName"))
                .receiverPhone(doc.getString("receiverPhone"))
                .lat(doc.getDouble("lat"))
                .lng(doc.getDouble("lng"))
                .isDefault(toBoolean(doc.get("isDefault")))
                .createdAt(toInstant(doc.get("createdAt")))
                .updatedAt(toInstant(doc.get("updatedAt")))
                .deletedAt(toInstant(doc.get("deletedAt")))
                .build();
    }

    private Boolean toBoolean(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        return false;
    }

    private Instant toInstant(Object value) {
        if (value == null) return null;
        if (value instanceof com.google.protobuf.Timestamp) {
            com.google.protobuf.Timestamp ts = (com.google.protobuf.Timestamp) value;
            return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
        }
        if (value instanceof java.util.Date) {
            return ((java.util.Date) value).toInstant();
        }
        return null;
    }
}
