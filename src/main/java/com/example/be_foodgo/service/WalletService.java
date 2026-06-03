package com.example.be_foodgo.service;

import com.example.be_foodgo.dto.TransactionDTO;
import com.example.be_foodgo.dto.WalletDTO;
import com.example.be_foodgo.exception.BusinessException;
import com.example.be_foodgo.repository.WalletRepository;
import com.google.cloud.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WalletService {

    private static final Logger log = LoggerFactory.getLogger(WalletService.class);

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public WalletDTO getDriverWallet(String userId) {
        log.info("Bat dau lay vi cua tai xe: {}", userId);
        try {
            List<com.google.cloud.firestore.QueryDocumentSnapshot> wallets = walletRepository
                    .findDriverWalletByUserIdAndRole(userId, "driver");
            if (!wallets.isEmpty()) {
                com.google.cloud.firestore.DocumentSnapshot doc = wallets.get(0);
                return mapToWalletDTO(doc.getId(), doc.getData());
            }

            walletRepository.createDriverWallet(userId);

            log.info("Da tao vi moi cho tai xe: {}", userId);
            return WalletDTO.builder()
                    .userId(userId)
                    .role("driver")
                    .balance(0.0)
                    .totalEarned(0.0)
                    .totalWithdrawn(0.0)
                    .pendingBalance(0.0)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi lay vi: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        } catch (java.util.concurrent.ExecutionException e) {
            log.error("Loi khi lay vi: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        }
    }

    public List<TransactionDTO> getDriverTransactions(String userId, int page, int size) {
        log.info("Bat dau lay lich su giao dich cua tai xe: {}, page={}, size={}", userId, page, size);
        try {
            com.google.cloud.firestore.QueryDocumentSnapshot firstDoc = walletRepository
                    .findTransactionsPaginated(userId, "delivery_income", page, size)
                    .get()
                    .getDocuments()
                    .stream()
                    .findFirst()
                    .orElse(null);

            if (firstDoc == null) {
                log.info("Tim thay 0 giao dich");
                return List.of();
            }

            List<com.google.cloud.firestore.QueryDocumentSnapshot> allInPage = walletRepository
                    .findTransactionsPaginated(userId, "delivery_income", page, size)
                    .get()
                    .getDocuments();

            List<TransactionDTO> result = new java.util.ArrayList<>();
            for (com.google.cloud.firestore.QueryDocumentSnapshot doc : allInPage) {
                result.add(mapToTransactionDTO(doc.getId(), doc.getData()));
            }
            log.info("Tim thay {} giao dich", result.size());
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi lay giao dich: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        } catch (java.util.concurrent.ExecutionException e) {
            log.error("Loi khi lay giao dich: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        }
    }

    public TransactionDTO requestWithdrawal(String userId, double amount) {
        log.info("Bat dau yeu cau rut tien: userId={}, amount={}", userId, amount);
        try {
            List<com.google.cloud.firestore.QueryDocumentSnapshot> walletSnapshots = walletRepository
                    .findDriverWalletByUserIdAndRole(userId, "driver");
            if (walletSnapshots.isEmpty()) {
                throw BusinessException.viKhongTonTai(userId);
            }
            com.google.cloud.firestore.DocumentSnapshot walletDoc = walletSnapshots.get(0);
            String walletId = walletDoc.getId();
            double currentBalance = walletDoc.getDouble("balance") != null
                    ? walletDoc.getDouble("balance") : 0.0;

            if (currentBalance < amount) {
                throw BusinessException.soDuKhongDu(currentBalance, amount);
            }

            Map<String, Object> configData = walletRepository.findSystemConfig();
            double minWithdrawal = configData != null && configData.get("minWithdrawalAmount") != null
                    ? toDouble(configData.get("minWithdrawalAmount")) : 50000.0;
            double maxWithdrawal = configData != null && configData.get("maxWithdrawalAmount") != null
                    ? toDouble(configData.get("maxWithdrawalAmount")) : 50000000.0;

            if (amount < minWithdrawal) {
                throw BusinessException.vuotGioiHanRutTien(
                        String.format("So tien rut toi thieu la %.0f VND.", minWithdrawal));
            }
            if (amount > maxWithdrawal) {
                throw BusinessException.vuotGioiHanRutTien(
                        String.format("So tien rut toi da la %.0f VND.", maxWithdrawal));
            }

            String transId = walletRepository.withdrawInTransaction(walletId, userId, amount);

            com.google.cloud.firestore.DocumentSnapshot createdTrans = walletRepository.getFirestore()
                    .collection("transactions")
                    .document(transId)
                    .get()
                    .get();
            log.info("Yeu cau rut tien thanh cong: transId={}", transId);
            return mapToTransactionDTO(transId, createdTrans.getData());
        } catch (BusinessException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi rut tien: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        } catch (java.util.concurrent.ExecutionException e) {
            log.error("Loi khi rut tien: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        }
    }

    public WalletDTO getMerchantWallet(String userId) {
        log.info("Bat dau lay vi cua gian hang: {}", userId);
        try {
            List<com.google.cloud.firestore.QueryDocumentSnapshot> wallets = walletRepository
                    .findDriverWalletByUserIdAndRole(userId, "merchant");
            if (!wallets.isEmpty()) {
                com.google.cloud.firestore.DocumentSnapshot doc = wallets.get(0);
                return mapToWalletDTO(doc.getId(), doc.getData());
            }

            walletRepository.createMerchantWallet(userId);

            log.info("Da tao vi moi cho gian hang: {}", userId);
            return WalletDTO.builder()
                    .userId(userId)
                    .role("merchant")
                    .balance(0.0)
                    .totalEarned(0.0)
                    .totalWithdrawn(0.0)
                    .pendingBalance(0.0)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi lay vi: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        } catch (java.util.concurrent.ExecutionException e) {
            log.error("Loi khi lay vi: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        }
    }

    public List<TransactionDTO> getMerchantTransactions(String userId, int page, int size) {
        log.info("Bat dau lay lich su giao dich cua gian hang: {}, page={}, size={}", userId, page, size);
        try {
            com.google.cloud.firestore.QueryDocumentSnapshot firstDoc = walletRepository
                    .findAllTransactionsPaginated(userId, page, size)
                    .get()
                    .getDocuments()
                    .stream()
                    .findFirst()
                    .orElse(null);

            if (firstDoc == null) {
                log.info("Tim thay 0 giao dich");
                return List.of();
            }

            List<com.google.cloud.firestore.QueryDocumentSnapshot> allInPage = walletRepository
                    .findAllTransactionsPaginated(userId, page, size)
                    .get()
                    .getDocuments();

            List<TransactionDTO> result = new java.util.ArrayList<>();
            for (com.google.cloud.firestore.QueryDocumentSnapshot doc : allInPage) {
                result.add(mapToTransactionDTO(doc.getId(), doc.getData()));
            }
            log.info("Tim thay {} giao dich", result.size());
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi lay giao dich: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        } catch (java.util.concurrent.ExecutionException e) {
            log.error("Loi khi lay giao dich: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        }
    }

    public TransactionDTO requestMerchantWithdrawal(String userId, double amount) {
        log.info("Bat dau yeu cau rut tien (merchant): userId={}, amount={}", userId, amount);
        try {
            List<com.google.cloud.firestore.QueryDocumentSnapshot> walletSnapshots = walletRepository
                    .findDriverWalletByUserIdAndRole(userId, "merchant");
            if (walletSnapshots.isEmpty()) {
                throw BusinessException.viKhongTonTai(userId);
            }
            com.google.cloud.firestore.DocumentSnapshot walletDoc = walletSnapshots.get(0);
            String walletId = walletDoc.getId();
            double currentBalance = walletDoc.getDouble("balance") != null
                    ? walletDoc.getDouble("balance") : 0.0;

            if (currentBalance < amount) {
                throw BusinessException.soDuKhongDu(currentBalance, amount);
            }

            Map<String, Object> configData = walletRepository.findSystemConfig();
            double minWithdrawal = configData != null && configData.get("minWithdrawalAmount") != null
                    ? toDouble(configData.get("minWithdrawalAmount")) : 50000.0;
            double maxWithdrawal = configData != null && configData.get("maxWithdrawalAmount") != null
                    ? toDouble(configData.get("maxWithdrawalAmount")) : 50000000.0;

            if (amount < minWithdrawal) {
                throw BusinessException.vuotGioiHanRutTien(
                        String.format("So tien rut toi thieu la %.0f VND.", minWithdrawal));
            }
            if (amount > maxWithdrawal) {
                throw BusinessException.vuotGioiHanRutTien(
                        String.format("So tien rut toi da la %.0f VND.", maxWithdrawal));
            }

            String transId = walletRepository.withdrawInTransaction(walletId, userId, amount);

            com.google.cloud.firestore.DocumentSnapshot createdTrans = walletRepository.getFirestore()
                    .collection("transactions")
                    .document(transId)
                    .get()
                    .get();
            log.info("Yeu cau rut tien (merchant) thanh cong: transId={}", transId);
            return mapToTransactionDTO(transId, createdTrans.getData());
        } catch (BusinessException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi rut tien (merchant): {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        } catch (java.util.concurrent.ExecutionException e) {
            log.error("Loi khi rut tien (merchant): {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        }
    }

    public void taoGiaoDichThuNhap(String driverId, String orderId, double deliveryFee) {
        try {
            String walletId = null;
            List<com.google.cloud.firestore.QueryDocumentSnapshot> wallets = walletRepository
                    .findDriverWalletByUserIdAndRole(driverId, "driver");
            if (!wallets.isEmpty()) {
                walletId = wallets.get(0).getId();
            }

            Map<String, Object> transData = new HashMap<>();
            transData.put("walletId", walletId);
            transData.put("userId", driverId);
            transData.put("type", "delivery_income");
            transData.put("amount", deliveryFee);
            transData.put("fee", 0.0);
            transData.put("netAmount", deliveryFee);
            transData.put("description", "Thu nhap giao hang cho don hang [" + orderId + "]");
            transData.put("orderId", orderId);
            transData.put("status", "completed");
            transData.put("createdAt", Instant.now());

            walletRepository.createTransaction(transData);

            if (walletId != null) {
                Map<String, Object> walletUpdates = new HashMap<>();
                walletUpdates.put("balance",
                        com.google.cloud.firestore.FieldValue.increment(deliveryFee));
                walletUpdates.put("totalEarned",
                        com.google.cloud.firestore.FieldValue.increment(deliveryFee));
                walletUpdates.put("updatedAt", Instant.now());
                walletRepository.updateWalletFields(walletId, walletUpdates);
            }

            log.info("Da tao giao dich thu nhap: driverId={}, orderId={}, amount={}", driverId, orderId, deliveryFee);
        } catch (Exception e) {
            log.warn("Loi khi tao giao dich thu nhap: {}", e.getMessage());
        }
    }

    public List<TransactionDTO> getPendingWithdrawals() {
        log.info("Admin bat dau lay danh sach cac yeu cau rut tien dang cho");
        try {
            com.google.cloud.firestore.Firestore firestore = walletRepository.getFirestore();
            List<com.google.cloud.firestore.QueryDocumentSnapshot> docs;
            try {
                docs = firestore.collection("transactions")
                        .whereEqualTo("type", "withdrawal")
                        .whereEqualTo("status", "pending")
                        .orderBy("createdAt", com.google.cloud.firestore.Query.Direction.DESCENDING)
                        .get()
                        .get()
                        .getDocuments();
            } catch (Exception e) {
                log.warn("Loi index Firestore khi lay pending withdrawals, thuc hien fallback khong orderBy va tu sap xep in-memory: {}", e.getMessage());
                docs = firestore.collection("transactions")
                        .whereEqualTo("type", "withdrawal")
                        .whereEqualTo("status", "pending")
                        .get()
                        .get()
                        .getDocuments();
            }

            List<TransactionDTO> result = new java.util.ArrayList<>();
            for (com.google.cloud.firestore.QueryDocumentSnapshot doc : docs) {
                result.add(mapToTransactionDTO(doc.getId(), doc.getData()));
            }
            
            // Sap xep in-memory giam dan theo createdAt
            result.sort((a, b) -> {
                if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
                if (a.getCreatedAt() == null) return 1;
                if (b.getCreatedAt() == null) return -1;
                return b.getCreatedAt().compareTo(a.getCreatedAt());
            });
            
            log.info("Tim thay {} yeu cau rut tien pending", result.size());
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi lay giao dich pending: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        } catch (java.util.concurrent.ExecutionException e) {
            log.error("Loi khi lay giao dich pending: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        }
    }

    public void approveWithdrawal(String transactionId) {
        log.info("Admin bat dau duyet yeu cau rut tien: {}", transactionId);
        try {
            com.google.cloud.firestore.Firestore firestore = walletRepository.getFirestore();
            com.google.cloud.firestore.DocumentReference transRef = firestore.collection("transactions").document(transactionId);
            com.google.cloud.firestore.DocumentSnapshot transDoc = transRef.get().get();

            if (!transDoc.exists()) {
                throw new IllegalArgumentException("Khong tim thay giao dich rut tien voi ID: " + transactionId);
            }

            String status = transDoc.getString("status");
            if (!"pending".equals(status)) {
                throw new IllegalArgumentException("Giao dich khong o trang thai cho duyet.");
            }

            String walletId = transDoc.getString("walletId");
            Double amount = transDoc.getDouble("amount");
            if (walletId == null || amount == null) {
                throw new IllegalArgumentException("Giao dich khong hop le.");
            }

            com.google.cloud.firestore.DocumentReference walletRef = firestore.collection("wallets").document(walletId);
            com.google.cloud.firestore.DocumentSnapshot walletDoc = walletRef.get().get();
            if (!walletDoc.exists()) {
                throw new IllegalArgumentException("Khong tim thay vi lien ket.");
            }

            Double pendingBalance = walletDoc.getDouble("pendingBalance") != null ? walletDoc.getDouble("pendingBalance") : 0.0;
            Double totalWithdrawn = walletDoc.getDouble("totalWithdrawn") != null ? walletDoc.getDouble("totalWithdrawn") : 0.0;

            com.google.cloud.firestore.WriteBatch batch = firestore.batch();
            
            // Cap nhat giao dich
            batch.update(transRef, "status", "completed");
            
            // Cap nhat vi
            Map<String, Object> walletUpdates = new HashMap<>();
            walletUpdates.put("pendingBalance", Math.max(0.0, pendingBalance - amount));
            walletUpdates.put("totalWithdrawn", totalWithdrawn + amount);
            walletUpdates.put("updatedAt", Instant.now());
            batch.update(walletRef, walletUpdates);

            batch.commit().get();
            log.info("Da duyet thanh cong yeu cau rut tien: {}", transactionId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi duyet rut tien: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        } catch (java.util.concurrent.ExecutionException e) {
            log.error("Loi khi duyet rut tien: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        }
    }

    public void rejectWithdrawal(String transactionId, String reason) {
        log.info("Admin bat dau tu choi yeu cau rut tien: {}, ly do: {}", transactionId, reason);
        try {
            com.google.cloud.firestore.Firestore firestore = walletRepository.getFirestore();
            com.google.cloud.firestore.DocumentReference transRef = firestore.collection("transactions").document(transactionId);
            com.google.cloud.firestore.DocumentSnapshot transDoc = transRef.get().get();

            if (!transDoc.exists()) {
                throw new IllegalArgumentException("Khong tim thay giao dich rut tien voi ID: " + transactionId);
            }

            String status = transDoc.getString("status");
            if (!"pending".equals(status)) {
                throw new IllegalArgumentException("Giao dich khong o trang thai cho duyet.");
            }

            String walletId = transDoc.getString("walletId");
            Double amount = transDoc.getDouble("amount");
            if (walletId == null || amount == null) {
                throw new IllegalArgumentException("Giao dich khong hop le.");
            }

            com.google.cloud.firestore.DocumentReference walletRef = firestore.collection("wallets").document(walletId);
            com.google.cloud.firestore.DocumentSnapshot walletDoc = walletRef.get().get();
            if (!walletDoc.exists()) {
                throw new IllegalArgumentException("Khong tim thay vi lien ket.");
            }

            Double balance = walletDoc.getDouble("balance") != null ? walletDoc.getDouble("balance") : 0.0;
            Double pendingBalance = walletDoc.getDouble("pendingBalance") != null ? walletDoc.getDouble("pendingBalance") : 0.0;

            com.google.cloud.firestore.WriteBatch batch = firestore.batch();
            
            // Cap nhat giao dich
            batch.update(transRef, "status", "failed");
            batch.update(transRef, "description", "Tu choi rut tien: " + reason);
            
            // Cap nhat vi: hoan tien
            Map<String, Object> walletUpdates = new HashMap<>();
            walletUpdates.put("balance", balance + amount);
            walletUpdates.put("pendingBalance", Math.max(0.0, pendingBalance - amount));
            walletUpdates.put("updatedAt", Instant.now());
            batch.update(walletRef, walletUpdates);

            batch.commit().get();
            log.info("Da tu choi yeu cau rut tien: {}", transactionId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi tu choi rut tien: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        } catch (java.util.concurrent.ExecutionException e) {
            log.error("Loi khi tu choi rut tien: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        }
    }

    private WalletDTO mapToWalletDTO(String id, Map<String, Object> data) {
        if (data == null) {
            return WalletDTO.builder().id(id).build();
        }
        return WalletDTO.builder()
                .id(id)
                .userId((String) data.get("userId"))
                .role((String) data.get("role"))
                .balance(toDouble(data.get("balance")))
                .totalEarned(toDouble(data.get("totalEarned")))
                .totalWithdrawn(toDouble(data.get("totalWithdrawn")))
                .pendingBalance(toDouble(data.get("pendingBalance")))
                .createdAt(toInstant(data.get("createdAt")))
                .updatedAt(toInstant(data.get("updatedAt")))
                .build();
    }

    private TransactionDTO mapToTransactionDTO(String id, Map<String, Object> data) {
        if (data == null) {
            return TransactionDTO.builder().id(id).build();
        }
        return TransactionDTO.builder()
                .id(id)
                .walletId((String) data.get("walletId"))
                .userId((String) data.get("userId"))
                .type((String) data.get("type"))
                .amount(toDouble(data.get("amount")))
                .fee(toDouble(data.get("fee")))
                .netAmount(toDouble(data.get("netAmount")))
                .description((String) data.get("description"))
                .orderId((String) data.get("orderId"))
                .status((String) data.get("status"))
                .createdAt(toInstant(data.get("createdAt")))
                .build();
    }

    private Double toDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return null;
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        return null;
    }

    private Instant toInstant(Object value) {
        if (value == null) return null;
        if (value instanceof Timestamp) return ((Timestamp) value).toDate().toInstant();
        if (value instanceof java.util.Date) return ((java.util.Date) value).toInstant();
        if (value instanceof Long) return Instant.ofEpochMilli((Long) value);
        return null;
    }
}
