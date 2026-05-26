package com.example.be_foodgo.repository;

import com.example.be_foodgo.model.User;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import org.springframework.stereotype.Repository;

import com.google.cloud.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Repository
public class UserRepository {

    private static final String COLLECTION_NAME = "users";
    private final Firestore firestore;

    public UserRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    private CollectionReference getCollection() {
        return firestore.collection(COLLECTION_NAME);
    }

    public boolean tonTaiEmail(String email) throws ExecutionException, InterruptedException {
        Query query = getCollection().whereEqualTo("email", email).limit(1);
        QuerySnapshot snapshot = query.get().get();
        return !snapshot.isEmpty();
    }

    public boolean tonTaiPhoneNumber(String phoneNumber) throws ExecutionException, InterruptedException {
        Query query = getCollection().whereEqualTo("phoneNumber", phoneNumber).limit(1);
        QuerySnapshot snapshot = query.get().get();
        return !snapshot.isEmpty();
    }

    public User timTheoId(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = getCollection().document(id).get().get();
        if (!doc.exists()) {
            return null;
        }
        return documentToUser(doc);
    }

    public User timTheoEmail(String email) throws ExecutionException, InterruptedException {
        Query query = getCollection().whereEqualTo("email", email).limit(1);
        QuerySnapshot snapshot = query.get().get();
        if (snapshot.isEmpty()) {
            return null;
        }
        return documentToUser(snapshot.getDocuments().get(0));
    }

    public String taoUser(User user) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getCollection().document(user.getId());
        Map<String, Object> data = userToMap(user);
        docRef.set(data).get();
        return docRef.getId();
    }

    public void capNhatPassword(String userId, String hashedPassword) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getCollection().document(userId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("password", hashedPassword);
        updates.put("updatedAt", java.time.Instant.now().toString());
        docRef.update(updates).get();
    }

    public void capNhatThongTinHoSo(String userId, String fullName, String photoUrl) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getCollection().document(userId);
        Map<String, Object> updates = new HashMap<>();
        if (fullName != null) {
            updates.put("fullName", fullName);
        }
        if (photoUrl != null) {
            updates.put("photoUrl", photoUrl);
        }
        updates.put("updatedAt", java.time.Instant.now().toString());
        docRef.update(updates).get();
    }

    public String sinhNextUserId() throws ExecutionException, InterruptedException {
        Query query = getCollection().orderBy("id", Query.Direction.DESCENDING).limit(1);
        QuerySnapshot snapshot = query.get().get();
        if (snapshot.isEmpty()) {
            return "user_001";
        }
        String lastId = snapshot.getDocuments().get(0).getString("id");
        if (lastId != null && lastId.startsWith("user_")) {
            try {
                int number = Integer.parseInt(lastId.replace("user_", ""));
                return String.format("user_%03d", number + 1);
            } catch (NumberFormatException ignored) {
            }
        }
        return "user_001";
    }

    private User documentToUser(DocumentSnapshot doc) {
        Map<String, Object> data = doc.getData();
        if (data == null) {
            return null;
        }
        List<Integer> roles = null;
        Object rolesObj = data.get("roles");
        if (rolesObj instanceof List) {
            List<?> rawList = (List<?>) rolesObj;
            roles = new ArrayList<>();
            for (Object val : rawList) {
                if (val instanceof Long) {
                    roles.add(((Long) val).intValue());
                } else if (val instanceof Integer) {
                    roles.add((Integer) val);
                }
            }
        }
        return User.builder()
                .id((String) data.get("id"))
                .email((String) data.get("email"))
                .password((String) data.get("password"))
                .fullName((String) data.get("fullName"))
                .phoneNumber((String) data.get("phoneNumber"))
                .photoUrl((String) data.get("photoUrl"))
                .roles(roles)
                .createdAt(objectToString(data.get("createdAt")))
                .updatedAt(objectToString(data.get("updatedAt")))
                .build();
    }

    private Map<String, Object> userToMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("email", user.getEmail());
        map.put("password", user.getPassword());
        map.put("fullName", user.getFullName());
        map.put("phoneNumber", user.getPhoneNumber());
        map.put("photoUrl", user.getPhotoUrl());
        map.put("roles", user.getRoles());
        map.put("createdAt", user.getCreatedAt());
        map.put("updatedAt", user.getUpdatedAt());
        return map;
    }

    private String objectToString(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            return (String) obj;
        }
        if (obj instanceof Timestamp) {
            return ((Timestamp) obj).toString();
        }
        return obj.toString();
    }
}
