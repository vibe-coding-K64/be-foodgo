package com.example.be_foodgo.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteBatch;
import com.example.be_foodgo.exception.InsufficientBalanceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Repository
public class DriverRepository {

    private static final Logger log = LoggerFactory.getLogger(DriverRepository.class);

    private static final String COLLECTION_DRIVER_PROFILES = "driver_profiles";
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_WALLETS = "wallets";
    private static final String COLLECTION_TRANSACTIONS = "transactions";
    private static final String COLLECTION_SYSTEM_CONFIGS = "system_configs";

    private final Firestore firestore;

    public DriverRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public Firestore getFirestore() {
        return firestore;
    }

    public Map<String, Object> findDriverProfileById(String userId)
            throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = firestore
                .collection(COLLECTION_DRIVER_PROFILES)
                .document(userId)
                .get()
                .get();
        return doc.exists() ? doc.getData() : null;
    }

    public Map<String, Object> findUserById(String userId)
            throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = firestore
                .collection(COLLECTION_USERS)
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

    public void updateUserFields(String userId, Map<String, Object> fields)
            throws ExecutionException, InterruptedException {
        if (fields == null || fields.isEmpty()) return;
        firestore.collection(COLLECTION_USERS)
                .document(userId)
                .update(fields)
                .get();
    }

    public WriteBatch createBatch() {
        return firestore.batch();
    }

    public void commitBatch(WriteBatch batch) throws ExecutionException, InterruptedException {
        batch.commit().get();
    }

    public List<QueryDocumentSnapshot> findDriverWalletByUserIdAndRole(String userId, String role)
            throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION_WALLETS)
                .whereEqualTo("userId", userId)
                .whereEqualTo("role", role)
                .limit(1)
                .get()
                .get()
                .getDocuments();
    }

    public String createDriverWallet(String userId) throws ExecutionException, InterruptedException {
        DocumentReference newDocRef = firestore.collection(COLLECTION_WALLETS).document();
        String walletId = newDocRef.getId();

        Map<String, Object> walletData = new HashMap<>();
        walletData.put("id", walletId);
        walletData.put("userId", userId);
        walletData.put("role", "driver");
        walletData.put("balance", 0.0);
        walletData.put("totalEarned", 0.0);
        walletData.put("totalWithdrawn", 0.0);
        walletData.put("pendingBalance", 0.0);
        walletData.put("createdAt", Instant.now());
        walletData.put("updatedAt", Instant.now());

        newDocRef.set(walletData).get();
        log.info("Da tao vi moi cho tai xe {}: walletId={}", userId, walletId);
        return walletId;
    }

    public void updateWalletFields(String walletId, Map<String, Object> fields)
            throws ExecutionException, InterruptedException {
        if (fields == null || fields.isEmpty()) return;
        firestore.collection(COLLECTION_WALLETS)
                .document(walletId)
                .update(fields)
                .get();
    }

    public ApiFuture<QuerySnapshot> findTransactionsPaginated(
            String userId, String type, int page, int size) {
        return firestore.collection(COLLECTION_TRANSACTIONS)
                .whereEqualTo("userId", userId)
                .whereEqualTo("type", type)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .offset(page * size)
                .limit(size)
                .get();
    }

    public String createTransaction(Map<String, Object> data)
            throws ExecutionException, InterruptedException {
        ApiFuture<DocumentReference> ref = firestore.collection(COLLECTION_TRANSACTIONS).add(data);
        return ref.get().getId();
    }

    public ApiFuture<QuerySnapshot> findAllDeliveryTransactionsByUserId(String userId) {
        return firestore.collection(COLLECTION_TRANSACTIONS)
                .whereEqualTo("userId", userId)
                .whereEqualTo("type", "delivery_income")
                .whereEqualTo("status", "completed")
                .get();
    }

    public ApiFuture<QuerySnapshot> findAllTransactionsByUserId(String userId) {
        return firestore.collection(COLLECTION_TRANSACTIONS)
                .whereEqualTo("userId", userId)
                .get();
    }

    public Map<String, Object> findSystemConfig() throws ExecutionException, InterruptedException {
        QuerySnapshot snapshots = firestore.collection(COLLECTION_SYSTEM_CONFIGS)
                .limit(1)
                .get()
                .get();
        if (snapshots.isEmpty()) return null;
        return snapshots.getDocuments().get(0).getData();
    }

    public String withdrawInTransaction(String walletId, String userId, double amount)
            throws ExecutionException, InterruptedException {

        DocumentReference walletRef = firestore.collection(COLLECTION_WALLETS).document(walletId);
        DocumentReference transRef = firestore.collection(COLLECTION_TRANSACTIONS).document();
        final String transId = transRef.getId();

        firestore.runTransaction(transaction -> {
            DocumentSnapshot walletDoc = transaction.get(walletRef).get();
            double balance = walletDoc.getDouble("balance") != null ? walletDoc.getDouble("balance") : 0.0;

            if (balance < amount) {
                throw new InsufficientBalanceException(
                        "INSUFFICIENT_BALANCE");
            }

            Map<String, Object> transData = new HashMap<>();
            transData.put("id", transId);
            transData.put("walletId", walletId);
            transData.put("userId", userId);
            transData.put("type", "withdrawal");
            transData.put("amount", amount);
            transData.put("fee", 0.0);
            transData.put("netAmount", amount);
            transData.put("description", "Yeu cau rut tien tai khoan.");
            transData.put("status", "pending");
            transData.put("createdAt", Instant.now());
            transaction.set(transRef, transData);

            double currentPending = walletDoc.getDouble("pendingBalance") != null
                    ? walletDoc.getDouble("pendingBalance") : 0.0;

            Map<String, Object> walletUpdates = new HashMap<>();
            walletUpdates.put("balance", balance - amount);
            walletUpdates.put("pendingBalance", currentPending + amount);
            walletUpdates.put("updatedAt", Instant.now());
            transaction.update(walletRef, walletUpdates);

            return null;
        }).get();

        return transId;
    }
}
