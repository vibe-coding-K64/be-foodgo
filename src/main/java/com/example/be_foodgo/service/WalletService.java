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
            List<com.google.cloud.firestore.QueryDocumentSnapshot> allDocs = walletRepository
                    .findTransactionsPaginated(userId, 2, page, size)
                    .get()
                    .getDocuments();

            List<TransactionDTO> result = new java.util.ArrayList<>();
            for (com.google.cloud.firestore.QueryDocumentSnapshot doc : allDocs) {
                result.add(mapToTransactionDTO(doc.getId(), doc.getData()));
            }
            
            result.sort((t1, t2) -> {
                if (t1.getCreatedAt() == null || t2.getCreatedAt() == null) return 0;
                return t2.getCreatedAt().compareTo(t1.getCreatedAt());
            });
            
            int start = Math.min(page * size, result.size());
            int end = Math.min(start + size, result.size());
            List<TransactionDTO> paginated = result.subList(start, end);
            
            log.info("Tim thay {} giao dich (tong: {})", paginated.size(), result.size());
            return paginated;
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

            String description = "Yêu cầu rút tiền tài khoản.";
            String transId = walletRepository.withdrawInTransaction(walletId, userId, amount, description);

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
        log.info("Bat dau lay thong tin vi cua gian hang: {}", userId);
        try {
            List<com.google.cloud.firestore.QueryDocumentSnapshot> wallets = walletRepository
                    .findDriverWalletByUserIdAndRole(userId, 1);
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

    public List<TransactionDTO> getMerchantTransactions(String userId, Integer type, int page, int size) {
        log.info("Bat dau lay lich su giao dich cua gian hang: {}, type={}, page={}, size={}", userId, type, page, size);
        try {
            List<com.google.cloud.firestore.QueryDocumentSnapshot> allDocs;
            if (type != null) {
                allDocs = walletRepository.findTransactionsPaginated(userId, type, page, size)
                        .get().getDocuments();
            } else {
                allDocs = walletRepository.findAllTransactionsPaginated(userId, page, size)
                        .get().getDocuments();
            }

            List<TransactionDTO> result = new java.util.ArrayList<>();
            for (com.google.cloud.firestore.QueryDocumentSnapshot doc : allDocs) {
                result.add(mapToTransactionDTO(doc.getId(), doc.getData()));
            }
            
            result.sort((t1, t2) -> {
                if (t1.getCreatedAt() == null || t2.getCreatedAt() == null) return 0;
                return t2.getCreatedAt().compareTo(t1.getCreatedAt());
            });
            
            int start = Math.min(page * size, result.size());
            int end = Math.min(start + size, result.size());
            List<TransactionDTO> paginated = result.subList(start, end);
            
            log.info("Tim thay {} giao dich (tong: {})", paginated.size(), result.size());
            return paginated;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi lay giao dich: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        } catch (java.util.concurrent.ExecutionException e) {
            log.error("Loi khi lay giao dich: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        }
    }

    public TransactionDTO requestMerchantWithdrawal(String userId, com.example.be_foodgo.dto.WithdrawRequest request) {
        double amount = request.getAmount();
        log.info("Bat dau yeu cau rut tien (merchant): userId={}, amount={}", userId, amount);
        try {
            List<com.google.cloud.firestore.QueryDocumentSnapshot> walletSnapshots = walletRepository
                    .findDriverWalletByUserIdAndRole(userId, 1);
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

            if (request.getBankName() != null && !request.getBankName().trim().isEmpty()) {
                Map<String, Object> updates = new HashMap<>();
                updates.put("bankName", request.getBankName());
                updates.put("bankAccountNumber", request.getBankAccountNumber());
                updates.put("bankAccountName", request.getBankAccountName());
                updates.put("updatedAt", com.google.cloud.Timestamp.now());
                walletDoc.getReference().update(updates).get();
            }

            String description = "Yêu cầu rút tiền tài khoản.";
            if (request.getBankName() != null && request.getBankAccountNumber() != null) {
                description = "Rút tiền về " + request.getBankName() + " - " + request.getBankAccountNumber();
            }
            String transId = walletRepository.withdrawInTransaction(walletId, userId, amount, description);

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
                    .findDriverWalletByUserIdAndRole(driverId, 2);
            if (!wallets.isEmpty()) {
                walletId = wallets.get(0).getId();
            }

            Map<String, Object> transData = new HashMap<>();
            transData.put("walletId", walletId);
            transData.put("userId", driverId);
            transData.put("type", 2);
            transData.put("amount", deliveryFee);
            transData.put("fee", 0.0);
            transData.put("netAmount", deliveryFee);
            transData.put("description", "Thu nhap giao hang cho don hang [" + orderId + "]");
            transData.put("orderId", orderId);
            transData.put("status", 1);
            transData.put("createdAt", com.google.cloud.firestore.FieldValue.serverTimestamp());

            walletRepository.createTransaction(transData);

            if (walletId != null) {
                Map<String, Object> walletUpdates = new HashMap<>();
                walletUpdates.put("balance",
                        com.google.cloud.firestore.FieldValue.increment(deliveryFee));
                walletUpdates.put("totalEarned",
                        com.google.cloud.firestore.FieldValue.increment(deliveryFee));
                walletUpdates.put("updatedAt", com.google.cloud.firestore.FieldValue.serverTimestamp());
                walletRepository.updateWalletFields(walletId, walletUpdates);
            }

            log.info("Da tao giao dich thu nhap: driverId={}, orderId={}, amount={}", driverId, orderId, deliveryFee);
        } catch (Exception e) {
            log.warn("Loi khi tao giao dich thu nhap: {}", e.getMessage());
        }
    }

    public void createMerchantIncomeTransaction(String storeId, String orderId, String orderCode, double amount) {
        try {
            com.google.cloud.firestore.Firestore firestore = walletRepository.getFirestore();
            List<com.google.cloud.firestore.QueryDocumentSnapshot> docs = firestore.collection("merchant_profiles")
                    .whereArrayContains("storeIds", storeId)
                    .limit(1)
                    .get().get().getDocuments();
            if (docs.isEmpty()) {
                log.warn("Không tìm thấy chủ gian hàng cho storeId: {}", storeId);
                return;
            }
            String merchantId = docs.get(0).getId();

            String walletId = null;
            List<com.google.cloud.firestore.QueryDocumentSnapshot> wallets = walletRepository
                    .findDriverWalletByUserIdAndRole(merchantId, 1);
            
            if (wallets.isEmpty()) {
                walletId = walletRepository.createMerchantWallet(merchantId);
            } else {
                walletId = wallets.get(0).getId();
            }

            double platformFeePercentage = 0.0;

            double fee = 0.0;
            double netAmount = amount;

            Map<String, Object> transData = new HashMap<>();
            transData.put("walletId", walletId);
            transData.put("userId", merchantId);
            transData.put("type", 1);
            transData.put("amount", amount);
            transData.put("fee", fee);
            transData.put("netAmount", netAmount);
            transData.put("description", "Doanh thu đơn hàng " + orderCode);
            transData.put("orderId", orderId);
            transData.put("status", 1);
            transData.put("createdAt", com.google.cloud.firestore.FieldValue.serverTimestamp());

            walletRepository.createTransaction(transData);

            Map<String, Object> walletUpdates = new HashMap<>();
            walletUpdates.put("balance", com.google.cloud.firestore.FieldValue.increment(netAmount));
            walletUpdates.put("totalEarned", com.google.cloud.firestore.FieldValue.increment(netAmount));
            walletUpdates.put("updatedAt", com.google.cloud.firestore.FieldValue.serverTimestamp());
            walletRepository.updateWalletFields(walletId, walletUpdates);

            log.info("Đã cộng doanh thu cho gian hàng: merchantId={}, orderId={}, amount={}", merchantId, orderId, amount);
        } catch (Exception e) {
            log.warn("Lỗi khi tạo giao dịch thu nhập cho gian hàng: {}", e.getMessage());
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
            List<com.google.cloud.firestore.QueryDocumentSnapshot> docs = firestore.collection("transactions")
                    .whereEqualTo("type", 3)
                    .whereEqualTo("status", 0)
                    .get()
                    .get()
                    .getDocuments();

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
            
            result.sort((t1, t2) -> {
                if (t1.getCreatedAt() == null || t2.getCreatedAt() == null) return 0;
                return t2.getCreatedAt().compareTo(t1.getCreatedAt());
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

            Long status = transDoc.getLong("status");
            if (status == null || status != 0) {
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
            batch.update(transRef, "status", 1);
            
            // Cap nhat vi
            Map<String, Object> walletUpdates = new HashMap<>();
            walletUpdates.put("pendingBalance", Math.max(0.0, pendingBalance - amount));
            walletUpdates.put("totalWithdrawn", totalWithdrawn + amount);
            walletUpdates.put("updatedAt", com.google.cloud.firestore.FieldValue.serverTimestamp());
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

            Long status = transDoc.getLong("status");
            if (status == null || status != 0) {
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
            batch.update(transRef, "status", 2);
            batch.update(transRef, "description", "Tu choi rut tien: " + reason);
            
            // Cap nhat vi: hoan tien
            Map<String, Object> walletUpdates = new HashMap<>();
            walletUpdates.put("balance", balance + amount);
            walletUpdates.put("pendingBalance", Math.max(0.0, pendingBalance - amount));
            walletUpdates.put("updatedAt", com.google.cloud.firestore.FieldValue.serverTimestamp());
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
                .role(data.get("role") != null ? String.valueOf(data.get("role")) : null)
                .balance(toDouble(data.get("balance")))
                .totalEarned(toDouble(data.get("totalEarned")))
                .totalWithdrawn(toDouble(data.get("totalWithdrawn")))
                .pendingBalance(toDouble(data.get("pendingBalance")))
                .bankName((String) data.get("bankName"))
                .bankAccountNumber((String) data.get("bankAccountNumber"))
                .bankAccountName((String) data.get("bankAccountName"))
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
                .type(data.get("type") != null ? Integer.parseInt(String.valueOf(data.get("type"))) : null)
                .amount(data.get("amount") != null ? Double.parseDouble(String.valueOf(data.get("amount"))) : null)
                .fee(data.get("fee") != null ? Double.parseDouble(String.valueOf(data.get("fee"))) : null)
                .netAmount(data.get("netAmount") != null ? Double.parseDouble(String.valueOf(data.get("netAmount"))) : null)
                .description(data.get("description") != null ? String.valueOf(data.get("description")) : null)
                .orderId(data.get("orderId") != null ? String.valueOf(data.get("orderId")) : null)
                .status(data.get("status") != null ? Integer.parseInt(String.valueOf(data.get("status"))) : null)
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
