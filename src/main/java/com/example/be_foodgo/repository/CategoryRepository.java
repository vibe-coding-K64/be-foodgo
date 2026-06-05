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

    public static final String COLLECTION_NAME = "categories";

    @Autowired
    private Firestore firestore;

    public List<Category> findAll(String storeId) throws ExecutionException, InterruptedException {
        List<Category> systemCategories = findAllSystemCategories();
        if (storeId == null || storeId.isEmpty() || "null".equalsIgnoreCase(storeId) || "system".equalsIgnoreCase(storeId)) {
            return systemCategories;
        }
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("storeId", storeId)
                .get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<Category> categories = new ArrayList<>(systemCategories);
        for (QueryDocumentSnapshot document : documents) {
            categories.add(document.toObject(Category.class));
        }

        sortByOrder(categories);
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
        if (storeId == null || storeId.isEmpty() || "null".equalsIgnoreCase(storeId) || "system".equalsIgnoreCase(storeId)) {
            return findSystemCategoryByOrder(order);
        }
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
        firestore.collection(COLLECTION_NAME).document(id).delete().get();
        return "Deleted successfully";
    }

    public List<Category> findAllSystemCategories() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<Category> categories = new ArrayList<>();
        for (QueryDocumentSnapshot document : documents) {
            Category cat = document.toObject(Category.class);
            boolean isSys = cat.getStoreId() == null || cat.getStoreId().isEmpty() || "null".equalsIgnoreCase(cat.getStoreId()) || "system".equalsIgnoreCase(cat.getStoreId());
            if (isSys) {
                categories.add(cat);
            }
        }
        sortByOrder(categories);
        return categories;
    }

    public List<Category> findAllStoreCategories(String storeId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<Category> categories = new ArrayList<>();
        for (QueryDocumentSnapshot document : documents) {
            Category cat = document.toObject(Category.class);
            if (storeId.equals(cat.getStoreId())) {
                categories.add(cat);
            }
        }
        sortByOrder(categories);
        return categories;
    }

    public List<String> getAllSystemCategoryIds() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<String> ids = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            Category cat = doc.toObject(Category.class);
            boolean isSys = cat.getStoreId() == null || cat.getStoreId().isEmpty() || "null".equalsIgnoreCase(cat.getStoreId()) || "system".equalsIgnoreCase(cat.getStoreId());
            if (isSys) {
                ids.add(doc.getId());
            }
        }
        return ids;
    }

    public List<String> getAllStoreCategoryIds() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<String> ids = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            String id = doc.getId();
            if (id != null && id.startsWith("stocate_")) {
                ids.add(id);
            }
        }
        return ids;
    }

    public Category findSystemCategoryByOrder(int order) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        for (QueryDocumentSnapshot doc : documents) {
            Category cat = doc.toObject(Category.class);
            boolean isSys = cat.getStoreId() == null || cat.getStoreId().isEmpty() || "null".equalsIgnoreCase(cat.getStoreId()) || "system".equalsIgnoreCase(cat.getStoreId());
            if (isSys && cat.getOrder() != null && order == cat.getOrder()) {
                return cat;
            }
        }
        return null;
    }

    public Category findStoreCategoryByOrder(String storeId, int order) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("storeId", storeId)
                .whereEqualTo("order", order).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        if (!documents.isEmpty()) {
            return documents.get(0).toObject(Category.class);
        }
        return null;
    }

    private void sortByOrder(List<Category> categories) {
        categories.sort((c1, c2) -> {
            Integer o1 = c1.getOrder() != null ? c1.getOrder() : 0;
            Integer o2 = c2.getOrder() != null ? c2.getOrder() : 0;
            return o1.compareTo(o2);
        });
    }
}
