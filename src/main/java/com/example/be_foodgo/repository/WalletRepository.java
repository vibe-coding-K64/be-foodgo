package com.example.be_foodgo.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteBatch;
import com.example.be_foodgo.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Repository
public class WalletRepository {

    private static final Logger log = LoggerFactory.getLogger(WalletRepository.class);

    private static final String COLLECTION_DRIVER_PROFILES = "driver_profiles";
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_WALLETS = "wallets";
    private static final String COLLECTION_TRANSACTIONS = "transactions";
    private static final String COLLECTION_SYSTEM_CONFIGS = "system_configs";

    private final Firestore firestore;

    public WalletRepository(Firestore firestore) {
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

    public List<Map<String, Object>> findActiveDriverProfiles() throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> docs = firestore.collection(COLLECTION_DRIVER_PROFILES)
                .whereEqualTo("isActive", true)
                .get()
                .get()
                .getDocuments();
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (QueryDocumentSnapshot doc : docs) {
            Map<String, Object> data = new java.util.HashMap<>(doc.getData());
            data.put("id", doc.getId());
            result.add(data);
        }
        return result;
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

    public List<QueryDocumentSnapshot> findDriverWalletByUserIdAndRole(String userId, Object role)
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
        String walletId = generateNextWalletId();
        DocumentReference newDocRef = firestore.collection(COLLECTION_WALLETS).document(walletId);

        Map<String, Object> walletData = new HashMap<>();
        walletData.put("id", walletId);
        walletData.put("userId", userId);
        walletData.put("role", "driver");
        walletData.put("balance", 0.0);
        walletData.put("totalEarned", 0.0);
        walletData.put("totalWithdrawn", 0.0);
        walletData.put("pendingBalance", 0.0);
        walletData.put("createdAt", com.google.cloud.firestore.FieldValue.serverTimestamp());
        walletData.put("updatedAt", com.google.cloud.firestore.FieldValue.serverTimestamp());

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
            String userId, Integer type, int page, int size) {
        return firestore.collection(COLLECTION_TRANSACTIONS)
                .whereEqualTo("userId", userId)
                .whereEqualTo("type", type)
                .get();
    }

    public ApiFuture<QuerySnapshot> findAllTransactionsPaginated(
            String userId, int page, int size) {
        return firestore.collection(COLLECTION_TRANSACTIONS)
                .whereEqualTo("userId", userId)
                .get();
    }

    private String generateNextWalletId() throws ExecutionException, InterruptedException {
        QuerySnapshot snapshot = firestore.collection(COLLECTION_WALLETS)
                .orderBy("id", com.google.cloud.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .get();

        if (snapshot.isEmpty()) {
            return "wallet_001";
        }

        String lastId = snapshot.getDocuments().get(0).getId();
        if (lastId.startsWith("wallet_")) {
            try {
                int num = Integer.parseInt(lastId.substring(7));
                return String.format("wallet_%03d", num + 1);
            } catch (NumberFormatException e) {
                log.warn("Invalid wallet id format: {}", lastId);
            }
        }
        return "wallet_" + System.currentTimeMillis();
    }

    public String createMerchantWallet(String userId) throws ExecutionException, InterruptedException {
        String walletId = generateNextWalletId();
        DocumentReference newDocRef = firestore.collection(COLLECTION_WALLETS).document(walletId);

        Map<String, Object> walletData = new HashMap<>();
        walletData.put("id", walletId);
        walletData.put("userId", userId);
        walletData.put("role", 1);
        walletData.put("balance", 0.0);
        walletData.put("totalEarned", 0.0);
        walletData.put("totalWithdrawn", 0.0);
        walletData.put("pendingBalance", 0.0);
        walletData.put("createdAt", com.google.cloud.firestore.FieldValue.serverTimestamp());
        walletData.put("updatedAt", com.google.cloud.firestore.FieldValue.serverTimestamp());

        newDocRef.set(walletData).get();
        log.info("Da tao vi moi cho gian hang {}: walletId={}", userId, walletId);
        return walletId;
    }

    private String generateNextTransactionId() throws ExecutionException, InterruptedException {
        QuerySnapshot snapshot = firestore.collection(COLLECTION_TRANSACTIONS)
                .orderBy("id", com.google.cloud.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .get();

        if (snapshot.isEmpty()) {
            return "trans_001";
        }

        String lastId = snapshot.getDocuments().get(0).getId();
        if (lastId.startsWith("trans_")) {
            try {
                int num = Integer.parseInt(lastId.substring(6));
                return String.format("trans_%03d", num + 1);
            } catch (NumberFormatException e) {
                log.warn("Invalid transaction id format: {}", lastId);
            }
        }
        return "trans_" + System.currentTimeMillis();
    }

    public String createTransaction(Map<String, Object> data)
            throws ExecutionException, InterruptedException {
        String transId = generateNextTransactionId();
        data.put("id", transId);
        firestore.collection(COLLECTION_TRANSACTIONS).document(transId).set(data).get();
        return transId;
    }

    public ApiFuture<QuerySnapshot> findAllDeliveryTransactionsByUserId(String userId) {
        return firestore.collection(COLLECTION_TRANSACTIONS)
                .whereEqualTo("userId", userId)
                .whereEqualTo("type", 2)
                .whereEqualTo("status", 1)
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

    public String withdrawInTransaction(String walletId, String userId, double amount, String description)
            throws ExecutionException, InterruptedException {

        DocumentReference walletRef = firestore.collection(COLLECTION_WALLETS).document(walletId);
        final String transId = generateNextTransactionId();
        DocumentReference transRef = firestore.collection(COLLECTION_TRANSACTIONS).document(transId);

        firestore.runTransaction(transaction -> {
            DocumentSnapshot walletDoc = transaction.get(walletRef).get();
            double balance = walletDoc.getDouble("balance") != null ? walletDoc.getDouble("balance") : 0.0;

            if (balance < amount) {
                throw BusinessException.soDuKhongDu(balance, amount);
            }

            Map<String, Object> transData = new HashMap<>();
            transData.put("id", transId);
            transData.put("walletId", walletId);
            transData.put("userId", userId);
            transData.put("type", 3);
            transData.put("amount", amount);
            transData.put("fee", 0.0);
            transData.put("netAmount", amount);
            transData.put("description", description != null ? description : "Yêu cầu rút tiền tài khoản.");
            transData.put("status", 0);
            transData.put("createdAt", com.google.cloud.firestore.FieldValue.serverTimestamp());
            transaction.set(transRef, transData);

            double currentPending = walletDoc.getDouble("pendingBalance") != null
                    ? walletDoc.getDouble("pendingBalance") : 0.0;

            Map<String, Object> walletUpdates = new HashMap<>();
            walletUpdates.put("balance", balance - amount);
            walletUpdates.put("pendingBalance", currentPending + amount);
            walletUpdates.put("updatedAt", com.google.cloud.firestore.FieldValue.serverTimestamp());
            transaction.update(walletRef, walletUpdates);

            return null;
        }).get();

        return transId;
    }
}
