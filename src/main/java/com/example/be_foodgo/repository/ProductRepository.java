package com.example.be_foodgo.repository;

import com.example.be_foodgo.model.Product;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class ProductRepository {

    private static final Logger log = LoggerFactory.getLogger(ProductRepository.class);

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

    public List<Product> layTatCaProducts() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<Product> products = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            Product product = doc.toObject(Product.class);
            if (product != null) {
                products.add(product);
            }
        }
        return products;
    }

    public List<Product> findFeatured(String categoryId) throws ExecutionException, InterruptedException {
        Query query = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("isFeatured", true)
                .whereEqualTo("isOutOfStock", false);
        if (categoryId != null && !categoryId.isEmpty()) {
            query = query.whereEqualTo("categoryId", categoryId);
        }
        ApiFuture<QuerySnapshot> future = query.get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<Product> products = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            Product product = doc.toObject(Product.class);
            if (product != null) {
                product.setId(doc.getId());
                products.add(product);
            }
        }
        products.sort((a, b) -> {
            com.google.cloud.Timestamp ta = a.getCreatedAt();
            com.google.cloud.Timestamp tb = b.getCreatedAt();
            if (ta == null && tb == null) return 0;
            if (ta == null) return 1;
            if (tb == null) return -1;
            return tb.compareTo(ta);
        });
        return products;
    }

    public void capNhatProductRating(String productId, double rating, int reviewCount) throws ExecutionException, InterruptedException {
        DocumentReference productRef = firestore.collection(COLLECTION_NAME).document(productId);
        ApiFuture<WriteResult> future = productRef.update(
                "rating", rating,
                "reviewCount", reviewCount,
                "updatedAt", com.google.cloud.Timestamp.now()
        );
        WriteResult result = future.get();
        log.info("Da cap nhat rating product [{}]: rating={}, reviewCount={}, luc [{}]",
                productId, rating, reviewCount, result.getUpdateTime());
    }
}
