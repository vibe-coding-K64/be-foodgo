package com.example.be_foodgo.repository;

import com.example.be_foodgo.model.AdminProfile;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@Repository
public class AdminProfileRepository {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION_NAME = "admin_profiles";

    public AdminProfile getProfileById(String userId) throws ExecutionException, InterruptedException {
        DocumentReference documentReference = firestore.collection(COLLECTION_NAME).document(userId);
        ApiFuture<DocumentSnapshot> future = documentReference.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            return documentToProfile(document);
        }
        return null;
    }

    private AdminProfile documentToProfile(DocumentSnapshot doc) {
        String id = doc.getId();
        Long adminLevelLong = doc.getLong("adminLevel");
        Integer adminLevel = adminLevelLong != null ? adminLevelLong.intValue() : 1;
        String department = doc.getString("department");
        
        java.util.List<String> permissions = new java.util.ArrayList<>();
        Object permObj = doc.get("permissions");
        if (permObj instanceof java.util.List) {
            java.util.List<?> rawList = (java.util.List<?>) permObj;
            for (Object val : rawList) {
                if (val != null) {
                    permissions.add(val.toString());
                }
            }
        }

        return AdminProfile.builder()
                .id(id)
                .adminLevel(adminLevel)
                .department(department)
                .permissions(permissions)
                .createdAt(objectToString(doc.get("createdAt")))
                .updatedAt(objectToString(doc.get("updatedAt")))
                .build();
    }

    private String objectToString(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            return (String) obj;
        }
        if (obj instanceof com.google.cloud.Timestamp) {
            return ((com.google.cloud.Timestamp) obj).toString();
        }
        return obj.toString();
    }

    public String saveProfile(AdminProfile profile) throws ExecutionException, InterruptedException {
        ApiFuture<WriteResult> collectionsApiFuture = firestore.collection(COLLECTION_NAME).document(profile.getId()).set(profile);
        return collectionsApiFuture.get().getUpdateTime().toString();
    }

    public String updateFields(String userId, Map<String, Object> fields) throws ExecutionException, InterruptedException {
        ApiFuture<WriteResult> collectionsApiFuture = firestore.collection(COLLECTION_NAME).document(userId).update(fields);
        return collectionsApiFuture.get().getUpdateTime().toString();
    }
}
