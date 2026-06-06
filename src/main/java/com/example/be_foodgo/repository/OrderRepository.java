package com.example.be_foodgo.repository;

import com.example.be_foodgo.model.Order;
import com.example.be_foodgo.model.OrderItem;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
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
            Order order = documentToOrder(document);
            if (order != null) {
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
            return documentToOrder(document);
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
        List<QueryDocumentSnapshot> documents;
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .orderBy("createdAt", Query.Direction.DESCENDING).get();
            documents = future.get().getDocuments();
        } catch (Exception e) {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
            documents = future.get().getDocuments();
        }
        List<Order> orders = new ArrayList<>();
        for (DocumentSnapshot document : documents) {
            Order order = documentToOrder(document);
            if (order != null) {
                orders.add(order);
            }
        }
        return orders;
    }

    @SuppressWarnings("unchecked")
    private Instant toInstant(Object value) {
        if (value == null) return null;
        if (value instanceof com.google.protobuf.Timestamp) {
            com.google.protobuf.Timestamp ts = (com.google.protobuf.Timestamp) value;
            return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
        }
        if (value instanceof com.google.cloud.Timestamp) {
            com.google.cloud.Timestamp ts = (com.google.cloud.Timestamp) value;
            return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
        }
        if (value instanceof java.util.Date) {
            return ((java.util.Date) value).toInstant();
        }
        if (value instanceof String) {
            try {
                return Instant.parse((String) value);
            } catch (Exception e) {
                return null;
            }
        }
        if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            Object seconds = map.get("epochSecond");
            Object nanos = map.get("nano");
            if (seconds != null) {
                long sec = seconds instanceof Long ? (Long) seconds : ((Integer) seconds).longValue();
                int nano = nanos != null ? (nanos instanceof Long ? ((Long) nanos).intValue() : (Integer) nanos) : 0;
                return Instant.ofEpochSecond(sec, nano);
            }
            return null;
        }
        return null;
    }

    private Date toDate(Object value) {
        if (value == null) return null;
        Instant instant = toInstant(value);
        return instant != null ? Date.from(instant) : null;
    }

    @SuppressWarnings("unchecked")
    private Order documentToOrder(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) {
            return null;
        }
        try {
            Order order = doc.toObject(Order.class);
            if (order != null) {
                order.setId(doc.getId());
                return order;
            }
        } catch (Exception e) {
            // Fallback manual parsing when toObject fails due to format mismatch (e.g., String dates in old DB)
            Order order = new Order();
            order.setId(doc.getId());
            order.setUserId(doc.getString("userId"));
            order.setStoreId(doc.getString("storeId"));
            order.setStoreName(doc.getString("storeName"));
            order.setCode(doc.getString("code"));
            order.setDeliveryAddress(doc.getString("deliveryAddress"));
            order.setAddressId(doc.getString("addressId"));
            order.setReceiverName(doc.getString("receiverName"));
            order.setReceiverPhone(doc.getString("receiverPhone"));
            
            Double deliveryFee = doc.getDouble("deliveryFee");
            order.setDeliveryFee(deliveryFee != null ? deliveryFee : 0.0);
            
            order.setDriverName(doc.getString("driverName"));
            order.setDriverPhone(doc.getString("driverPhone"));
            
            Double totalAmount = doc.getDouble("totalAmount");
            order.setTotalAmount(totalAmount != null ? totalAmount : 0.0);
            
            Double discountAmount = doc.getDouble("discountAmount");
            order.setDiscountAmount(discountAmount != null ? discountAmount : 0.0);
            
            Double shopDiscountAmount = doc.getDouble("shopDiscountAmount");
            order.setShopDiscountAmount(shopDiscountAmount != null ? shopDiscountAmount : 0.0);
            
            Double freeshipDiscountAmount = doc.getDouble("freeshipDiscountAmount");
            order.setFreeshipDiscountAmount(freeshipDiscountAmount != null ? freeshipDiscountAmount : 0.0);
            
            Double finalAmount = doc.getDouble("finalAmount");
            order.setFinalAmount(finalAmount != null ? finalAmount : 0.0);
            
            order.setPaymentMethod(doc.get("paymentMethod"));
            order.setStatus(doc.get("status"));
            
            order.setCreatedAt(toDate(doc.get("createdAt")));
            order.setUpdatedAt(toDate(doc.get("updatedAt")));
            order.setDeletedAt(toDate(doc.get("deletedAt")));
            
            order.setDeliveryHeading(doc.getDouble("deliveryHeading"));
            order.setDeliveryLat(doc.getDouble("deliveryLat"));
            order.setDeliveryLng(doc.getDouble("deliveryLng"));
            order.setNote(doc.getString("note"));
            
            // Parse items list
            Object itemsObj = doc.get("items");
            if (itemsObj instanceof List) {
                List<OrderItem> items = new ArrayList<>();
                for (Object itemObj : (List<?>) itemsObj) {
                    if (itemObj instanceof Map) {
                        Map<?, ?> itemMap = (Map<?, ?>) itemObj;
                        OrderItem item = new OrderItem();
                        item.setFoodId((String) itemMap.get("foodId"));
                        item.setName((String) itemMap.get("name"));
                        item.setImageUrl((String) itemMap.get("imageUrl"));
                        
                        Object qtyObj = itemMap.get("quantity");
                        if (qtyObj instanceof Number) {
                            item.setQuantity(((Number) qtyObj).intValue());
                        }
                        
                        Object priceObj = itemMap.get("price");
                        if (priceObj instanceof Number) {
                            item.setPrice(((Number) priceObj).doubleValue());
                        }
                        
                        Object optsObj = itemMap.get("options");
                        if (optsObj instanceof List) {
                            item.setOptions((List<Map<String, Object>>) optsObj);
                        }
                        items.add(item);
                    }
                }
                order.setItems(items);
            }
            return order;
        }
        return null;
    }
}
