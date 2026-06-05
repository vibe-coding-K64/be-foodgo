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
            Order order = mapDocumentToOrder(document);
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
            return mapDocumentToOrder(document);
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
            Order order = mapDocumentToOrder(document);
            if (order != null) {
                orders.add(order);
            }
        }
        return orders;
    }

    private Order mapDocumentToOrder(DocumentSnapshot document) {
        if (document == null || !document.exists()) {
            return null;
        }

        Order order = new Order();
        order.setId(document.getId());
        order.setUserId(document.getString("userId"));
        order.setStoreId(document.getString("storeId"));
        order.setStoreName(document.getString("storeName"));
        order.setCode(document.getString("code"));
        order.setDeliveryAddress(document.getString("deliveryAddress"));
        order.setAddressId(document.getString("addressId"));
        order.setReceiverName(document.getString("receiverName"));
        order.setReceiverPhone(document.getString("receiverPhone"));
        order.setDeliveryFee(toDouble(document.get("deliveryFee")));
        order.setDriverName(document.getString("driverName"));
        order.setDriverPhone(document.getString("driverPhone"));
        order.setItems(extractOrderItems(document.get("items")));
        order.setTotalAmount(toDouble(document.get("totalAmount")));
        order.setDiscountAmount(toDouble(document.get("discountAmount")));
        order.setShopDiscountAmount(toDouble(document.get("shopDiscountAmount")));
        order.setFreeshipDiscountAmount(toDouble(document.get("freeshipDiscountAmount")));
        order.setFinalAmount(toDouble(document.get("finalAmount")));
        order.setPaymentMethod(resolvePaymentMethod(document));
        order.setPaymentStatus(toInteger(document.get("paymentStatus")));
        order.setStatus(document.get("status"));
        order.setCreatedAt(toDate(document.get("createdAt")));
        order.setUpdatedAt(toDate(document.get("updatedAt")));
        order.setDeletedAt(toDate(document.get("deletedAt")));
        order.setDeliveryHeading(toNullableDouble(document.get("deliveryHeading")));
        order.setDeliveryLat(toNullableDouble(document.get("deliveryLat")));
        order.setDeliveryLng(toNullableDouble(document.get("deliveryLng")));
        order.setNote(document.getString("note"));
        return order;
    }

    private Object resolvePaymentMethod(DocumentSnapshot document) {
        Object paymentMethod = document.get("paymentMethod");
        return paymentMethod != null ? paymentMethod : document.get("paymentMethodString");
    }

    private List<OrderItem> extractOrderItems(Object value) {
        if (!(value instanceof List<?> rawItems)) {
            return null;
        }

        List<OrderItem> items = new ArrayList<>();
        for (Object rawItem : rawItems) {
            if (rawItem instanceof OrderItem orderItem) {
                items.add(orderItem);
                continue;
            }
            if (!(rawItem instanceof Map<?, ?> map)) {
                continue;
            }

            OrderItem item = new OrderItem();
            item.setFoodId(asString(firstNonNull(map.get("foodId"), map.get("productId"))));
            item.setImageUrl(asString(firstNonNull(map.get("imageUrl"), map.get("image"))));
            item.setName(asString(firstNonNull(map.get("name"), map.get("productName"))));
            item.setSize(asString(map.get("size")));
            item.setOptions(map.get("options"));
            Integer quantity = toInteger(map.get("quantity"));
            item.setQuantity(quantity != null ? quantity : 0);
            item.setPrice(toDouble(map.get("price")));
            items.add(item);
        }
        return items;
    }

    private Object firstNonNull(Object... values) {
        for (Object value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String asString(Object value) {
        return value != null ? String.valueOf(value) : null;
    }

    private double toDouble(Object value) {
        Double number = toNullableDouble(value);
        return number != null ? number : 0.0;
    }

    private Double toNullableDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String str) {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String str) {
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private Date toDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Date date) {
            return date;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toDate();
        }
        if (value instanceof Number number) {
            return new Date(number.longValue());
        }
        if (value instanceof Map<?, ?> map) {
            Object seconds = map.get("_seconds");
            Object nanoseconds = map.get("_nanoseconds");
            if (seconds instanceof Number secondsNumber) {
                long millis = secondsNumber.longValue() * 1000L;
                if (nanoseconds instanceof Number nanosNumber) {
                    millis += nanosNumber.longValue() / 1_000_000L;
                }
                return new Date(millis);
            }
            Object timestamp = map.get("timestamp");
            if (timestamp != null) {
                return toDate(timestamp);
            }
        }
        if (value instanceof String str) {
            try {
                return Date.from(java.time.Instant.parse(str));
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }
}
