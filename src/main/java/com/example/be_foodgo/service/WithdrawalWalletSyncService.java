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
        log.info("[TransactionListener] Đã tắt Listener để tránh double deduction vì đã dùng REST API.");
    }
}
