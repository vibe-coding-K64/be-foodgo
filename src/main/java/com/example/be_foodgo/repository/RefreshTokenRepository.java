package com.example.be_foodgo.repository;

import com.example.be_foodgo.model.RefreshToken;
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
public class RefreshTokenRepository {

    private static final String COLLECTION_NAME = "refresh_tokens";
    private final Firestore firestore;

    public RefreshTokenRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    private CollectionReference getCollection() {
        return firestore.collection(COLLECTION_NAME);
    }

    public RefreshToken timTheoToken(String token) throws ExecutionException, InterruptedException {
        Query query = getCollection()
                .whereEqualTo("token", token)
                .whereEqualTo("revoked", false)
                .limit(1);
        QuerySnapshot snapshot = query.get().get();
        if (snapshot.isEmpty()) {
            return null;
        }
        return documentToRefreshToken(snapshot.getDocuments().get(0));
    }

    public RefreshToken timTheoId(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = getCollection().document(id).get().get();
        if (!doc.exists()) {
            return null;
        }
        return documentToRefreshToken(doc);
    }

    public List<RefreshToken> timTheoUserId(String userId) throws ExecutionException, InterruptedException {
        Query query = getCollection().whereEqualTo("userId", userId);
        QuerySnapshot snapshot = query.get().get();
        List<RefreshToken> tokens = new ArrayList<>();
        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            tokens.add(documentToRefreshToken(doc));
        }
        return tokens;
    }

    public String taoRefreshToken(RefreshToken refreshToken) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getCollection().document();
        refreshToken.setId(docRef.getId());
        Map<String, Object> data = refreshTokenToMap(refreshToken);
        docRef.set(data).get();
        return docRef.getId();
    }

    public void thuHoiToken(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getCollection().document(id);
        Map<String, Object> updates = new HashMap<>();
        updates.put("revoked", true);
        docRef.update(updates).get();
    }

    public void thuHoiTatCaTokenCuaUser(String userId) throws ExecutionException, InterruptedException {
        Query query = getCollection().whereEqualTo("userId", userId);
        QuerySnapshot snapshot = query.get().get();
        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("revoked", true);
            doc.getReference().update(updates);
        }
    }

    public void xoaToken(String id) throws ExecutionException, InterruptedException {
        getCollection().document(id).delete().get();
    }

    public void xoaTatCaTokenDaThuHoi() throws ExecutionException, InterruptedException {
        Query query = getCollection().whereEqualTo("revoked", true);
        QuerySnapshot snapshot = query.get().get();
        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            doc.getReference().delete();
        }
    }

    private RefreshToken documentToRefreshToken(DocumentSnapshot doc) {
        Map<String, Object> data = doc.getData();
        if (data == null) {
            return null;
        }
        return RefreshToken.builder()
                .id(doc.getId())
                .token((String) data.get("token"))
                .userId((String) data.get("userId"))
                .deviceInfo((String) data.get("deviceInfo"))
                .createdAt(objectToString(data.get("createdAt")))
                .expiresAt(objectToString(data.get("expiresAt")))
                .revoked(data.get("revoked") != null && (Boolean) data.get("revoked"))
                .build();
    }

    private Map<String, Object> refreshTokenToMap(RefreshToken token) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", token.getId());
        map.put("token", token.getToken());
        map.put("userId", token.getUserId());
        map.put("deviceInfo", token.getDeviceInfo());
        map.put("createdAt", token.getCreatedAt());
        map.put("expiresAt", token.getExpiresAt());
        map.put("revoked", token.isRevoked());
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
