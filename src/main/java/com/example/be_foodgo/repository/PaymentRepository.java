package com.example.be_foodgo.repository;

import com.example.be_foodgo.model.PaymentMethod;
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
public class PaymentRepository {

    private static final Logger log = LoggerFactory.getLogger(PaymentRepository.class);

    private static final String CUSTOMER_PROFILES_COLLECTION = "customer_profiles";
    private static final String PAYMENT_METHODS_SUB_COLLECTION = "payment_methods";

    private final Firestore firestore;

    public PaymentRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    private CollectionReference getPaymentMethodsCollection(String userId) {
        return firestore
                .collection(CUSTOMER_PROFILES_COLLECTION)
                .document(userId)
                .collection(PAYMENT_METHODS_SUB_COLLECTION);
    }

    public DocumentReference getPaymentMethodDocument(String userId, String paymentMethodId) {
        return getPaymentMethodsCollection(userId).document(paymentMethodId);
    }

    public List<PaymentMethod> layTatCaPhuongThuc(String userId) throws ExecutionException, InterruptedException {
        log.info("Truy van tat ca phuong thuc thanh toan cua nguoi dung: {}", userId);
        CollectionReference paymentMethodsRef = getPaymentMethodsCollection(userId);

        ApiFuture<QuerySnapshot> query = paymentMethodsRef.get();
        QuerySnapshot snapshot = query.get();

        List<PaymentMethod> paymentMethods = new ArrayList<>();
        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            PaymentMethod pm = mapToPaymentMethod(doc.getId(), doc);
            paymentMethods.add(pm);
        }

        log.info("Tim thay {} phuong thuc thanh toan cua nguoi dung {}", paymentMethods.size(), userId);
        return paymentMethods;
    }

    public PaymentMethod layMotPhuongThuc(String userId, String paymentMethodId)
            throws ExecutionException, InterruptedException {
        log.info("Truy van mot phuong thuc thanh toan - userId: {}, paymentMethodId: {}", userId, paymentMethodId);
        DocumentReference docRef = getPaymentMethodDocument(userId, paymentMethodId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot doc = future.get();

        if (!doc.exists()) {
            log.warn("Khong tim thay phuong thuc thanh toan [{}] cua nguoi dung {}", paymentMethodId, userId);
            return null;
        }

        PaymentMethod pm = mapToPaymentMethod(doc.getId(), doc);
        log.info("Tim thay phuong thuc thanh toan [{}] cua nguoi dung {}", paymentMethodId, userId);
        return pm;
    }

    public String taoPhuongThuc(String userId, PaymentMethod paymentMethod) {
        log.info("Tao phuong thuc thanh toan moi cho nguoi dung {} - ten: {}, type: {}",
                userId, paymentMethod.getName(), paymentMethod.getType());
        CollectionReference paymentMethodsRef = getPaymentMethodsCollection(userId);
        String paymentMethodId = paymentMethodsRef.document().getId();

        Map<String, Object> data = new HashMap<>();
        data.put("name", paymentMethod.getName() != null ? paymentMethod.getName() : "");
        data.put("type", paymentMethod.getType() != null ? paymentMethod.getType() : "");
        data.put("details", paymentMethod.getDetails() != null ? paymentMethod.getDetails() : "");
        data.put("isDefault", paymentMethod.getIsDefault() != null ? paymentMethod.getIsDefault() : false);
        data.put("cardBrand", paymentMethod.getCardBrand() != null ? paymentMethod.getCardBrand() : null);
        data.put("last4Digits", paymentMethod.getLast4Digits() != null ? paymentMethod.getLast4Digits() : null);
        data.put("walletBrand", paymentMethod.getWalletBrand() != null ? paymentMethod.getWalletBrand() : null);
        data.put("isLinked", paymentMethod.getIsLinked() != null ? paymentMethod.getIsLinked() : false);
        data.put("createdAt", FieldValue.serverTimestamp());
        data.put("updatedAt", FieldValue.serverTimestamp());

        paymentMethodsRef.document(paymentMethodId).set(data);
        log.info("Da tao phuong thuc thanh toan [{}] cho nguoi dung {} thanh cong", paymentMethodId, userId);
        return paymentMethodId;
    }

    public String taoPhuongThucVoiBatch(String userId, PaymentMethod paymentMethod, WriteBatch batch) {
        log.info("Tao phuong thuc thanh toan moi voi batch cho nguoi dung {} - ten: {}, type: {}",
                userId, paymentMethod.getName(), paymentMethod.getType());
        CollectionReference paymentMethodsRef = getPaymentMethodsCollection(userId);
        String paymentMethodId = paymentMethodsRef.document().getId();

        Map<String, Object> data = new HashMap<>();
        data.put("name", paymentMethod.getName() != null ? paymentMethod.getName() : "");
        data.put("type", paymentMethod.getType() != null ? paymentMethod.getType() : "");
        data.put("details", paymentMethod.getDetails() != null ? paymentMethod.getDetails() : "");
        data.put("isDefault", paymentMethod.getIsDefault() != null ? paymentMethod.getIsDefault() : false);
        data.put("cardBrand", paymentMethod.getCardBrand() != null ? paymentMethod.getCardBrand() : null);
        data.put("last4Digits", paymentMethod.getLast4Digits() != null ? paymentMethod.getLast4Digits() : null);
        data.put("walletBrand", paymentMethod.getWalletBrand() != null ? paymentMethod.getWalletBrand() : null);
        data.put("isLinked", paymentMethod.getIsLinked() != null ? paymentMethod.getIsLinked() : false);
        data.put("createdAt", FieldValue.serverTimestamp());
        data.put("updatedAt", FieldValue.serverTimestamp());

        DocumentReference newDocRef = paymentMethodsRef.document(paymentMethodId);
        batch.set(newDocRef, data);
        log.info("Da tao phuong thuc thanh toan [{}] voi batch cho nguoi dung {}", paymentMethodId, userId);
        return paymentMethodId;
    }

    public void datPhuongThucMacDinh(String userId, String paymentMethodId) {
        log.info("Dat phuong thuc thanh toan [{}] lam mac dinh cho nguoi dung {}", paymentMethodId, userId);
        DocumentReference docRef = getPaymentMethodDocument(userId, paymentMethodId);
        docRef.update(
                "isDefault", true,
                "updatedAt", FieldValue.serverTimestamp()
        );
        log.info("Da dat phuong thuc thanh toan [{}] lam mac dinh thanh cong", paymentMethodId);
    }

    public void boPhuongThucMacDinh(String userId, String paymentMethodId) {
        log.info("Bo phuong thuc thanh toan [{}] khoi vai tro mac dinh cho nguoi dung {}", paymentMethodId, userId);
        DocumentReference docRef = getPaymentMethodDocument(userId, paymentMethodId);
        docRef.update(
                "isDefault", false,
                "updatedAt", FieldValue.serverTimestamp()
        );
        log.info("Da bo phuong thuc thanh toan [{}] khoi vai tro mac dinh thanh cong", paymentMethodId);
    }

    public void xoaPhuongThuc(String userId, String paymentMethodId) {
        log.info("Xoa phuong thuc thanh toan [{}] cua nguoi dung {}", paymentMethodId, userId);
        DocumentReference docRef = getPaymentMethodDocument(userId, paymentMethodId);
        docRef.delete();
        log.info("Da xoa phuong thuc thanh toan [{}] cua nguoi dung {} thanh cong", paymentMethodId, userId);
    }

    public List<PaymentMethod> layTatCaPhuongThucMacDinh(String userId)
            throws ExecutionException, InterruptedException {
        log.info("Quet tat ca phuong thuc thanh toan mac dinh cua nguoi dung {}", userId);
        CollectionReference paymentMethodsRef = getPaymentMethodsCollection(userId);

        ApiFuture<QuerySnapshot> query = paymentMethodsRef.whereEqualTo("isDefault", true).get();
        QuerySnapshot snapshot = query.get();

        List<PaymentMethod> paymentMethods = new ArrayList<>();
        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            PaymentMethod pm = mapToPaymentMethod(doc.getId(), doc);
            paymentMethods.add(pm);
        }

        log.info("Tim thay {} phuong thuc thanh toan mac dinh cua nguoi dung {}", paymentMethods.size(), userId);
        return paymentMethods;
    }

    public WriteBatch taoWriteBatch() {
        return firestore.batch();
    }

    public void commitBatch(WriteBatch batch) throws ExecutionException, InterruptedException {
        batch.commit().get();
        log.info("Batch da duoc commit thanh cong");
    }

    private PaymentMethod mapToPaymentMethod(String paymentMethodId, DocumentSnapshot doc) {
        return PaymentMethod.builder()
                .id(paymentMethodId)
                .name(doc.getString("name"))
                .type(doc.getString("type"))
                .details(doc.getString("details"))
                .isDefault(toBoolean(doc.get("isDefault")))
                .cardBrand(doc.getString("cardBrand"))
                .last4Digits(doc.getString("last4Digits"))
                .walletBrand(doc.getString("walletBrand"))
                .isLinked(toBoolean(doc.get("isLinked")))
                .createdAt(toInstant(doc.get("createdAt")))
                .updatedAt(toInstant(doc.get("updatedAt")))
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
