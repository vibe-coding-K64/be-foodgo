package com.example.be_foodgo.repository;

import com.example.be_foodgo.dto.CartRequest.SelectedOption;
import com.example.be_foodgo.dto.CartRequest.SelectedOptionGroup;
import com.example.be_foodgo.model.CartItem;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Repository
public class CartRepository {

    private static final Logger log = LoggerFactory.getLogger(CartRepository.class);

    private static final String CART_COLLECTION = "customer_profiles";

    private final Firestore firestore;

    public CartRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public List<CartItem> layTatCaMonTrongGio(String userId) throws ExecutionException, InterruptedException {
        log.info("Truy vấn giỏ hàng của người dùng: {}", userId);
        CollectionReference cartRef = firestore
                .collection(CART_COLLECTION)
                .document(userId)
                .collection("cart");

        ApiFuture<QuerySnapshot> query = cartRef.get();
        QuerySnapshot snapshot = query.get();

        List<CartItem> items = new ArrayList<>();
        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            CartItem item = CartItem.builder()
                    .id(doc.getId())
                    .userId(userId)
                    .storeId(doc.getString("storeId"))
                    .foodId(doc.getString("foodId"))
                    .name(doc.getString("name"))
                    .price(doc.getDouble("price"))
                    .quantity(doc.getLong("quantity") != null ? doc.getLong("quantity").intValue() : 1)
                    .note(doc.getString("note"))
                    .imageUrl(doc.getString("imageUrl"))
                    .selectedOptions(toSelectedOptionGroups(doc.get("selectedOptions")))
                    .createdAt(toInstant(doc.get("createdAt")))
                    .updatedAt(toInstant(doc.get("updatedAt")))
                    .build();
            items.add(item);
        }

        log.info("Tìm thấy {} món trong giỏ hàng của người dùng {}", items.size(), userId);
        return items;
    }

    @SuppressWarnings("unchecked")
    private List<SelectedOptionGroup> toSelectedOptionGroups(Object selectedOptionsObj) {
        if (selectedOptionsObj == null) {
            return null;
        }
        List<?> rawList = (List<?>) selectedOptionsObj;
        List<SelectedOptionGroup> groups = new ArrayList<>();
        for (Object item : rawList) {
            if (!(item instanceof Map)) continue;
            Map<String, Object> groupMap = (Map<String, Object>) item;
            String groupName = (String) groupMap.get("name");
            if (groupName == null) continue;

            List<SelectedOption> options = new ArrayList<>();
            Object optionsObj = groupMap.get("options");
            if (optionsObj instanceof List) {
                for (Object opt : (List<?>) optionsObj) {
                    if (!(opt instanceof Map)) continue;
                    Map<String, Object> optMap = (Map<String, Object>) opt;
                    String optName = (String) optMap.get("name");
                    if (optName == null) continue;
                    options.add(SelectedOption.builder().name(optName).build());
                }
            }
            groups.add(SelectedOptionGroup.builder()
                    .name(groupName)
                    .options(options.isEmpty() ? null : options)
                    .build());
        }
        return groups;
    }

    private Double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return 0.0;
    }

    private java.time.Instant toInstant(Object value) {
        if (value == null) return null;
        if (value instanceof com.google.protobuf.Timestamp) {
            com.google.protobuf.Timestamp ts = (com.google.protobuf.Timestamp) value;
            return java.time.Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
        }
        if (value instanceof java.util.Date) {
            return ((java.util.Date) value).toInstant();
        }
        return null;
    }

    public String themMonVaoGio(String userId, CartItem item) {
        log.info("Thêm món [{}] vào giỏ hàng người dùng {}", item.getFoodId(), userId);
        CollectionReference cartRef = firestore
                .collection(CART_COLLECTION)
                .document(userId)
                .collection("cart");

        String cartItemId = item.getId() != null ? item.getId() : cartRef.document().getId();
        DocumentReference newDoc = cartRef.document(cartItemId);

        Map<String, Object> data = Map.ofEntries(
                Map.entry("storeId", item.getStoreId()),
                Map.entry("foodId", item.getFoodId()),
                Map.entry("name", item.getName()),
                Map.entry("price", item.getPrice()),
                Map.entry("quantity", item.getQuantity()),
                Map.entry("note", item.getNote() != null ? item.getNote() : ""),
                Map.entry("imageUrl", item.getImageUrl() != null ? item.getImageUrl() : ""),
                Map.entry("createdAt", item.getCreatedAt() != null ? item.getCreatedAt() : FieldValue.serverTimestamp()),
                Map.entry("updatedAt", FieldValue.serverTimestamp())
        );

        WriteBatch batch = firestore.batch();
        batch.set(newDoc, data);

        if (item.getSelectedOptions() != null && !item.getSelectedOptions().isEmpty()) {
            List<Map<String, Object>> selectedOptionsMaps = item.getSelectedOptions().stream()
                    .map(g -> {
                        List<Map<String, String>> options = g.getOptions() == null ? List.of()
                                : g.getOptions().stream()
                                        .map(o -> Map.<String, String>of("name", o.getName()))
                                        .toList();
                        return (Map<String, Object>) Map.of(
                                "name", g.getName(),
                                "options", options
                        );
                    })
                    .toList();
            batch.update(newDoc, "selectedOptions", selectedOptionsMaps);
        }

        try {
            batch.commit().get();
            log.info("Đã lưu món [{}] vào giỏ hàng với ID: {}", item.getFoodId(), cartItemId);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Lỗi khi commit giỏ hàng vào Firestore: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
        return cartItemId;
    }

    public void xoaTatCaMonTrongGio(String userId) {
        log.info("Xóa toàn bộ món trong giỏ hàng của người dùng {}", userId);
        CollectionReference cartRef = firestore
                .collection(CART_COLLECTION)
                .document(userId)
                .collection("cart");

        WriteBatch batch = firestore.batch();
        ApiFuture<QuerySnapshot> query = cartRef.get();
        QuerySnapshot snapshot;
        try {
            snapshot = query.get();
        } catch (Exception e) {
            log.error("Lỗi khi truy vấn giỏ hàng để xóa: {}", e.getMessage());
            return;
        }

        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            batch.delete(doc.getReference());
        }

        batch.commit();
        log.info("Đã xóa {} món khỏi giỏ hàng của người dùng {}", snapshot.size(), userId);
    }

    public CartItem layMotMonTrongGio(String userId, String itemId)
            throws ExecutionException, InterruptedException {
        log.info("Truy vấn một món trong giỏ hàng - userId: {}, itemId: {}", userId, itemId);
        DocumentReference docRef = firestore
                .collection(CART_COLLECTION)
                .document(userId)
                .collection("cart")
                .document(itemId);

        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot doc = future.get();

        if (!doc.exists()) {
            log.warn("Không tìm thấy món với itemId [{}] trong giỏ hàng của người dùng {}", itemId, userId);
            return null;
        }

        CartItem item = CartItem.builder()
                .id(doc.getId())
                .storeId(doc.getString("storeId"))
                .foodId(doc.getString("foodId"))
                .name(doc.getString("name"))
                .price(doc.getDouble("price"))
                .quantity(doc.getLong("quantity") != null ? doc.getLong("quantity").intValue() : 1)
                .note(doc.getString("note"))
                .imageUrl(doc.getString("imageUrl"))
                .selectedOptions(toSelectedOptionGroups(doc.get("selectedOptions")))
                .createdAt(toInstant(doc.get("createdAt")))
                .updatedAt(toInstant(doc.get("updatedAt")))
                .build();

        log.info("Tim thay mon [{}] trong gio hang cua nguoi dung {}", itemId, userId);
        return item;
    }

    public void capNhatSoLuongMon(String userId, String itemId, Integer quantity) {
        log.info("Cập nhật số lượng món {} trong giỏ hàng người dùng {} - số lượng mới: {}",
                itemId, userId, quantity);
        DocumentReference docRef = firestore
                .collection(CART_COLLECTION)
                .document(userId)
                .collection("cart")
                .document(itemId);

        try {
            docRef.update(
                    "quantity", quantity,
                    "updatedAt", FieldValue.serverTimestamp()
            ).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Lỗi khi cập nhật số lượng món [{}]: {}", itemId, e.getMessage());
            Thread.currentThread().interrupt();
        }

        log.info("Đã cập nhật số lượng món [{}] thành {} trong giỏ hàng người dùng {}", itemId, quantity, userId);
    }

    public void capNhatCartItem(String userId, CartItem item) {
        log.info("Cap nhat cart item {} trong gio hang nguoi dung {} - so luong: {}, gia: {}",
                item.getId(), userId, item.getQuantity(), item.getPrice());
        DocumentReference docRef = firestore
                .collection(CART_COLLECTION)
                .document(userId)
                .collection("cart")
                .document(item.getId());

        try {
            FirestoreExecutor executor = new FirestoreExecutor(docRef);
            executor.add("quantity", item.getQuantity());
            executor.add("price", item.getPrice());
            executor.add("note", item.getNote() != null ? item.getNote() : "");
            executor.add("updatedAt", FieldValue.serverTimestamp());

            if (item.getSelectedOptions() != null && !item.getSelectedOptions().isEmpty()) {
                List<Map<String, Object>> selectedOptionsMaps = item.getSelectedOptions().stream()
                        .map(g -> {
                            List<Map<String, String>> options = g.getOptions() == null ? List.of()
                                    : g.getOptions().stream()
                                            .map(o -> Map.<String, String>of("name", o.getName()))
                                            .toList();
                            return (Map<String, Object>) Map.of(
                                    "name", g.getName(),
                                    "options", options
                            );
                        })
                        .toList();
                executor.add("selectedOptions", selectedOptionsMaps);
            } else {
                executor.add("selectedOptions", null);
            }

            executor.commit().get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Loi khi cap nhat cart item [{}]: {}", item.getId(), e.getMessage());
            Thread.currentThread().interrupt();
        }

        log.info("Da cap nhat cart item [{}] trong gio hang nguoi dung {}", item.getId(), userId);
    }

    private static class FirestoreExecutor {
        private final DocumentReference docRef;
        private final Map<String, Object> updates = new java.util.HashMap<>();

        FirestoreExecutor(DocumentReference docRef) {
            this.docRef = docRef;
        }

        void add(String field, Object value) {
            updates.put(field, value);
        }

        ApiFuture<WriteResult> commit() {
            return docRef.update(updates);
        }
    }

    public void capNhatSoLuongVaGia(String userId, String itemId, Integer quantity, Double price) {
        log.info("Cap nhat so luong va gia mon {} trong gio hang nguoi dung {} - so luong moi: {}, gia moi: {}",
                itemId, userId, quantity, price);
        DocumentReference docRef = firestore
                .collection(CART_COLLECTION)
                .document(userId)
                .collection("cart")
                .document(itemId);

        try {
            docRef.update(
                    "quantity", quantity,
                    "price", price,
                    "updatedAt", FieldValue.serverTimestamp()
            ).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Loi khi cap nhat so luong va gia mon [{}]: {}", itemId, e.getMessage());
            Thread.currentThread().interrupt();
        }

        log.info("Da cap nhat so luong va gia mon [{}] thanh ({}, {}) trong gio hang nguoi dung {}",
                itemId, quantity, price, userId);
    }

    public void xoaMotMonTrongGio(String userId, String itemId) {
        log.info("Xóa món {} khỏi giỏ hàng người dùng {}", itemId, userId);
        DocumentReference docRef = firestore
                .collection(CART_COLLECTION)
                .document(userId)
                .collection("cart")
                .document(itemId);

        docRef.delete();
        log.info("Đã xóa món [{}] khỏi giỏ hàng người dùng {}", itemId, userId);
    }

    public FirestoreDocument layThongTinSanPham(String foodId) throws ExecutionException, InterruptedException {
        log.info("Truy vấn thông tin sản phẩm: {}", foodId);
        DocumentReference productRef = firestore.collection("products").document(foodId);
        ApiFuture<DocumentSnapshot> future = productRef.get();
        DocumentSnapshot doc = future.get();
        if (!doc.exists()) {
            log.warn("Sản phẩm [{}] không tồn tại", foodId);
            return null;
        }
        return new FirestoreDocument(doc.getData());
    }

    public FirestoreDocument layThongTinCuaHang(String storeId) throws ExecutionException, InterruptedException {
        log.info("Truy vấn thông tin cửa hàng: {}", storeId);
        DocumentReference storeRef = firestore.collection("stores").document(storeId);
        ApiFuture<DocumentSnapshot> future = storeRef.get();
        DocumentSnapshot doc = future.get();
        if (!doc.exists()) {
            log.warn("Cửa hàng [{}] không tồn tại", storeId);
            return null;
        }
        return new FirestoreDocument(doc.getData());
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class FirestoreDocument {
        private java.util.Map<String, Object> data;
        public Object get(String field) { return data.get(field); }
    }
}
