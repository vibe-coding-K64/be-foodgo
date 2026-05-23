package com.example.be_foodgo.repository;

import com.example.be_foodgo.model.Category;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class CategoryRepository {

    private static final String COLLECTION_NAME = "system_categories";

    @Autowired
    private Firestore firestore;

    public List<Category> findAll(String storeId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("storeId", storeId)
                .get(); // Sắp xếp ở Java để tránh lỗi Index Firestore
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<Category> categories = new ArrayList<>();
        for (QueryDocumentSnapshot document : documents) {
            categories.add(document.toObject(Category.class));
        }
        
        categories.sort((c1, c2) -> {
            Integer o1 = c1.getOrder() != null ? c1.getOrder() : 0;
            Integer o2 = c2.getOrder() != null ? c2.getOrder() : 0;
            return o1.compareTo(o2);
        });
        
        return categories;
    }

    public Category findById(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            return document.toObject(Category.class);
        }
        return null;
    }

    public Category findByOrder(String storeId, int order) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("storeId", storeId)
                .whereEqualTo("order", order).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        if (!documents.isEmpty()) {
            return documents.get(0).toObject(Category.class);
        }
        return null;
    }

    public String save(Category category) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(category.getId());
        ApiFuture<WriteResult> result = docRef.set(category);
        result.get();
        return category.getId();
    }

    public List<String> getAllCategoryIds() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<String> ids = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            ids.add(doc.getId());
        }
        return ids;
    }

    public String delete(String id) throws ExecutionException, InterruptedException {
        ApiFuture<WriteResult> writeResult = firestore.collection(COLLECTION_NAME).document(id).delete();
        writeResult.get();
        return "Deleted successfully";
    }
}
