package com.example.be_foodgo.repository;

import com.example.be_foodgo.model.Review;
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
public class ReviewRepository {

    private static final Logger log = LoggerFactory.getLogger(ReviewRepository.class);

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION_NAME = "reviews";

    public String save(Review review) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
        review.setId(docRef.getId());
        ApiFuture<WriteResult> collectionsApiFuture = docRef.set(review);
        String updateTime = collectionsApiFuture.get().getUpdateTime().toString();
        log.info("Da luu review voi ID [{}] vao collection [{}] luc [{}]", review.getId(), COLLECTION_NAME, updateTime);
        return review.getId();
    }

    public Review findById(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            Review review = document.toObject(Review.class);
            if (review != null) {
                review.setId(document.getId());
            }
            return review;
        }
        return null;
    }

    public List<Review> findByOrderId(String orderId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("orderId", orderId)
                .get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<Review> reviews = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            Review review = doc.toObject(Review.class);
            if (review != null) {
                review.setId(doc.getId());
                reviews.add(review);
            }
        }
        return reviews;
    }

    public List<Review> findByStoreId(String storeId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("storeId", storeId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<Review> reviews = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            Review review = doc.toObject(Review.class);
            if (review != null) {
                review.setId(doc.getId());
                reviews.add(review);
            }
        }
        return reviews;
    }

    public List<Review> findByFoodId(String foodId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("foodId", foodId)
                .get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<Review> reviews = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            Review review = doc.toObject(Review.class);
            if (review != null) {
                review.setId(doc.getId());
                reviews.add(review);
            }
        }
        return reviews;
    }

    public List<Review> findByOrderIdAndItemId(String orderId, String itemId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("orderId", orderId)
                .whereEqualTo("itemId", itemId)
                .get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<Review> reviews = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            Review review = doc.toObject(Review.class);
            if (review != null) {
                review.setId(doc.getId());
                reviews.add(review);
            }
        }
        return reviews;
    }

    public void capNhatStoreRating(String storeId, double rating, int reviewCount) throws ExecutionException, InterruptedException {
        DocumentReference storeRef = firestore.collection("stores").document(storeId);
        ApiFuture<WriteResult> future = storeRef.update(
                "rating", rating,
                "reviewCount", reviewCount,
                "updatedAt", com.google.cloud.Timestamp.now()
        );
        WriteResult result = future.get();
        log.info("Da cap nhat rating cua store [{}]: rating={}, reviewCount={}, luc [{}]",
                storeId, rating, reviewCount, result.getUpdateTime());
    }

    public double tinhRatingTrungBinh(String fieldName, String fieldValue) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                .whereEqualTo(fieldName, fieldValue)
                .get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        if (documents.isEmpty()) {
            return 0.0;
        }
        long tongSao = 0;
        for (QueryDocumentSnapshot doc : documents) {
            Long star = doc.getLong("starRating");
            if (star != null) {
                tongSao += star;
            }
        }
        return Math.round((double) tongSao / documents.size() * 10.0) / 10.0;
    }

    public int demSoReview(String fieldName, String fieldValue) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                .whereEqualTo(fieldName, fieldValue)
                .get();
        return future.get().getDocuments().size();
    }

    public void deleteById(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
        docRef.delete();
        log.info("Da xoa review [{}]", id);
    }

    public void updateReview(Review review) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(review.getId());
        ApiFuture<WriteResult> future = docRef.set(review);
        WriteResult result = future.get();
        log.info("Da cap nhat review [{}] luc [{}]", review.getId(), result.getUpdateTime());
    }
}
