package com.example.be_foodgo.repository;

import com.example.be_foodgo.model.Product;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class ProductRepository {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION_NAME = "products";

    public String save(Product product) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(product.getId());
        ApiFuture<WriteResult> collectionsApiFuture = docRef.set(product);
        return collectionsApiFuture.get().getUpdateTime().toString();
    }

    public List<String> getAllProductIds() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<String> ids = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            ids.add(doc.getId());
        }
        return ids;
    }

    public List<Product> findAll(String storeId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("storeId", storeId).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<Product> products = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            products.add(doc.toObject(Product.class));
        }
        return products;
    }

    public Product findById(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            return document.toObject(Product.class);
        }
        return null;
    }

    public String delete(String id) throws ExecutionException, InterruptedException {
        ApiFuture<WriteResult> writeResult = firestore.collection(COLLECTION_NAME).document(id).delete();
        return writeResult.get().getUpdateTime().toString();
    }
}
