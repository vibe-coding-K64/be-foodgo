package com.example.be_foodgo.service;

import com.google.cloud.firestore.DocumentChange;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.WriteBatch;
import com.google.cloud.firestore.FieldValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * Lắng nghe thay đổi trạng thái giao dịch RÚT TIỀN (type=3) từ Firestore.
 * Khi Admin (bên máy khác) duyệt hoặc từ chối, tự động cập nhật ví.
 *
 * Logic ví:
 *  - Khi tạo lệnh rút: balance giữ nguyên, pendingBalance += amount
 *  - Khi DUYỆT (status 0 -> 1): balance -= amount, pendingBalance -= amount, totalWithdrawn += amount
 *  - Khi TỪ CHỐI (status 0 -> 2): pendingBalance -= amount (hoàn lại, balance không đổi)
 */
@Service
public class WithdrawalWalletSyncService {

    private static final Logger log = LoggerFactory.getLogger(WithdrawalWalletSyncService.class);

    @Autowired
    private Firestore firestore;

    @PostConstruct
    public void startListening() {
        log.info("[TransactionListener] Bat dau lang nghe thay doi giao dich rut tien tren Firestore...");

        firestore.collection("transactions")
                .whereEqualTo("type", 3) // type=3 là rút tiền
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        log.error("[TransactionListener] Loi lang nghe: {}", error.getMessage());
                        return;
                    }
                    if (snapshots == null) return;

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        if (dc.getType() != DocumentChange.Type.MODIFIED) continue;

                        QueryDocumentSnapshot doc = dc.getDocument();
                        String transId = doc.getId();
                        Long newStatus = doc.getLong("status");
                        Boolean walletUpdated = doc.getBoolean("walletUpdated");

                        // Chỉ xử lý khi status chuyển sang 1 (Duyệt) hoặc 2 (Từ chối)
                        // và chưa được xử lý trước đó
                        if (newStatus == null || (newStatus != 1 && newStatus != 2)) continue;
                        if (Boolean.TRUE.equals(walletUpdated)) {
                            log.info("[TransactionListener] Giao dich {} da duoc xu ly truoc do, bo qua.", transId);
                            continue;
                        }

                        String walletId = doc.getString("walletId");
                        Double amount = doc.getDouble("amount");

                        if (walletId == null || amount == null || amount <= 0) {
                            log.warn("[TransactionListener] Giao dich {} thieu walletId hoac amount.", transId);
                            continue;
                        }

                        log.info("[TransactionListener] Phat hien giao dich {} chuyen sang status={}. Bat dau cap nhat vi...", transId, newStatus);

                        try {
                            DocumentReference walletRef = firestore.collection("wallets").document(walletId);
                            DocumentSnapshot walletDoc = walletRef.get().get();

                            if (!walletDoc.exists()) {
                                log.error("[TransactionListener] Khong tim thay vi {} cho giao dich {}.", walletId, transId);
                                continue;
                            }

                            double balance = walletDoc.getDouble("balance") != null ? walletDoc.getDouble("balance") : 0.0;
                            double pendingBalance = walletDoc.getDouble("pendingBalance") != null ? walletDoc.getDouble("pendingBalance") : 0.0;
                            double totalWithdrawn = walletDoc.getDouble("totalWithdrawn") != null ? walletDoc.getDouble("totalWithdrawn") : 0.0;

                            Map<String, Object> walletUpdates = new HashMap<>();
                            if (newStatus == 1) {
                                // DUYỆT: trừ balance và pendingBalance, cộng totalWithdrawn
                                walletUpdates.put("balance", Math.max(0.0, balance - amount));
                                walletUpdates.put("pendingBalance", Math.max(0.0, pendingBalance - amount));
                                walletUpdates.put("totalWithdrawn", totalWithdrawn + amount);
                                log.info("[TransactionListener] DUYET: balance {} -> {}, pendingBalance {} -> {}",
                                        balance, Math.max(0.0, balance - amount),
                                        pendingBalance, Math.max(0.0, pendingBalance - amount));
                            } else {
                                // TỪ CHỐI: chỉ trừ pendingBalance (hoàn lại cho balance ngầm)
                                walletUpdates.put("pendingBalance", Math.max(0.0, pendingBalance - amount));
                                log.info("[TransactionListener] TU CHOI: pendingBalance {} -> {}, balance giu nguyen {}",
                                        pendingBalance, Math.max(0.0, pendingBalance - amount), balance);
                            }
                            walletUpdates.put("updatedAt", FieldValue.serverTimestamp());

                            // Cập nhật ví và đánh dấu giao dịch đã xử lý (tránh xử lý 2 lần)
                            WriteBatch batch = firestore.batch();
                            batch.update(walletRef, walletUpdates);
                            batch.update(firestore.collection("transactions").document(transId), "walletUpdated", true);
                            batch.commit().get();

                            log.info("[TransactionListener] Da cap nhat vi thanh cong cho giao dich {}!", transId);

                        } catch (Exception ex) {
                            log.error("[TransactionListener] Loi khi cap nhat vi cho giao dich {}: {}", transId, ex.getMessage());
                        }
                    }
                });
    }
}
