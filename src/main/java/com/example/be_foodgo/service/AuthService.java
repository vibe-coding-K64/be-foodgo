package com.example.be_foodgo.service;

import com.example.be_foodgo.dto.AuthRequestDTO;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuthService {

    private final Firestore firestore;
    private final FirebaseAuth firebaseAuth;

    public AuthService(Firestore firestore) {
        this.firestore = firestore;
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    public Map<String, Object> registerMerchant(AuthRequestDTO request) throws Exception {
        // 1. Sinh mã ID user mới (user_00X)
        String newId = generateNextUserId();

        // 2. Tạo User trên Firebase Auth với ID vừa sinh
        UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                .setUid(newId)
                .setEmail(request.getEmail())
                .setPassword(request.getPassword())
                .setDisplayName(request.getFullName());

        UserRecord userRecord = firebaseAuth.createUser(createRequest);

        // 3. Lưu thông tin vào Firestore collection "users"
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", newId);
        userData.put("email", request.getEmail());
        userData.put("fullName", request.getFullName());
        userData.put("phoneNumber", request.getPhoneNumber());
        userData.put("photoUrl", null);
        userData.put("roles", List.of(3)); // Role 3 = Người bán
        userData.put("createdAt", Instant.now().toString());

        firestore.collection("users").document(newId).set(userData).get();

        Map<String, Object> result = new HashMap<>();
        result.put("message", "Đăng ký tài khoản người bán thành công");
        result.put("uid", userRecord.getUid());
        return result;
    }

    private String generateNextUserId() throws Exception {
        // Tìm user có id cao nhất
        List<QueryDocumentSnapshot> docs = firestore.collection("users")
                .orderBy("id", com.google.cloud.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .get()
                .getDocuments();

        if (docs.isEmpty()) {
            return "user_001";
        }

        String lastId = docs.get(0).getString("id");
        if (lastId != null && lastId.startsWith("user_")) {
            try {
                int number = Integer.parseInt(lastId.replace("user_", ""));
                return String.format("user_%03d", number + 1);
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
        return "user_001"; // Default fallback
    }

    public java.util.Map<String, Object> checkMerchantProfile(String uid) throws Exception {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("isMerchant", false);
        result.put("storeId", null);

        com.google.cloud.firestore.DocumentSnapshot doc = firestore.collection("users").document(uid).get().get();
        if (doc.exists()) {
            List<Long> roles = (List<Long>) doc.get("roles");
            if (roles != null && roles.contains(3L)) {
                result.put("isMerchant", true);
                
                // Đọc merchant_profiles để lấy storeId
                com.google.cloud.firestore.DocumentSnapshot merchantDoc = firestore.collection("merchant_profiles").document(uid).get().get();
                if (merchantDoc.exists()) {
                    List<String> storeIds = (List<String>) merchantDoc.get("storeIds");
                    if (storeIds != null && !storeIds.isEmpty()) {
                        result.put("storeId", storeIds.get(0));
                    }
                }
            }
        }
        return result;
    }
}
