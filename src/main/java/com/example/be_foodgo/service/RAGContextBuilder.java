package com.example.be_foodgo.service;

import com.example.be_foodgo.model.*;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class RAGContextBuilder {

    private static final Logger log = LoggerFactory.getLogger(RAGContextBuilder.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

    @Autowired
    private Firestore firestore;

    public static final int FETCH_TIMEOUT_SECONDS = 8;

    public String buildContext(String userMessage, String userId) {
        StringBuilder context = new StringBuilder();

        try {
            List<String> queryKeywords = extractKeywords(userMessage);

            CompletableFuture<Void> storeFuture = CompletableFuture.runAsync(() -> {
                try {
                    String stores = fetchRelevantStores(queryKeywords);
                    if (!stores.isEmpty()) {
                        context.append(stores);
                    }
                } catch (Exception e) {
                    log.warn("Khong the lay du lieu stores: {}", e.getMessage());
                }
            });

            CompletableFuture<Void> productFuture = CompletableFuture.runAsync(() -> {
                try {
                    String products = fetchRelevantProducts(queryKeywords);
                    if (!products.isEmpty()) {
                        context.append(products);
                    }
                } catch (Exception e) {
                    log.warn("Khong the lay du lieu products: {}", e.getMessage());
                }
            });

            CompletableFuture<Void> categoryFuture = CompletableFuture.runAsync(() -> {
                try {
                    String categories = fetchCategories();
                    if (!categories.isEmpty()) {
                        context.append(categories);
                    }
                } catch (Exception e) {
                    log.warn("Khong the lay du lieu categories: {}", e.getMessage());
                }
            });

            CompletableFuture<Void> voucherFuture = CompletableFuture.runAsync(() -> {
                try {
                    String vouchers = fetchActiveVouchers();
                    if (!vouchers.isEmpty()) {
                        context.append(vouchers);
                    }
                } catch (Exception e) {
                    log.warn("Khong the lay du lieu vouchers: {}", e.getMessage());
                }
            });

            CompletableFuture<Void> userFuture = CompletableFuture.runAsync(() -> {
                if (userId != null && !userId.equals("anonymous")) {
                    try {
                        String userContext = fetchUserContext(userId);
                        if (!userContext.isEmpty()) {
                            context.append(userContext);
                        }
                    } catch (Exception e) {
                        log.warn("Khong the lay du lieu nguoi dung: {}", e.getMessage());
                    }
                }
            });

            CompletableFuture.allOf(storeFuture, productFuture, categoryFuture, voucherFuture, userFuture)
                    .get(FETCH_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Bi gian doan khi fetch RAG context: {}", e.getMessage());
        } catch (ExecutionException | TimeoutException e) {
            log.error("Loi khi fetch RAG context: {}", e.getMessage());
        }

        return context.toString();
    }

    private List<String> extractKeywords(String message) {
        if (message == null || message.isBlank()) {
            return Collections.emptyList();
        }
        String lower = message.toLowerCase();
        List<String> keywords = new ArrayList<>();

        String[] tokens = lower.split("\\s+");
        for (String token : tokens) {
            String cleaned = token.replaceAll("[^a-zA-ZÀ-ỹ\\s]", "");
            if (cleaned.length() >= 2) {
                keywords.add(cleaned);
            }
        }

        String[] foodPatterns = {
                "burger", "pizza", "café", "cafe", "cà phê", "caphe", "coffee",
                "trà sữa", "trasua", "tra sua", "bún", "bun", "phở", "pho",
                "gà", "ga", "cơm", "com", "mì", "mi", "bánh", "banh",
                "nước", "nuoc", "thức ăn", "thucan", "đồ ăn", "doan",
                "món", "mon", "tráng miệng", "trangmieng", "dessert",
                "ăn", "an", "uống", "uong", "nhậu", "nhau", "lẩu", "lau",
                "kfc", "lotteria", "mcdonald", "starbucks", " Highlands",
                " Highlands", "Bami", "Highlands", "giao", "ship", "đặt", "dat",
                "khuyến mãi", "khuyenmai", "giảm giá", "giamgia", "món mới", "monmoi"
        };
        for (String pattern : foodPatterns) {
            if (lower.contains(pattern)) {
                keywords.add(pattern);
            }
        }

        return keywords.stream().distinct().collect(Collectors.toList());
    }

    private String fetchRelevantStores(List<String> keywords) throws ExecutionException, InterruptedException {
        if (keywords.isEmpty()) {
            return fetchTopStores(5);
        }

        CollectionReference storesRef = firestore.collection("stores");
        Query query = storesRef.whereEqualTo("approvalStatus", "approved")
                              .whereEqualTo("isOpen", true)
                              .limit(10);

        ApiFuture<QuerySnapshot> future = query.get();
        QuerySnapshot snapshot = future.get();

        if (snapshot.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n=== DANH SÁCH CỬA HÀNG HIỆN CÓ ===\n");

        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            Store store = doc.toObject(Store.class);
            if (store == null) continue;

            String name = store.getName() != null ? store.getName().toLowerCase() : "";
            boolean matches = keywords.stream().anyMatch(k -> name.contains(k.toLowerCase()));

            if (matches || keywords.stream().anyMatch(k -> name.length() > 0)) {
                sb.append(formatStore(store));
            }
        }

        if (sb.length() > 100) {
            return sb.toString();
        }
        return fetchTopStores(5);
    }

    private String fetchTopStores(int limit) throws ExecutionException, InterruptedException {
        CollectionReference storesRef = firestore.collection("stores");
        Query query = storesRef.whereEqualTo("approvalStatus", "approved")
                              .whereEqualTo("isOpen", true)
                              .orderBy("rating", com.google.cloud.firestore.Query.Direction.DESCENDING)
                              .limit(limit);

        ApiFuture<QuerySnapshot> future = query.get();
        QuerySnapshot snapshot = future.get();

        if (snapshot.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n=== TOP CỬA HÀNG NỔI BẬT ===\n");

        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            Store store = doc.toObject(Store.class);
            if (store != null) {
                sb.append(formatStore(store));
            }
        }

        return sb.toString();
    }

    private String formatStore(Store store) {
        StringBuilder sb = new StringBuilder();
        sb.append("- Cửa hàng: ").append(nullSafe(store.getName())).append("\n");
        if (store.getDescription() != null) {
            sb.append("  Mô tả: ").append(store.getDescription()).append("\n");
        }
        sb.append("  Địa chỉ: ").append(nullSafe(store.getAddress())).append("\n");
        if (store.getRating() != null) {
            sb.append("  Đánh giá: ").append(String.format("%.1f", store.getRating()));
            if (store.getReviewCount() != null) {
                sb.append(" (").append(store.getReviewCount()).append(" đánh giá)");
            }
            sb.append("\n");
        }
        if (store.getDeliveryTime() != null) {
            sb.append("  Thời gian giao: ").append(store.getDeliveryTime()).append("\n");
        }
        if (store.getDeliveryFee() != null) {
            sb.append("  Phí giao hàng: ").append(formatPrice(store.getDeliveryFee())).append("\n");
        }
        sb.append("  Trạng thái: ").append(store.getIsOpen() ? "Đang mở cửa" : "Đã đóng cửa").append("\n");
        sb.append("\n");
        return sb.toString();
    }

    private String fetchRelevantProducts(List<String> keywords) throws ExecutionException, InterruptedException {
        if (keywords.isEmpty()) {
            return fetchFeaturedProducts(5);
        }

        CollectionReference productsRef = firestore.collection("products");
        ApiFuture<QuerySnapshot> future = productsRef.limit(20).get();
        QuerySnapshot snapshot = future.get();

        if (snapshot.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n=== MÓN ĂN LIÊN QUAN ===\n");

        int count = 0;
        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            if (count >= 8) break;

            Product product = doc.toObject(Product.class);
            if (product == null) continue;

            if (Boolean.TRUE.equals(product.getIsOutOfStock())) {
                continue;
            }

            String name = product.getName() != null ? product.getName().toLowerCase() : "";
            boolean matches = keywords.stream().anyMatch(k -> name.contains(k.toLowerCase()));

            if (matches) {
                sb.append(formatProduct(product));
                count++;
            }
        }

        if (count == 0) {
            return fetchFeaturedProducts(5);
        }

        return sb.toString();
    }

    private String fetchFeaturedProducts(int limit) throws ExecutionException, InterruptedException {
        CollectionReference productsRef = firestore.collection("products");
        Query query = productsRef.whereEqualTo("isFeatured", true)
                                 .whereEqualTo("isOutOfStock", false)
                                 .limit(limit);

        ApiFuture<QuerySnapshot> future = query.get();
        QuerySnapshot snapshot = future.get();

        if (snapshot.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n=== MÓN ĂN NỔI BẬT ===\n");

        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            Product product = doc.toObject(Product.class);
            if (product != null) {
                sb.append(formatProduct(product));
            }
        }

        return sb.toString();
    }

    private String formatProduct(Product product) {
        StringBuilder sb = new StringBuilder();
        sb.append("- Món: ").append(nullSafe(product.getName())).append("\n");
        if (product.getDescription() != null && !product.getDescription().isBlank()) {
            sb.append("  Mô tả: ").append(product.getDescription()).append("\n");
        }
        sb.append("  Giá: ").append(formatPrice(product.getBasePrice())).append("\n");
        if (product.getRating() != null) {
            sb.append("  Đánh giá: ").append(String.format("%.1f", product.getRating()));
            if (product.getReviewCount() != null) {
                sb.append(" (").append(product.getReviewCount()).append(" đánh giá)");
            }
            sb.append("\n");
        }
        if (product.getIsOutOfStock() != null && product.getIsOutOfStock()) {
            sb.append("  [Tạm hết hàng]\n");
        }
        sb.append("\n");
        return sb.toString();
    }

    private String fetchCategories() throws ExecutionException, InterruptedException {
        CollectionReference categoriesRef = firestore.collection("categories");
        ApiFuture<QuerySnapshot> future = categoriesRef.limit(15).get();
        QuerySnapshot snapshot = future.get();

        if (snapshot.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n=== DANH MỤC MÓN ĂN ===\n");

        Set<String> seen = new LinkedHashSet<>();
        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            Category cat = doc.toObject(Category.class);
            if (cat == null || cat.getName() == null) continue;
            if (seen.contains(cat.getName())) continue;
            seen.add(cat.getName());
            sb.append("- ").append(cat.getName());
            if (cat.getIcon() != null) {
                sb.append(" (").append(cat.getIcon()).append(")");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    private String fetchActiveVouchers() throws ExecutionException, InterruptedException {
        CollectionReference vouchersRef = firestore.collection("vouchers");
        ApiFuture<QuerySnapshot> future = vouchersRef.limit(20).get();
        QuerySnapshot snapshot = future.get();

        if (snapshot.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n=== KHUYẾN MÃI & VOUCHER ===\n");

        Date now = new Date();
        int count = 0;
        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            if (count >= 5) break;

            Boolean isActive = doc.getBoolean("isActive");
            if (!Boolean.TRUE.equals(isActive)) continue;

            Long remaining = doc.getLong("remaining");
            if (remaining == null || remaining <= 0) continue;

            Voucher voucher = doc.toObject(Voucher.class);
            if (voucher == null) continue;
            if (voucher.getExpiryDate() != null && voucher.getExpiryDate().before(now)) {
                continue;
            }
            sb.append(formatVoucher(voucher));
            count++;
        }

        if (count == 0) {
            return "";
        }
        return sb.toString();
    }

    private String formatVoucher(Voucher voucher) {
        StringBuilder sb = new StringBuilder();
        sb.append("- ").append(nullSafe(voucher.getTitle())).append("\n");
        if (voucher.getCode() != null) {
            sb.append("  Mã: ").append(voucher.getCode()).append("\n");
        }
        if (voucher.getType() == 1) {
            sb.append("  Giảm: ").append((int) voucher.getValue()).append("%");
        } else {
            sb.append("  Giảm: ").append(formatPrice(voucher.getValue()));
        }
        if (voucher.getMinOrderValue() > 0) {
            sb.append(" (đơn tối thiểu ").append(formatPrice(voucher.getMinOrderValue())).append(")");
        }
        sb.append("\n");
        if (voucher.getExpiryDate() != null) {
            sb.append("  Hết hạn: ").append(formatDate(voucher.getExpiryDate())).append("\n");
        }
        if (Boolean.TRUE.equals(voucher.getIsFreeship())) {
            sb.append("  [Freeship]\n");
        }
        sb.append("\n");
        return sb.toString();
    }

    private String fetchUserContext(String userId) throws ExecutionException, InterruptedException {
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== THÔNG TIN KHÁCH HÀNG ===\n");

        DocumentReference userDoc = firestore.collection("users").document(userId);
        DocumentSnapshot userSnap = userDoc.get().get();
        if (userSnap.exists()) {
            Map<String, Object> userData = userSnap.getData();
            if (userData != null) {
                Object fullName = userData.get("fullName");
                if (fullName != null) {
                    sb.append("Tên: ").append(String.valueOf(fullName)).append("\n");
                }
                Object phone = userData.get("phoneNumber");
                if (phone != null) {
                    sb.append("SĐT: ").append(String.valueOf(phone)).append("\n");
                }
            }
        }

        DocumentReference profileDoc = firestore.collection("customer_profiles").document(userId);
        DocumentSnapshot profileSnap = profileDoc.get().get();
        if (profileSnap.exists()) {
            Map<String, Object> data = profileSnap.getData();
            if (data != null) {
                Object addressesObj = data.get("addresses");
                if (addressesObj instanceof List && !((List<?>) addressesObj).isEmpty()) {
                    List<?> addresses = (List<?>) addressesObj;
                    if (!addresses.isEmpty() && addresses.get(0) instanceof Map) {
                        Map<?, ?> addr = (Map<?, ?>) addresses.get(0);
                        Object addrName = addr.get("address");
                        if (addrName != null) {
                            sb.append("Địa chỉ giao hàng: ").append(addrName).append("\n");
                        }
                    }
                }
                Object vouchersObj = data.get("myVouchers");
                if (vouchersObj instanceof List && !((List<?>) vouchersObj).isEmpty()) {
                    sb.append("Voucher đã lưu: ").append(((List<?>) vouchersObj).size()).append(" mã\n");
                }
            }
        }

        Query ordersQuery = firestore.collection("orders")
                .whereEqualTo("userId", userId)
                .limit(3);
        ApiFuture<QuerySnapshot> ordersFuture = ordersQuery.get();
        QuerySnapshot ordersSnap = ordersFuture.get();
        if (!ordersSnap.isEmpty()) {
            sb.append("Đơn hàng gần đây:\n");
            for (DocumentSnapshot orderDoc : ordersSnap.getDocuments()) {
                Map<String, Object> orderData = orderDoc.getData();
                if (orderData != null) {
                    sb.append("  - ");
                    Object storeName = orderData.get("storeName");
                    if (storeName != null) sb.append(String.valueOf(storeName)).append(" | ");
                    Object total = orderData.get("totalAmount");
                    if (total != null) sb.append(formatPrice(toDouble(total)));
                    Object createdAt = orderData.get("createdAt");
                    if (createdAt != null) sb.append(" | ").append(formatDate(toDate(createdAt)));
                    sb.append("\n");
                }
            }
        }

        return sb.toString();
    }

    private double toDouble(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        try { return Double.parseDouble(String.valueOf(obj)); } catch (Exception e) { return 0; }
    }

    private String nullSafe(String val) {
        return val != null ? val : "(không có thông tin)";
    }

    private String formatPrice(double price) {
        return String.format("%.0f", price).replaceAll(",", ".") + " VND";
    }

    private String formatDate(Date date) {
        if (date == null) return "(không rõ)";
        return DATE_FORMAT.format(date.toInstant().atZone(ZoneId.of("Asia/Ho_Chi_Minh")));
    }

    private Date toDate(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Date) return (Date) obj;
        if (obj instanceof com.google.protobuf.Timestamp) {
            com.google.protobuf.Timestamp ts = (com.google.protobuf.Timestamp) obj;
            return Date.from(Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos()));
        }
        if (obj instanceof Long) {
            return new Date((Long) obj);
        }
        return null;
    }
}
