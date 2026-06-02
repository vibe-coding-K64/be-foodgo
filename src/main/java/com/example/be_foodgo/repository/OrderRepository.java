package com.example.be_foodgo.repository;

import com.example.be_foodgo.model.Order;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Repository
public class OrderRepository {
    @Autowired
    private Firestore firestore;

    private static final String COLLECTION_NAME = "orders";
    private static final String RDB_ORDERS = "orders";

    private FirebaseDatabase getRdb() {
        return FirebaseDatabase.getInstance();
    }

    public List<Order> findByStoreId(String storeId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("storeId", storeId)
                .get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<Order> orders = new ArrayList<>();
        for (DocumentSnapshot document : documents) {
            Order order = document.toObject(Order.class);
            if (order != null) {
                order.setId(document.getId());
                orders.add(order);
            }
        }
        return orders;
    }

    public Order findById(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            Order order = document.toObject(Order.class);
            if (order != null) {
                order.setId(document.getId());
            }
            return order;
        }
        return null;
    }

    public String save(Order order) throws ExecutionException, InterruptedException {
        ApiFuture<DocumentReference> collectionsApiFuture = firestore.collection(COLLECTION_NAME).add(order);
        return collectionsApiFuture.get().getId();
    }

    public String update(String id, Order order) throws ExecutionException, InterruptedException {
        ApiFuture<WriteResult> collectionsApiFuture = firestore.collection(COLLECTION_NAME).document(id).set(order);
        return collectionsApiFuture.get().getUpdateTime().toString();
    }

    public String updateFields(String id, Map<String, Object> fields) throws ExecutionException, InterruptedException {
        ApiFuture<WriteResult> collectionsApiFuture = firestore.collection(COLLECTION_NAME).document(id).update(fields);
        return collectionsApiFuture.get().getUpdateTime().toString();
    }

    public void updateRdbStatus(String orderId, int status) {
        DatabaseReference ref = getRdb().getReference(RDB_ORDERS).child(orderId);
        ref.child("status").setValueAsync(status);
        ref.child("updatedAt").setValueAsync(System.currentTimeMillis());
    }

    public void syncOrderToRdb(Order order) {
        DatabaseReference ref = getRdb().getReference(RDB_ORDERS).child(order.getId());
        ref.child("id").setValueAsync(order.getId());
        ref.child("storeId").setValueAsync(order.getStoreId());
        ref.child("storeName").setValueAsync(order.getStoreName());
        ref.child("userId").setValueAsync(order.getUserId());
        ref.child("receiverName").setValueAsync(order.getReceiverName());
        ref.child("receiverPhone").setValueAsync(order.getReceiverPhone());
        ref.child("deliveryAddress").setValueAsync(order.getDeliveryAddress());
        ref.child("deliveryLat").setValueAsync(order.getDeliveryLat());
        ref.child("deliveryLng").setValueAsync(order.getDeliveryLng());
        ref.child("totalAmount").setValueAsync(order.getTotalAmount());
        ref.child("finalAmount").setValueAsync(order.getFinalAmount());
        ref.child("deliveryFee").setValueAsync(order.getDeliveryFee());
        ref.child("status").setValueAsync(order.getStatusValue());
        ref.child("deliveryHeading").setValueAsync(order.getDeliveryHeading());
        ref.child("createdAt").setValueAsync(order.getCreatedAt() != null ? order.getCreatedAt().getTime() : System.currentTimeMillis());
        ref.child("updatedAt").setValueAsync(System.currentTimeMillis());
    }

    public String delete(String id) throws ExecutionException, InterruptedException {
        ApiFuture<WriteResult> writeResult = firestore.collection(COLLECTION_NAME).document(id).delete();
        return writeResult.get().getUpdateTime().toString();
    }

    public List<Order> findAllOrders() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).orderBy("createdAt", Query.Direction.DESCENDING).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<Order> orders = new ArrayList<>();
        for (DocumentSnapshot document : documents) {
            Order order = document.toObject(Order.class);
            if (order != null) {
                order.setId(document.getId());
                orders.add(order);
            }
        }
        return orders;
    }
}
