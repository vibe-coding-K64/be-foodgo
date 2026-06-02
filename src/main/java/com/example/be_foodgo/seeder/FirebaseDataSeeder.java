package com.example.be_foodgo.seeder;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.cloud.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

 @Component
public class FirebaseDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(FirebaseDataSeeder.class);

    private final Firestore firestore;

    @Autowired
    public FirebaseDataSeeder(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Bat dau qua trinh seed du lieu Firebase...");
        long startTime = System.currentTimeMillis();

        seedSystemConfigs();
        seedWallets();
        seedTransactions();
        seedUsers();
        seedSystemCategories();
        seedStoreCategories();
        seedStores();
        seedProducts();
        seedBanners();
        seedVouchers();
        seedReviews();
        seedOrders();
        seedCustomerProfiles();
        seedDriverProfiles();
        seedMerchantProfiles();
        seedAdminProfiles();

        long endTime = System.currentTimeMillis();
        log.info("Hoan tat qua trinh seed du lieu Firebase trong {} ms.", (endTime - startTime));
    }

    private void kiemTraVaSeed(String collectionName, List<Map<String, Object>> documents) {
        try {
            ApiFuture<QuerySnapshot> query = firestore.collection(collectionName).limit(1).get();
            QuerySnapshot querySnapshot = query.get();
            if (!querySnapshot.isEmpty()) {
                log.info("Collection [{}] da co du lieu, bo qua viec seed.", collectionName);
                return;
            }
            WriteBatch batch = firestore.batch();
            for (Map<String, Object> doc : documents) {
                String docId = (String) doc.get("id");
                batch.set(firestore.collection(collectionName).document(docId), doc);
            }
            batch.commit();
            log.info("Da seed {} document vao collection [{}].", documents.size(), collectionName);
        } catch (Exception e) {
            log.error("Loi khi seed collection [{}]: {}", collectionName, e.getMessage());
        }
    }

    private Map<String, Object> entry(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    private void seedSystemConfigs() {
        String collectionName = "system_configs";
        List<Map<String, Object>> configs = Arrays.asList(
                Map.ofEntries(
                        Map.entry("id", "config_001"),
                        Map.entry("platformFeePercentage", 15.0),
                        Map.entry("baseDeliveryFee", 15000.0),
                        Map.entry("minDeliveryFee", 5000.0),
                        Map.entry("maxDeliveryFee", 50000.0),
                        Map.entry("driverCommissionPercentage", 80.0),
                        Map.entry("merchantCommissionPercentage", 85.0),
                        Map.entry("minWithdrawalAmount", 50000.0),
                        Map.entry("maxWithdrawalAmount", 50000000.0),
                        Map.entry("appVersion", "1.0.0"),
                        Map.entry("maintenanceMode", false),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                )
        );
        kiemTraVaSeed(collectionName, configs);
    }

    private void seedWallets() {
        String collectionName = "wallets";
        List<Map<String, Object>> wallets = Arrays.asList(
                Map.ofEntries(
                        Map.entry("id", "wallet_001"),
                        Map.entry("userId", "user_001"),
                        Map.entry("role", 1),
                        Map.entry("balance", 2500000.0),
                        Map.entry("totalEarned", 5000000.0),
                        Map.entry("totalWithdrawn", 2500000.0),
                        Map.entry("pendingBalance", 0.0),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "wallet_002"),
                        Map.entry("userId", "user_001"),
                        Map.entry("role", 2),
                        Map.entry("balance", 850000.0),
                        Map.entry("totalEarned", 1500000.0),
                        Map.entry("totalWithdrawn", 650000.0),
                        Map.entry("pendingBalance", 0.0),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "wallet_003"),
                        Map.entry("userId", "user_003"),
                        Map.entry("role", 2),
                        Map.entry("balance", 1200000.0),
                        Map.entry("totalEarned", 2000000.0),
                        Map.entry("totalWithdrawn", 800000.0),
                        Map.entry("pendingBalance", 0.0),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "wallet_004"),
                        Map.entry("userId", "user_006"),
                        Map.entry("role", 2),
                        Map.entry("balance", 950000.0),
                        Map.entry("totalEarned", 1800000.0),
                        Map.entry("totalWithdrawn", 850000.0),
                        Map.entry("pendingBalance", 0.0),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "wallet_005"),
                        Map.entry("userId", "user_005"),
                        Map.entry("role", 2),
                        Map.entry("balance", 750000.0),
                        Map.entry("totalEarned", 1500000.0),
                        Map.entry("totalWithdrawn", 750000.0),
                        Map.entry("pendingBalance", 0.0),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "wallet_006"),
                        Map.entry("userId", "user_004"),
                        Map.entry("role", 1),
                        Map.entry("balance", 1500000.0),
                        Map.entry("totalEarned", 3000000.0),
                        Map.entry("totalWithdrawn", 1500000.0),
                        Map.entry("pendingBalance", 0.0),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                )
        );
        kiemTraVaSeed(collectionName, wallets);
    }

    private void seedTransactions() {
        String collectionName = "transactions";
        List<Map<String, Object>> transactions = Arrays.asList(
                Map.ofEntries(
                        Map.entry("id", "trans_001"),
                        Map.entry("walletId", "wallet_001"),
                        Map.entry("userId", "user_001"),
                        Map.entry("type", 1),
                        Map.entry("amount", 76500.0),
                        Map.entry("fee", 11475.0),
                        Map.entry("netAmount", 65025.0),
                        Map.entry("description", "Đơn hàng order_001 - Phiên bản trừ phí hoa hồng"),
                        Map.entry("orderId", "order_001"),
                        Map.entry("status", 1),
                        Map.entry("createdAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "trans_002"),
                        Map.entry("walletId", "wallet_002"),
                        Map.entry("userId", "user_001"),
                        Map.entry("type", 2),
                        Map.entry("amount", 15000.0),
                        Map.entry("fee", 3000.0),
                        Map.entry("netAmount", 12000.0),
                        Map.entry("description", "Thu nhập giao hàng đơn order_001"),
                        Map.entry("orderId", "order_001"),
                        Map.entry("status", 1),
                        Map.entry("createdAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "trans_003"),
                        Map.entry("walletId", "wallet_003"),
                        Map.entry("userId", "user_003"),
                        Map.entry("type", 3),
                        Map.entry("amount", 200000.0),
                        Map.entry("fee", 0.0),
                        Map.entry("netAmount", 200000.0),
                        Map.entry("description", "Rút tiền về tài khoản ngân hàng"),
                        Map.entry("status", 1),
                        Map.entry("createdAt", FieldValue.serverTimestamp())
                )
        );
        kiemTraVaSeed(collectionName, transactions);
    }

    private void seedUsers() {
        String collectionName = "users";
        List<Map<String, Object>> users = Arrays.asList(
                Map.ofEntries(
                        Map.entry("id", "user_001"),
                        Map.entry("email", "khachhang@gmail.com"),
                        Map.entry("password", "$2a$10$o2RRBNQzzEn2zXiKJ9g2Z.m.UybDFl4pduE85Z5RCdtI5NQl3D8Q."),
                        Map.entry("fullName", "Khoi"),
                        Map.entry("phoneNumber", "0123456789"),
                        Map.entry("photoUrl", "https://example.com/avatar/user001.jpg"),
                        Map.entry("roles", Arrays.asList(1, 2, 3)),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp()),
                        Map.entry("isEmailVerified", true)
                ),
                Map.ofEntries(
                        Map.entry("id", "user_002"),
                        Map.entry("email", "admin@foodgo.com"),
                        Map.entry("password", "$2a$10$tjUxP9PIZuJs4Q648Ojkneu.tDJ.QFd3SBs4rP9bgtbJINXm70iBW"),
                        Map.entry("fullName", "Quan Tri Vien"),
                        Map.entry("phoneNumber", "0987654321"),
                        Map.entry("photoUrl", "https://example.com/avatar/admin.jpg"),
                        Map.entry("roles", Arrays.asList(4)),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp()),
                        Map.entry("isEmailVerified", true)
                ),
                Map.ofEntries(
                        Map.entry("id", "user_003"),
                        Map.entry("email", "taixe@gmail.com"),
                        Map.entry("password", "$2a$10$Ml9G9qbGlknmSXigd4LyJOnMuSoD8uGpiupnUn8D3Dd5jpjfzvp0a"),
                        Map.entry("fullName", "Le Van B"),
                        Map.entry("phoneNumber", "0912345678"),
                        Map.entry("photoUrl", "https://example.com/avatar/driver001.jpg"),
                        Map.entry("roles", Arrays.asList(2)),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp()),
                        Map.entry("isEmailVerified", true)
                ),
                Map.ofEntries(
                        Map.entry("id", "user_006"),
                        Map.entry("email", "taixe2@gmail.com"),
                        Map.entry("password", "$2a$10$OX1yl/AJRIlkBcFVn8jwhu.NowCl1ZO9y70TNU5r0kAbTd916UACu"),
                        Map.entry("fullName", "Nguyen Van C"),
                        Map.entry("phoneNumber", "0923456789"),
                        Map.entry("photoUrl", "https://example.com/avatar/driver002.jpg"),
                        Map.entry("roles", Arrays.asList(2)),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp()),
                        Map.entry("isEmailVerified", true)
                ),
                Map.ofEntries(
                        Map.entry("id", "user_005"),
                        Map.entry("email", "taixe3@gmail.com"),
                        Map.entry("password", "$2a$10$wi/S7awfTrfZMIoiczESfO9H2nQbEY6/DoajfGwUAWas2YMM96U/u"),
                        Map.entry("fullName", "Tran Van D"),
                        Map.entry("phoneNumber", "0934567890"),
                        Map.entry("photoUrl", "https://example.com/avatar/driver003.jpg"),
                        Map.entry("roles", Arrays.asList(2)),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp()),
                        Map.entry("isEmailVerified", true)
                ),
                Map.ofEntries(
                        Map.entry("id", "user_004"),
                        Map.entry("email", "luudinhnghia30012005@gmail.com"),
                        Map.entry("password", "$2a$10$H5DqZGW/bgSgXbls3hiDpeqKax8jFr9H73OADH5F.RFdRPP7g7Fsi"),
                        Map.entry("fullName", "Lưu Nghĩa"),
                        Map.entry("phoneNumber", "0337681072"),
                        Map.entry("photoUrl", "https://placehold.co/150"),
                        Map.entry("roles", Arrays.asList(3)),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp()),
                        Map.entry("isEmailVerified", true)
                )
        );
        kiemTraVaSeed(collectionName, users);

        seedSearchHistory();
    }

    private void seedSearchHistory() {
        String userId = "user_001";
        String collectionName = "users/" + userId + "/search_history";
        List<Map<String, Object>> histories = Arrays.asList(
                Map.ofEntries(
                        Map.entry("id", "sh_001"),
                        Map.entry("keyword", "cơm tấm"),
                        Map.entry("keywordNormalized", "cơm tấm"),
                        Map.entry("createdAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "sh_002"),
                        Map.entry("keyword", "trà sữa"),
                        Map.entry("keywordNormalized", "trà sữa"),
                        Map.entry("createdAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "sh_003"),
                        Map.entry("keyword", "gà rán"),
                        Map.entry("keywordNormalized", "gà rán"),
                        Map.entry("createdAt", FieldValue.serverTimestamp())
                )
        );
        kiemTraVaSeed(collectionName, histories);
    }

    private void seedSystemCategories() {
        String collectionName = "categories";
        List<Map<String, Object>> categories = Arrays.asList(
                createCategoryMap("syscate_001", null, "Cơm", "restaurant", 1, "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=400&q=80"),
                createCategoryMap("syscate_002", null, "Phở/Bún", "restaurant", 2, "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=400&q=80"),
                createCategoryMap("syscate_003", null, "Trà sữa", "local_cafe", 3, "https://images.unsplash.com/photo-1558857563-b371033873b8?w=400&q=80"),
                createCategoryMap("syscate_004", null, "Ăn vặt", "fastfood", 4, "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&q=80"),
                createCategoryMap("syscate_005", null, "Gà rán", "fastfood", 5, "https://images.unsplash.com/photo-1626645738196-c2a7c87a8f58?w=400&q=80"),
                createCategoryMap("syscate_006", null, "Món Hàn", "restaurant", 6, "https://images.unsplash.com/photo-1559314809-0d155014e29e?w=400&q=80"),
                createCategoryMap("syscate_007", null, "Món Nhật", "restaurant", 7, "https://images.unsplash.com/photo-1617196034183-421b4040ed20?w=400&q=80"),
                createCategoryMap("syscate_008", null, "Bánh mì", "bakery_dining", 8, "https://images.unsplash.com/photo-1605478371119-43802a1c79f5?w=400&q=80"),
                createCategoryMap("syscate_009", null, "Lẩu/Buffet", "restaurant", 9, "https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=400&q=80"),
                createCategoryMap("syscate_010", null, "Trà cây", "local_cafe", 10, "https://images.unsplash.com/photo-1550508776-b6a354f5f66d?w=400&q=80")
        );
        kiemTraVaSeed(collectionName, categories);
    }

    private Map<String, Object> createCategoryMap(String id, String storeId, String name, String icon, int order, String imageUrl) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("storeId", storeId);
        map.put("name", name);
        map.put("icon", icon);
        map.put("order", order);
        map.put("imageUrl", imageUrl);
        map.put("createdAt", FieldValue.serverTimestamp());
        map.put("updatedAt", FieldValue.serverTimestamp());
        return map;
    }

    private void seedStoreCategories() {
        String collectionName = "categories";
        List<Map<String, Object>> categories = Arrays.asList(
                createCategoryMap("stocate_001", "store_001", "Món chính", "restaurant", 1, "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=400&q=80"),
                createCategoryMap("stocate_002", "store_001", "Món phụ", "restaurant", 2, "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&q=80"),
                createCategoryMap("stocate_003", "store_001", "Nước uống", "local_cafe", 3, "https://images.unsplash.com/photo-1558857563-b371033873b8?w=400&q=80"),
                createCategoryMap("stocate_004", "store_002", "Trà sữa", "local_cafe", 1, "https://images.unsplash.com/photo-1558857563-b371033873b8?w=400&q=80"),
                createCategoryMap("stocate_005", "store_002", "Trà trái cây", "local_cafe", 2, "https://images.unsplash.com/photo-1550508776-b6a354f5f66d?w=400&q=80"),
                createCategoryMap("stocate_006", "store_003", "Bánh mì", "bakery_dining", 1, "https://images.unsplash.com/photo-1605478371119-43802a1c79f5?w=400&q=80"),
                createCategoryMap("stocate_007", "store_003", "Đồ ăn thêm", "fastfood", 2, "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&q=80"),
                createCategoryMap("stocate_008", "store_005", "Bún chả", "restaurant", 1, "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=400&q=80")
        );
        kiemTraVaSeed(collectionName, categories);
    }

    private void seedStores() {
        String collectionName = "stores";
        List<Map<String, Object>> stores = Arrays.asList(
                Map.ofEntries(
                        Map.entry("id", "store_001"),
                        Map.entry("name", "Cơm Tấm Phúc Lộc Thọ"),
                        Map.entry("description", "Quán cơm tấm ngon nhất khu vực TP. Thủ Đức với sườn nướng và bì chả đậm đà."),
                        Map.entry("address", "123 Lê Văn Việt, TP. Thủ Đức"),
                        Map.entry("rating", 4.8),
                        Map.entry("reviewCount", 500),
                        Map.entry("avtUrl", "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=400&q=80"),
                        Map.entry("backUrl", "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=800&q=80"),
                        Map.entry("isOpen", true),
                        Map.entry("deliveryTime", "20-30 phút"),
                        Map.entry("deliveryFee", 15000.0),
                        Map.entry("categoryIds", Arrays.asList("cate_001", "cate_004")),
                        Map.entry("lat", 10.8500),
                        Map.entry("lng", 106.7900),
                        Map.entry("restaurant_categories", Map.of(
                                "rest_cate_001", Map.of(
                                        "name", "Món chính",
                                        "order", 1,
                                        "createdAt", FieldValue.serverTimestamp(),
                                        "updatedAt", FieldValue.serverTimestamp()
                                ),
                                "rest_cate_002", Map.of(
                                        "name", "Món phụ",
                                        "order", 2,
                                        "createdAt", FieldValue.serverTimestamp(),
                                        "updatedAt", FieldValue.serverTimestamp()
                                )
                        )),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "store_002"),
                        Map.entry("name", "Trà Sữa Tocotoco"),
                        Map.entry("description", "Trà sữa và đồ uống ngon với nhiều loại topping phong phú."),
                        Map.entry("address", "456 Nguyễn Thi Định, TP. Thủ Đức"),
                        Map.entry("rating", 4.6),
                        Map.entry("reviewCount", 300),
                        Map.entry("avtUrl", "https://images.unsplash.com/photo-1558857563-b371033873b8?w=400&q=80"),
                        Map.entry("backUrl", "https://images.unsplash.com/photo-1557992260-ec58fa23b80b?w=800&q=80"),
                        Map.entry("isOpen", true),
                        Map.entry("deliveryTime", "15-25 phút"),
                        Map.entry("deliveryFee", 12000.0),
                        Map.entry("categoryIds", Arrays.asList("cate_003", "cate_010")),
                        Map.entry("lat", 10.8520),
                        Map.entry("lng", 106.7850),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "store_003"),
                        Map.entry("name", "Gà Rán KFC Nguyễn Cửu"),
                        Map.entry("description", "Gà rán giòn rụm, đa dạng menu với giá cả hợp lý."),
                        Map.entry("address", "789 Nguyễn Cửu, TP. Thủ Đức"),
                        Map.entry("rating", 4.5),
                        Map.entry("reviewCount", 800),
                        Map.entry("avtUrl", "https://images.unsplash.com/photo-1626645738196-c2a7c87a8f58?w=400&q=80"),
                        Map.entry("backUrl", "https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?w=800&q=80"),
                        Map.entry("isOpen", true),
                        Map.entry("deliveryTime", "25-35 phút"),
                        Map.entry("deliveryFee", 18000.0),
                        Map.entry("categoryIds", Arrays.asList("cate_005")),
                        Map.entry("lat", 10.8480),
                        Map.entry("lng", 106.7920),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "store_004"),
                        Map.entry("name", "Bún Bò Huế Ba Lẻ"),
                        Map.entry("description", "Bún bò Huế chuẩn vị với nước dùng đậm đà, thịt bò tươi ngon."),
                        Map.entry("address", "101 Phố Huế, Q.1, TP.HCM"),
                        Map.entry("rating", 4.7),
                        Map.entry("reviewCount", 450),
                        Map.entry("avtUrl", "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=400&q=80"),
                        Map.entry("backUrl", "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=800&q=80"),
                        Map.entry("isOpen", true),
                        Map.entry("deliveryTime", "30-40 phút"),
                        Map.entry("deliveryFee", 20000.0),
                        Map.entry("categoryIds", Arrays.asList("cate_002")),
                        Map.entry("lat", 10.8460),
                        Map.entry("lng", 106.7880),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "store_005"),
                        Map.entry("name", "Quán Bún Chả"),
                        Map.entry("description", "Bún chả Hà Nội chuẩn vị với thịt nướng than hoa thơm lừng."),
                        Map.entry("address", "Lê Văn Việt"),
                        Map.entry("rating", 5.0),
                        Map.entry("reviewCount", 120),
                        Map.entry("avtUrl", "https://tse3.mm.bing.net/th?id=OIF.k%2fmH6P9NM9GRsGE1VREFSw&pid=Api&P=0&h=180"),
                        Map.entry("backUrl", "https://tse3.mm.bing.net/th?id=OIF.0%2fwhOkeGa%2brJy%2f4BHVY9RA&pid=Api&P=0&h=180"),
                        Map.entry("isOpen", true),
                        Map.entry("deliveryTime", "20-30 phút"),
                        Map.entry("deliveryFee", 20000.0),
                        Map.entry("categoryIds", Arrays.asList("cate_002")),
                        Map.entry("lat", 10.8510),
                        Map.entry("lng", 106.7860),
                        Map.entry("restaurant_categories", Map.of(
                                "rest_cate_003", Map.of(
                                        "name", "Bún chả",
                                        "order", 1,
                                        "createdAt", FieldValue.serverTimestamp(),
                                        "updatedAt", FieldValue.serverTimestamp()
                                )
                        )),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                )
        );
        kiemTraVaSeed(collectionName, stores);
    }

    private void seedProducts() {
        String collectionName = "products";
        List<Map<String, Object>> products = Arrays.asList(
                Map.ofEntries(
                        Map.entry("id", "prod_001"),
                        Map.entry("storeId", "store_001"),
                        Map.entry("categoryId", "cate_001"),
                        Map.entry("categoryName", "Cơm"),
                        Map.entry("name", "Cơm tấm sườn bì chả"),
                        Map.entry("description", "Cơm tấm ngon chuẩn vị Sài Gòn với sườn nướng thơm phức"),
                        Map.entry("basePrice", 45000.0),
                        Map.entry("imageUrl", "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=400&q=80"),
                        Map.entry("isOutOfStock", false),
                        Map.entry("isFeatured", true),
                        Map.entry("optionGroups", List.of(
                                Map.of(
                                        "name", "Kích thước",
                                        "isSingleSelect", true,
                                        "options", List.of(
                                                Map.of("name", "Vừa", "price", 0.0),
                                                Map.of("name", "Lớn", "price", 10000.0)
                                        )
                                )
                        )),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp()),
                        Map.entry("rating", 4.5),
                        Map.entry("reviewCount", 2)
                ),
                Map.ofEntries(
                        Map.entry("id", "prod_002"),
                        Map.entry("storeId", "store_001"),
                        Map.entry("categoryId", "cate_001"),
                        Map.entry("categoryName", "Cơm"),
                        Map.entry("name", "Cơm tấm gà xối mỡ"),
                        Map.entry("description", "Cơm tấm với gà xối mỡ giòn tan"),
                        Map.entry("basePrice", 50000.0),
                        Map.entry("imageUrl", "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&q=80"),
                        Map.entry("isOutOfStock", false),
                        Map.entry("isFeatured", false),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp()),
                        Map.entry("rating", 4.0),
                        Map.entry("reviewCount", 1)
                ),
                Map.ofEntries(
                        Map.entry("id", "prod_003"),
                        Map.entry("storeId", "store_001"),
                        Map.entry("categoryId", "cate_004"),
                        Map.entry("categoryName", "Ăn vặt"),
                        Map.entry("name", "Bánh bột lọc"),
                        Map.entry("description", "Bánh bột lọc hấp chần, nướng giòn"),
                        Map.entry("basePrice", 25000.0),
                        Map.entry("imageUrl", "https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?w=400&q=80"),
                        Map.entry("isOutOfStock", false),
                        Map.entry("isFeatured", false),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp()),
                        Map.entry("rating", 5.0),
                        Map.entry("reviewCount", 1)
                ),
                Map.ofEntries(
                        Map.entry("id", "prod_004"),
                        Map.entry("storeId", "store_002"),
                        Map.entry("categoryId", "cate_003"),
                        Map.entry("categoryName", "Trà sữa"),
                        Map.entry("name", "Trà sữa trà chanh"),
                        Map.entry("description", "Trà sữa thơm ngát với trà mạch móc và trà chanh đài"),
                        Map.entry("basePrice", 29000.0),
                        Map.entry("imageUrl", "https://images.unsplash.com/photo-1558857563-b371033873b8?w=400&q=80"),
                        Map.entry("isOutOfStock", false),
                        Map.entry("isFeatured", true),
                        Map.entry("optionGroups", List.of(
                                Map.of(
                                        "name", "Kích thước",
                                        "isSingleSelect", true,
                                        "options", List.of(
                                                Map.of("name", "M", "price", 0.0),
                                                Map.of("name", "L", "price", 5000.0)
                                        )
                                ),
                                Map.of(
                                        "name", "Topping",
                                        "isSingleSelect", false,
                                        "options", List.of(
                                                Map.of("name", "Trân châu", "price", 5000.0),
                                                Map.of("name", "Thạch", "price", 3000.0),
                                                Map.of("name", "Pudding", "price", 6000.0)
                                        )
                                )
                        )),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp()),
                        Map.entry("rating", 5.0),
                        Map.entry("reviewCount", 1)
                ),
                Map.ofEntries(
                        Map.entry("id", "prod_005"),
                        Map.entry("storeId", "store_002"),
                        Map.entry("categoryId", "cate_010"),
                        Map.entry("categoryName", "Trà cây"),
                        Map.entry("name", "Trà đào cam"),
                        Map.entry("description", "Trà đào cam thật hương vị đài"),
                        Map.entry("basePrice", 25000.0),
                        Map.entry("imageUrl", "https://images.unsplash.com/photo-1550508776-b6a354f5f66d?w=400&q=80"),
                        Map.entry("isOutOfStock", false),
                        Map.entry("isFeatured", false),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp()),
                        Map.entry("rating", 5.0),
                        Map.entry("reviewCount", 1)
                ),
                Map.ofEntries(
                        Map.entry("id", "prod_006"),
                        Map.entry("storeId", "store_002"),
                        Map.entry("categoryId", "cate_003"),
                        Map.entry("categoryName", "Trà sữa"),
                        Map.entry("name", "Trà sữa khoai môn"),
                        Map.entry("description", "Trà sữa kem dưỡng bự với khoai môn ngọt tan"),
                        Map.entry("basePrice", 33000.0),
                        Map.entry("imageUrl", "https://images.unsplash.com/photo-1557992260-ec58fa23b80b?w=400&q=80"),
                        Map.entry("isOutOfStock", true),
                        Map.entry("isFeatured", false),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp()),
                        Map.entry("rating", 3.5),
                        Map.entry("reviewCount", 2)
                ),
                Map.ofEntries(
                        Map.entry("id", "prod_007"),
                        Map.entry("storeId", "store_003"),
                        Map.entry("categoryId", "cate_005"),
                        Map.entry("categoryName", "Gà rán"),
                        Map.entry("name", "Gà lăp xường"),
                        Map.entry("description", "Gà lăp xường giòn ơi, thịt nóng mặc, nấu từ bột pháp"),
                        Map.entry("basePrice", 55000.0),
                        Map.entry("imageUrl", "https://images.unsplash.com/photo-1626645738196-c2a7c87a8f58?w=400&q=80"),
                        Map.entry("isOutOfStock", false),
                        Map.entry("isFeatured", true),
                        Map.entry("optionGroups", List.of(
                                Map.of(
                                        "name", "Phần ăn",
                                        "isSingleSelect", true,
                                        "options", List.of(
                                                Map.of("name", "1 phần", "price", 0.0),
                                                Map.of("name", "2 phần", "price", 20000.0)
                                        )
                                )
                        )),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "prod_008"),
                        Map.entry("storeId", "store_003"),
                        Map.entry("categoryId", "cate_005"),
                        Map.entry("categoryName", "Gà rán"),
                        Map.entry("name", "Mì gà chua cay"),
                        Map.entry("description", "Mì gà nấu chua cay đậm đà, hậu sửa nuôi"),
                        Map.entry("basePrice", 35000.0),
                        Map.entry("imageUrl", "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=400&q=80"),
                        Map.entry("isOutOfStock", false),
                        Map.entry("isFeatured", false),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "prod_009"),
                        Map.entry("storeId", "store_003"),
                        Map.entry("categoryId", "cate_005"),
                        Map.entry("categoryName", "Gà rán"),
                        Map.entry("name", "Khoai tây chiên"),
                        Map.entry("description", "Khoai tây chiên giòn thật đài"),
                        Map.entry("basePrice", 20000.0),
                        Map.entry("imageUrl", "https://images.unsplash.com/photo-1573080496219-bb080dd4f877?w=400&q=80"),
                        Map.entry("isOutOfStock", false),
                        Map.entry("isFeatured", false),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "prod_010"),
                        Map.entry("storeId", "store_004"),
                        Map.entry("categoryId", "cate_002"),
                        Map.entry("categoryName", "Phở/Bún"),
                        Map.entry("name", "Bún bò Huế"),
                        Map.entry("description", "Bún bò Huế nước dùng trong, thịt bò chín mỏng, chả cua thơm phức"),
                        Map.entry("basePrice", 45000.0),
                        Map.entry("imageUrl", "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=400&q=80"),
                        Map.entry("isOutOfStock", false),
                        Map.entry("isFeatured", true),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp()),
                        Map.entry("rating", 5.0),
                        Map.entry("reviewCount", 1)
                ),
                Map.ofEntries(
                        Map.entry("id", "prod_011"),
                        Map.entry("storeId", "store_004"),
                        Map.entry("categoryId", "cate_002"),
                        Map.entry("categoryName", "Phở/Bún"),
                        Map.entry("name", "Bún mắm"),
                        Map.entry("description", "Bún mắm đặc sản Vũng Tàu với cá bôm và cua"),
                        Map.entry("basePrice", 55000.0),
                        Map.entry("imageUrl", "https://images.unsplash.com/photo-1559314809-0d155014e29e?w=400&q=80"),
                        Map.entry("isOutOfStock", false),
                        Map.entry("isFeatured", false),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "prod_012"),
                        Map.entry("storeId", "store_004"),
                        Map.entry("categoryId", "cate_002"),
                        Map.entry("categoryName", "Phở/Bún"),
                        Map.entry("name", "Bún riêu"),
                        Map.entry("description", "Bún riêu cua thật ngon với riêu nấu tôm chất"),
                        Map.entry("basePrice", 40000.0),
                        Map.entry("imageUrl", "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=400&q=80"),
                        Map.entry("isOutOfStock", false),
                        Map.entry("isFeatured", false),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp()),
                        Map.entry("rating", 5.0),
                        Map.entry("reviewCount", 1)
                ),
                Map.ofEntries(
                        Map.entry("id", "prod_013"),
                        Map.entry("storeId", "store_001"),
                        Map.entry("categoryId", "cate_001"),
                        Map.entry("categoryName", "Cơm"),
                        Map.entry("name", "Cơm sườn trộn"),
                        Map.entry("description", "Cơm sườn trộn trứng thập cẩm"),
                        Map.entry("basePrice", 48000.0),
                        Map.entry("imageUrl", "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=400&q=80"),
                        Map.entry("isOutOfStock", false),
                        Map.entry("isFeatured", false),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "prod_014"),
                        Map.entry("storeId", "store_002"),
                        Map.entry("categoryId", "cate_003"),
                        Map.entry("categoryName", "Trà sữa"),
                        Map.entry("name", "Trà sữa trái cây"),
                        Map.entry("description", "Trà sữa thập cẩm với trái cây tươi ngon"),
                        Map.entry("basePrice", 32000.0),
                        Map.entry("imageUrl", "https://images.unsplash.com/photo-1558857563-b371033873b8?w=400&q=80"),
                        Map.entry("isOutOfStock", false),
                        Map.entry("isFeatured", true),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "prod_015"),
                        Map.entry("storeId", "store_001"),
                        Map.entry("categoryId", "cate_004"),
                        Map.entry("categoryName", "Ăn vặt"),
                        Map.entry("name", "Giò chả"),
                        Map.entry("description", "Giò chả bì thơm ngon chất lượng"),
                        Map.entry("basePrice", 15000.0),
                        Map.entry("imageUrl", "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&q=80"),
                        Map.entry("isOutOfStock", false),
                        Map.entry("isFeatured", false),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "prod_016"),
                        Map.entry("storeId", "store_005"),
                        Map.entry("categoryId", "cate_002"),
                        Map.entry("categoryName", "Phở/Bún"),
                        Map.entry("name", "Bún chả Hà Nội"),
                        Map.entry("description", "Bún chả chuẩn vị Hà Nội với thịt nướng than hoa thơm lừng"),
                        Map.entry("basePrice", 50000.0),
                        Map.entry("imageUrl", "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=400&q=80"),
                        Map.entry("isOutOfStock", false),
                        Map.entry("isFeatured", true),
                        Map.entry("optionGroups", List.of(
                                Map.of(
                                        "name", "Kích thước",
                                        "isSingleSelect", true,
                                        "options", List.of(
                                                Map.of("name", "Phần nhỏ", "price", 0.0),
                                                Map.of("name", "Phần đặc biệt", "price", 15000.0)
                                        )
                                ),
                                Map.of(
                                        "name", "Gọi thêm",
                                        "isSingleSelect", false,
                                        "options", List.of(
                                                Map.of("name", "Thêm nem cua bể", "price", 15000.0),
                                                Map.of("name", "Thêm chả", "price", 10000.0)
                                        )
                                )
                        )),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "prod_017"),
                        Map.entry("storeId", "store_005"),
                        Map.entry("categoryId", "cate_002"),
                        Map.entry("categoryName", "Phở/Bún"),
                        Map.entry("name", "Nem cua bể"),
                        Map.entry("description", "Nem cua bể giòn rụm, nhân thịt tôm cua đậm đà"),
                        Map.entry("basePrice", 25000.0),
                        Map.entry("imageUrl", "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&q=80"),
                        Map.entry("isOutOfStock", false),
                        Map.entry("isFeatured", false),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                )
        );
        kiemTraVaSeed(collectionName, products);
    }

    private void seedBanners() {
        String collectionName = "banners";
        List<Map<String, Object>> banners = new ArrayList<>();

        Map<String, Object> banner1 = new HashMap<>();
        banner1.put("id", "banner_001");
        banner1.put("title", "Siêu sale giữa tháng");
        banner1.put("imageUrl", "https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?w=800&q=80");
        banner1.put("storeId", null);
        banner1.put("storeName", null);
        banner1.put("isActive", true);
        banner1.put("order", 1);
        banner1.put("createdAt", FieldValue.serverTimestamp());
        banner1.put("updatedAt", FieldValue.serverTimestamp());
        banners.add(banner1);

        Map<String, Object> banner2 = new HashMap<>();
        banner2.put("id", "banner_002");
        banner2.put("title", "Freeship 0 đồng");
        banner2.put("imageUrl", "https://images.unsplash.com/photo-1550508776-b6a354f5f66d?w=800&q=80");
        banner2.put("storeId", null);
        banner2.put("storeName", null);
        banner2.put("isActive", true);
        banner2.put("order", 2);
        banner2.put("createdAt", FieldValue.serverTimestamp());
        banner2.put("updatedAt", FieldValue.serverTimestamp());
        banners.add(banner2);

        Map<String, Object> banner3 = new HashMap<>();
        banner3.put("id", "banner_003");
        banner3.put("title", "Lễ hội ẩm thực");
        banner3.put("imageUrl", "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=800&q=80");
        banner3.put("storeId", null);
        banner3.put("storeName", null);
        banner3.put("isActive", true);
        banner3.put("order", 3);
        banner3.put("createdAt", FieldValue.serverTimestamp());
        banner3.put("updatedAt", FieldValue.serverTimestamp());
        banners.add(banner3);

        Map<String, Object> banner4 = new HashMap<>();
        banner4.put("id", "banner_004");
        banner4.put("title", "Uống trà vẫn chiều");
        banner4.put("imageUrl", "https://images.unsplash.com/photo-1558857563-b371033873b8?w=800&q=80");
        banner4.put("storeId", null);
        banner4.put("storeName", null);
        banner4.put("isActive", true);
        banner4.put("order", 4);
        banner4.put("createdAt", FieldValue.serverTimestamp());
        banner4.put("updatedAt", FieldValue.serverTimestamp());
        banners.add(banner4);

        kiemTraVaSeed(collectionName, banners);
    }

    private void seedVouchers() {
        String collectionName = "vouchers";
        List<Map<String, Object>> vouchers = new ArrayList<>();

        Map<String, Object> v1 = new HashMap<>();
        v1.put("id", "voucher_001");
        v1.put("storeId", null);
        v1.put("title", "Giảm 20K cho đơn từ 100K");
        v1.put("subtitle", "Dành cho khách hàng mới");
        v1.put("code", "GIAM20K");
        v1.put("type", 2);
        v1.put("value", 20000.0);
        v1.put("pointsRequired", 200);
        v1.put("imageUrl", "https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?w=400&q=80");
        v1.put("remaining", 100);
        v1.put("isActive", true);
        v1.put("validityDays", 30);
        v1.put("terms", "Áp dụng cho tất cả quán ăn.");
        v1.put("minOrderValue", 100000.0);
        v1.put("limitCount", 200);
        v1.put("usedCount", 0);
        v1.put("expiryDate", Timestamp.ofTimeSecondsAndNanos(Instant.now().plus(30, ChronoUnit.DAYS).getEpochSecond(), 0));
        v1.put("isFreeship", false);
        v1.put("createdAt", FieldValue.serverTimestamp());
        v1.put("updatedAt", FieldValue.serverTimestamp());
        vouchers.add(v1);

        Map<String, Object> v2 = new HashMap<>();
        v2.put("id", "voucher_002");
        v2.put("storeId", null);
        v2.put("title", "Freeship 0 đồng");
        v2.put("subtitle", "Miễn phí giao hàng");
        v2.put("code", "FREESHIP0");
        v2.put("type", 2);
        v2.put("value", 15000.0);
        v2.put("pointsRequired", 300);
        v2.put("imageUrl", "https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?w=400&q=80");
        v2.put("remaining", 50);
        v2.put("isActive", true);
        v2.put("validityDays", 30);
        v2.put("terms", "Áp dụng cho đơn từ 50K trở lên.");
        v2.put("minOrderValue", 50000.0);
        v2.put("limitCount", 100);
        v2.put("usedCount", 0);
        v2.put("expiryDate", Timestamp.ofTimeSecondsAndNanos(Instant.now().plus(30, ChronoUnit.DAYS).getEpochSecond(), 0));
        v2.put("isFreeship", true);
        v2.put("createdAt", FieldValue.serverTimestamp());
        v2.put("updatedAt", FieldValue.serverTimestamp());
        vouchers.add(v2);

        Map<String, Object> v3 = new HashMap<>();
        v3.put("id", "voucher_003");
        v3.put("storeId", null);
        v3.put("title", "Giảm 10% cho đơn từ 200K");
        v3.put("subtitle", "Khuyến mãi đặc biệt cuối tuần");
        v3.put("code", "SAVE10P");
        v3.put("type", 1);
        v3.put("value", 10.0);
        v3.put("pointsRequired", 500);
        v3.put("imageUrl", "https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?w=400&q=80");
        v3.put("remaining", 30);
        v3.put("isActive", true);
        v3.put("validityDays", 30);
        v3.put("terms", "Giảm tối đa 50K. Áp dụng cuối tuần.");
        v3.put("minOrderValue", 200000.0);
        v3.put("limitCount", 100);
        v3.put("usedCount", 0);
        v3.put("expiryDate", Timestamp.ofTimeSecondsAndNanos(Instant.now().plus(30, ChronoUnit.DAYS).getEpochSecond(), 0));
        v3.put("isFreeship", false);
        v3.put("createdAt", FieldValue.serverTimestamp());
        v3.put("updatedAt", FieldValue.serverTimestamp());
        vouchers.add(v3);

        Map<String, Object> sv1 = new HashMap<>();
        sv1.put("id", "sys_voucher_001");
        sv1.put("storeId", null);
        sv1.put("title", "Giảm 20K cho đơn từ 100K");
        sv1.put("subtitle", "Dành cho khách hàng mới");
        sv1.put("type", 2);
        sv1.put("value", 20000.0);
        sv1.put("pointsRequired", 200);
        sv1.put("imageUrl", "https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?w=400&q=80");
        sv1.put("remaining", 100);
        sv1.put("isActive", true);
        sv1.put("validityDays", 30);
        sv1.put("terms", "Áp dụng cho tất cả quán ăn.");
        sv1.put("minOrderValue", 100000.0);
        sv1.put("limitCount", 200);
        sv1.put("usedCount", 0);
        sv1.put("expiryDate", Timestamp.ofTimeSecondsAndNanos(Instant.now().plus(30, ChronoUnit.DAYS).getEpochSecond(), 0));
        sv1.put("isFreeship", false);
        sv1.put("createdAt", FieldValue.serverTimestamp());
        sv1.put("updatedAt", FieldValue.serverTimestamp());
        vouchers.add(sv1);

        Map<String, Object> sv2 = new HashMap<>();
        sv2.put("id", "sys_voucher_002");
        sv2.put("storeId", null);
        sv2.put("title", "Giảm 15% cho đơn từ 150K");
        sv2.put("subtitle", "Khuyến mãi hệ thống");
        sv2.put("type", 1);
        sv2.put("value", 15.0);
        sv2.put("pointsRequired", 400);
        sv2.put("imageUrl", "https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?w=400&q=80");
        sv2.put("remaining", 75);
        sv2.put("isActive", true);
        sv2.put("validityDays", 30);
        sv2.put("terms", "Giảm tối đa 40K. Áp dụng toàn hệ thống.");
        sv2.put("minOrderValue", 150000.0);
        sv2.put("limitCount", 150);
        sv2.put("usedCount", 0);
        sv2.put("expiryDate", Timestamp.ofTimeSecondsAndNanos(Instant.now().plus(30, ChronoUnit.DAYS).getEpochSecond(), 0));
        sv2.put("isFreeship", false);
        sv2.put("createdAt", FieldValue.serverTimestamp());
        sv2.put("updatedAt", FieldValue.serverTimestamp());
        vouchers.add(sv2);

        Map<String, Object> sv3 = new HashMap<>();
        sv3.put("id", "sys_voucher_003");
        sv3.put("storeId", null);
        sv3.put("title", "Freeship 15K cho đơn từ 80K");
        sv3.put("subtitle", "Ưu đãi thành viên mới");
        sv3.put("type", 2);
        sv3.put("value", 15000.0);
        sv3.put("pointsRequired", 0);
        sv3.put("imageUrl", "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=400&q=80");
        sv3.put("remaining", 200);
        sv3.put("isActive", true);
        sv3.put("validityDays", 30);
        sv3.put("terms", "Áp dụng cho thành viên mới, đơn từ 80K.");
        sv3.put("minOrderValue", 80000.0);
        sv3.put("limitCount", 500);
        sv3.put("usedCount", 0);
        sv3.put("expiryDate", Timestamp.ofTimeSecondsAndNanos(Instant.now().plus(30, ChronoUnit.DAYS).getEpochSecond(), 0));
        sv3.put("isFreeship", true);
        sv3.put("createdAt", FieldValue.serverTimestamp());
        sv3.put("updatedAt", FieldValue.serverTimestamp());
        vouchers.add(sv3);

        Map<String, Object> sv4 = new HashMap<>();
        sv4.put("id", "sys_voucher_004");
        sv4.put("storeId", null);
        sv4.put("title", "Giảm 5% cho đơn từ 50K");
        sv4.put("subtitle", "Quà tặng mỗi ngày");
        sv4.put("type", 1);
        sv4.put("value", 5.0);
        sv4.put("pointsRequired", 0);
        sv4.put("imageUrl", "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&q=80");
        sv4.put("remaining", 300);
        sv4.put("isActive", true);
        sv4.put("validityDays", 7);
        sv4.put("terms", "Giảm tối đa 20K. Áp dụng mỗi ngày.");
        sv4.put("minOrderValue", 50000.0);
        sv4.put("limitCount", 1000);
        sv4.put("usedCount", 0);
        sv4.put("expiryDate", Timestamp.ofTimeSecondsAndNanos(Instant.now().plus(30, ChronoUnit.DAYS).getEpochSecond(), 0));
        sv4.put("isFreeship", false);
        sv4.put("createdAt", FieldValue.serverTimestamp());
        sv4.put("updatedAt", FieldValue.serverTimestamp());
        vouchers.add(sv4);

        Map<String, Object> sv5 = new HashMap<>();
        sv5.put("id", "sys_voucher_005");
        sv5.put("storeId", null);
        sv5.put("title", "Giảm 25K cho đơn từ 200K");
        sv5.put("subtitle", "Khuyến mãi đặc biệt");
        sv5.put("type", 2);
        sv5.put("value", 25000.0);
        sv5.put("pointsRequired", 0);
        sv5.put("imageUrl", "https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?w=400&q=80");
        sv5.put("remaining", 150);
        sv5.put("isActive", true);
        sv5.put("validityDays", 14);
        sv5.put("terms", "Áp dụng cho đơn từ 200K. Không áp dụng đồng thời.");
        sv5.put("minOrderValue", 200000.0);
        sv5.put("limitCount", 300);
        sv5.put("usedCount", 0);
        sv5.put("expiryDate", Timestamp.ofTimeSecondsAndNanos(Instant.now().plus(30, ChronoUnit.DAYS).getEpochSecond(), 0));
        sv5.put("isFreeship", false);
        sv5.put("createdAt", FieldValue.serverTimestamp());
        sv5.put("updatedAt", FieldValue.serverTimestamp());
        vouchers.add(sv5);

        Map<String, Object> v4 = new HashMap<>();
        v4.put("id", "voucher_004");
        v4.put("storeId", "store_005");
        v4.put("title", "Giảm 30K Bún Chả");
        v4.put("subtitle", "Khuyến mãi mừng khai trương");
        v4.put("code", "BUNCHAMOI");
        v4.put("type", 2);
        v4.put("value", 30000.0);
        v4.put("pointsRequired", 0);
        v4.put("imageUrl", "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=400&q=80");
        v4.put("remaining", 50);
        v4.put("isActive", true);
        v4.put("validityDays", 30);
        v4.put("terms", "Áp dụng cho đơn từ 150K tại Quán Bún Chả.");
        v4.put("minOrderValue", 150000.0);
        v4.put("limitCount", 100);
        v4.put("usedCount", 0);
        v4.put("expiryDate", Timestamp.ofTimeSecondsAndNanos(Instant.now().plus(30, ChronoUnit.DAYS).getEpochSecond(), 0));
        v4.put("isFreeship", false);
        v4.put("createdAt", FieldValue.serverTimestamp());
        v4.put("updatedAt", FieldValue.serverTimestamp());
        vouchers.add(v4);

        kiemTraVaSeed(collectionName, vouchers);
    }

    private void seedReviews() {
        String collectionName = "reviews";
        List<Map<String, Object>> reviews = new ArrayList<>();

        Map<String, Object> rev1 = new HashMap<>();
        rev1.put("id", "rev_001");
        rev1.put("orderId", "order_001");
        rev1.put("itemId", "item_001");
        rev1.put("foodId", "prod_001");
        rev1.put("storeId", "store_001");
        rev1.put("userId", "user_001");
        rev1.put("userName", "Khôi");
        rev1.put("userAvatarUrl", "https://example.com/avatar/user001.jpg");
        rev1.put("starRating", 5);
        rev1.put("comment", "Đồ ăn rất ngon, giao hàng nhanh, đóng gói kỹ lưỡng.");
        rev1.put("imageUrls", List.of(
                "https://example.com/review/rev001_1.jpg",
                "https://example.com/review/rev001_2.jpg"
        ));
        rev1.put("replyComment", null);
        rev1.put("repliedAt", null);
        rev1.put("createdAt", FieldValue.serverTimestamp());
        rev1.put("updatedAt", FieldValue.serverTimestamp());
        reviews.add(rev1);

        Map<String, Object> rev2 = new HashMap<>();
        rev2.put("id", "rev_002");
        rev2.put("orderId", "order_002");
        rev2.put("itemId", "item_002");
        rev2.put("foodId", "prod_001");
        rev2.put("storeId", "store_001");
        rev2.put("userId", "user_002");
        rev2.put("userName", "Quản Trị Viên");
        rev2.put("userAvatarUrl", "https://example.com/avatar/admin.jpg");
        rev2.put("starRating", 4);
        rev2.put("comment", "Món ăn ngon, nhưng giao hàng trễ hơn 15 phút.");
        rev2.put("imageUrls", List.of());
        rev2.put("replyComment", null);
        rev2.put("repliedAt", null);
        rev2.put("createdAt", FieldValue.serverTimestamp());
        rev2.put("updatedAt", FieldValue.serverTimestamp());
        reviews.add(rev2);

        Map<String, Object> rev3 = new HashMap<>();
        rev3.put("id", "rev_003");
        rev3.put("orderId", "order_002");
        rev3.put("itemId", "item_003");
        rev3.put("foodId", "prod_004");
        rev3.put("storeId", "store_002");
        rev3.put("userId", "user_001");
        rev3.put("userName", "Khôi");
        rev3.put("userAvatarUrl", "https://example.com/avatar/user001.jpg");
        rev3.put("starRating", 5);
        rev3.put("comment", "Trà sữa rất ngon, topping nhiều, uống lạnh.");
        rev3.put("imageUrls", List.of(
                "https://example.com/review/rev003_1.jpg"
        ));
        rev3.put("replyComment", null);
        rev3.put("repliedAt", null);
        rev3.put("createdAt", FieldValue.serverTimestamp());
        rev3.put("updatedAt", FieldValue.serverTimestamp());
        reviews.add(rev3);

        Map<String, Object> rev4 = new HashMap<>();
        rev4.put("id", "rev_004");
        rev4.put("orderId", "order_003");
        rev4.put("itemId", "item_004");
        rev4.put("foodId", "prod_006");
        rev4.put("storeId", "store_003");
        rev4.put("userId", "user_002");
        rev4.put("userName", "Quản Trị Viên");
        rev4.put("userAvatarUrl", "https://example.com/avatar/admin.jpg");
        rev4.put("starRating", 4);
        rev4.put("comment", "Gà rán giòn, ăn biếu như hâm thịt ngọt.");
        rev4.put("imageUrls", List.of());
        rev4.put("replyComment", null);
        rev4.put("repliedAt", null);
        rev4.put("createdAt", FieldValue.serverTimestamp());
        rev4.put("updatedAt", FieldValue.serverTimestamp());
        reviews.add(rev4);

        Map<String, Object> rev5 = new HashMap<>();
        rev5.put("id", "rev_005");
        rev5.put("orderId", "order_004");
        rev5.put("itemId", "item_005");
        rev5.put("foodId", "prod_010");
        rev5.put("storeId", "store_004");
        rev5.put("userId", "user_001");
        rev5.put("userName", "Khôi");
        rev5.put("userAvatarUrl", "https://example.com/avatar/user001.jpg");
        rev5.put("starRating", 5);
        rev5.put("comment", "Bún bò Huế ngon chuẩn, nước dùng ngọt thanh.");
        rev5.put("imageUrls", List.of(
                "https://example.com/review/rev005_1.jpg",
                "https://example.com/review/rev005_2.jpg"
        ));
        rev5.put("replyComment", null);
        rev5.put("repliedAt", null);
        rev5.put("createdAt", FieldValue.serverTimestamp());
        rev5.put("updatedAt", FieldValue.serverTimestamp());
        reviews.add(rev5);

        Map<String, Object> rev6 = new HashMap<>();
        rev6.put("id", "rev_006");
        rev6.put("orderId", "order_001");
        rev6.put("itemId", "item_006");
        rev6.put("foodId", "prod_002");
        rev6.put("storeId", "store_001");
        rev6.put("userId", "user_003");
        rev6.put("userName", "Lê Văn B");
        rev6.put("userAvatarUrl", "https://example.com/avatar/driver001.jpg");
        rev6.put("starRating", 4);
        rev6.put("comment", "Cơm tấm ngon, phần ăn vừa đủ.");
        rev6.put("imageUrls", List.of());
        rev6.put("replyComment", null);
        rev6.put("repliedAt", null);
        rev6.put("createdAt", FieldValue.serverTimestamp());
        rev6.put("updatedAt", FieldValue.serverTimestamp());
        reviews.add(rev6);

        Map<String, Object> rev7 = new HashMap<>();
        rev7.put("id", "rev_007");
        rev7.put("orderId", "order_002");
        rev7.put("itemId", "item_007");
        rev7.put("foodId", "prod_005");
        rev7.put("storeId", "store_002");
        rev7.put("userId", "user_003");
        rev7.put("userName", "Lê Văn B");
        rev7.put("userAvatarUrl", "https://example.com/avatar/driver001.jpg");
        rev7.put("starRating", 5);
        rev7.put("comment", "Quán này bán trà sữa ngon lắm, giao hàng cũng nhanh.");
        rev7.put("imageUrls", List.of(
                "https://example.com/review/rev007_1.jpg"
        ));
        rev7.put("replyComment", null);
        rev7.put("repliedAt", null);
        rev7.put("createdAt", FieldValue.serverTimestamp());
        rev7.put("updatedAt", FieldValue.serverTimestamp());
        reviews.add(rev7);

        Map<String, Object> rev8 = new HashMap<>();
        rev8.put("id", "rev_008");
        rev8.put("orderId", "order_007");
        rev8.put("itemId", "item_008");
        rev8.put("foodId", "prod_006");
        rev8.put("storeId", "store_003");
        rev8.put("userId", "user_001");
        rev8.put("userName", "Khôi");
        rev8.put("userAvatarUrl", "https://example.com/avatar/user001.jpg");
        rev8.put("starRating", 3);
        rev8.put("comment", "Ăn cũng được nhưng giá cả hơi cao.");
        rev8.put("imageUrls", List.of());
        rev8.put("replyComment", null);
        rev8.put("repliedAt", null);
        rev8.put("createdAt", FieldValue.serverTimestamp());
        rev8.put("updatedAt", FieldValue.serverTimestamp());
        reviews.add(rev8);

        Map<String, Object> rev9 = new HashMap<>();
        rev9.put("id", "rev_009");
        rev9.put("orderId", "order_008");
        rev9.put("itemId", "item_009");
        rev9.put("foodId", "prod_012");
        rev9.put("storeId", "store_005");
        rev9.put("userId", "user_001");
        rev9.put("userName", "Khôi");
        rev9.put("userAvatarUrl", "https://example.com/avatar/user001.jpg");
        rev9.put("starRating", 5);
        rev9.put("comment", "Bún chả siêu ngon, thịt nướng thơm, nước chấm đậm đà!");
        rev9.put("imageUrls", List.of());
        rev9.put("replyComment", null);
        rev9.put("repliedAt", null);
        rev9.put("createdAt", FieldValue.serverTimestamp());
        rev9.put("updatedAt", FieldValue.serverTimestamp());
        reviews.add(rev9);

        kiemTraVaSeed(collectionName, reviews);
    }

    private void seedOrders() {
        String collectionName = "orders";
        List<Map<String, Object>> orders = new ArrayList<>();

        List<Map<String, Object>> order1Items = new ArrayList<>();
        Map<String, Object> order1Item1 = new HashMap<>();
        order1Item1.put("foodId", "prod_001");
        order1Item1.put("name", "Cơm tấm sườn bì chả");
        order1Item1.put("price", 45000.0);
        order1Item1.put("quantity", 2);
        order1Item1.put("imageUrl", "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=400&q=80");
        order1Items.add(order1Item1);
        Map<String, Object> order1 = new HashMap<>();
        order1.put("id", "order_001");
        order1.put("userId", "user_001");
        order1.put("storeId", "store_001");
        order1.put("storeName", "Cơm Tấm Phúc Lộc Thọ");
        order1.put("code", "FG001");
        order1.put("addressId", "addr_001");
        order1.put("items", order1Items);
        order1.put("totalAmount", 140000.0);
        order1.put("deliveryFee", 15000.0);
        order1.put("discountAmount", 0.0);
        order1.put("shopDiscountAmount", 0.0);
        order1.put("freeshipDiscountAmount", 0.0);
        order1.put("finalAmount", 155000.0);
        order1.put("status", 2);
        order1.put("deliveryAddress", "Ký túc xá UTC2, Quận 9, TP.HCM");
        order1.put("receiverName", "Khôi");
        order1.put("receiverPhone", "0123456789");
        order1.put("paymentMethod", 1);
        order1.put("driverName", "Le Van B");
        order1.put("driverPhone", "0912345678");
        order1.put("createdAt", FieldValue.serverTimestamp());
        order1.put("updatedAt", FieldValue.serverTimestamp());
        orders.add(order1);

        List<Map<String, Object>> order2Items = new ArrayList<>();
        Map<String, Object> order2Item1 = new HashMap<>();
        order2Item1.put("foodId", "prod_004");
        order2Item1.put("name", "Trà sữa trà chanh");
        order2Item1.put("price", 34000.0);
        order2Item1.put("quantity", 2);
        order2Item1.put("imageUrl", "https://images.unsplash.com/photo-1558857563-b371033873b8?w=400&q=80");
        List<Map<String, Object>> order2Item1Options = new ArrayList<>();
        Map<String, Object> order2Item1Opt = new HashMap<>();
        order2Item1Opt.put("name", "Tran chau");
        order2Item1Opt.put("price", 5000.0);
        order2Item1Options.add(order2Item1Opt);
        order2Item1.put("options", order2Item1Options);
        order2Items.add(order2Item1);
        Map<String, Object> order2 = new HashMap<>();
        order2.put("id", "order_002");
        order2.put("userId", "user_001");
        order2.put("storeId", "store_002");
        order2.put("storeName", "Trà Sữa Tocotoco");
        order2.put("code", "FG002");
        order2.put("addressId", "addr_001");
        order2.put("items", order2Items);
        order2.put("totalAmount", 79000.0);
        order2.put("deliveryFee", 12000.0);
        order2.put("discountAmount", 0.0);
        order2.put("shopDiscountAmount", 0.0);
        order2.put("freeshipDiscountAmount", 0.0);
        order2.put("finalAmount", 91000.0);
        order2.put("status", 3);
        order2.put("deliveryAddress", "Ký túc xá UTC2, Quận 9, TP.HCM");
        order2.put("receiverName", "Khôi");
        order2.put("receiverPhone", "0123456789");
        order2.put("paymentMethod", 2);
        order2.put("driverName", "Le Van B");
        order2.put("driverPhone", "0912345678");
        order2.put("createdAt", FieldValue.serverTimestamp());
        order2.put("updatedAt", FieldValue.serverTimestamp());
        orders.add(order2);

        List<Map<String, Object>> order3Items = new ArrayList<>();
        Map<String, Object> order3Item1 = new HashMap<>();
        order3Item1.put("foodId", "prod_007");
        order3Item1.put("name", "Ga lap xuong");
        order3Item1.put("price", 55000.0);
        order3Item1.put("quantity", 1);
        order3Item1.put("imageUrl", "https://images.unsplash.com/photo-1626645738196-c2a7c87a8f58?w=400&q=80");
        order3Items.add(order3Item1);
        Map<String, Object> order3Item2 = new HashMap<>();
        order3Item2.put("foodId", "prod_009");
        order3Item2.put("name", "Khoai tây chiên");
        order3Item2.put("price", 20000.0);
        order3Item2.put("quantity", 1);
        order3Item2.put("imageUrl", "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&q=80");
        order3Items.add(order3Item2);
        Map<String, Object> order3 = new HashMap<>();
        order3.put("id", "order_003");
        order3.put("userId", "user_002");
        order3.put("storeId", "store_003");
        order3.put("storeName", "Gà Rán KFC Nguyễn Cửu");
        order3.put("code", "FG003");
        order3.put("addressId", null);
        order3.put("items", order3Items);
        order3.put("totalAmount", 93000.0);
        order3.put("deliveryFee", 18000.0);
        order3.put("discountAmount", 0.0);
        order3.put("shopDiscountAmount", 0.0);
        order3.put("freeshipDiscountAmount", 0.0);
        order3.put("finalAmount", 111000.0);
        order3.put("status", 1);
        order3.put("deliveryAddress", "123 Lê Văn Việt, TP. Thủ Đức");
        order3.put("receiverName", "Quản Trị Viên");
        order3.put("receiverPhone", "0987654321");
        order3.put("paymentMethod", 1);
        order3.put("driverName", null);
        order3.put("driverPhone", null);
        order3.put("createdAt", FieldValue.serverTimestamp());
        order3.put("updatedAt", FieldValue.serverTimestamp());
        orders.add(order3);

        List<Map<String, Object>> order4Items = new ArrayList<>();
        Map<String, Object> order4Item1 = new HashMap<>();
        order4Item1.put("foodId", "prod_010");
        order4Item1.put("name", "Bún bò Huế");
        order4Item1.put("price", 45000.0);
        order4Item1.put("quantity", 1);
        order4Item1.put("imageUrl", "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=400&q=80");
        order4Items.add(order4Item1);
        Map<String, Object> order4 = new HashMap<>();
        order4.put("id", "order_004");
        order4.put("userId", "user_002");
        order4.put("storeId", "store_004");
        order4.put("storeName", "Bún Bò Huế Ba Lẻ");
        order4.put("code", "FG004");
        order4.put("addressId", null);
        order4.put("items", order4Items);
        order4.put("totalAmount", 65000.0);
        order4.put("deliveryFee", 20000.0);
        order4.put("discountAmount", 0.0);
        order4.put("shopDiscountAmount", 0.0);
        order4.put("freeshipDiscountAmount", 0.0);
        order4.put("finalAmount", 85000.0);
        order4.put("status", 0);
        order4.put("deliveryAddress", "456 Nguyễn Thi Định, TP. Thủ Đức");
        order4.put("receiverName", "Quản Trị Viên");
        order4.put("receiverPhone", "0987654321");
        order4.put("paymentMethod", 2);
        order4.put("driverName", null);
        order4.put("driverPhone", null);
        order4.put("createdAt", FieldValue.serverTimestamp());
        order4.put("updatedAt", FieldValue.serverTimestamp());
        orders.add(order4);

        List<Map<String, Object>> order5Items = new ArrayList<>();
        Map<String, Object> order5Item1 = new HashMap<>();
        order5Item1.put("foodId", "prod_002");
        order5Item1.put("name", "Cơm tấm gà xối mỡ");
        order5Item1.put("price", 50000.0);
        order5Item1.put("quantity", 1);
        order5Item1.put("imageUrl", "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=400&q=80");
        order5Items.add(order5Item1);
        Map<String, Object> order5 = new HashMap<>();
        order5.put("id", "order_005");
        order5.put("userId", "user_001");
        order5.put("storeId", "store_001");
        order5.put("storeName", "Cơm Tấm Phúc Lộc Thọ");
        order5.put("code", "FG005");
        order5.put("addressId", "addr_001");
        order5.put("items", order5Items);
        order5.put("totalAmount", 65000.0);
        order5.put("deliveryFee", 15000.0);
        order5.put("discountAmount", 0.0);
        order5.put("shopDiscountAmount", 0.0);
        order5.put("freeshipDiscountAmount", 0.0);
        order5.put("finalAmount", 80000.0);
        order5.put("status", 4);
        order5.put("deliveryAddress", "Ký túc xá UTC2, Quận 9, TP.HCM");
        order5.put("receiverName", "Khôi");
        order5.put("receiverPhone", "0123456789");
        order5.put("paymentMethod", 3);
        order5.put("driverName", null);
        order5.put("driverPhone", null);
        order5.put("createdAt", FieldValue.serverTimestamp());
        order5.put("updatedAt", FieldValue.serverTimestamp());
        orders.add(order5);

        List<Map<String, Object>> order6Items = new ArrayList<>();
        Map<String, Object> order6Item1 = new HashMap<>();
        order6Item1.put("foodId", "prod_005");
        order6Item1.put("name", "Tra dao cam");
        order6Item1.put("price", 25000.0);
        order6Item1.put("quantity", 2);
        order6Item1.put("imageUrl", "https://images.unsplash.com/photo-1550508776-b6a354f5f66d?w=400&q=80");
        order6Items.add(order6Item1);
        Map<String, Object> order6Item2 = new HashMap<>();
        order6Item2.put("foodId", "prod_014");
        order6Item2.put("name", "Trà sữa trái cây");
        order6Item2.put("price", 32000.0);
        order6Item2.put("quantity", 1);
        order6Item2.put("imageUrl", "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&q=80");
        order6Items.add(order6Item2);
        Map<String, Object> order6 = new HashMap<>();
        order6.put("id", "order_006");
        order6.put("userId", "user_003");
        order6.put("storeId", "store_002");
        order6.put("storeName", "Trà Sữa Tocotoco");
        order6.put("code", "FG006");
        order6.put("addressId", null);
        order6.put("items", order6Items);
        order6.put("totalAmount", 87000.0);
        order6.put("deliveryFee", 12000.0);
        order6.put("discountAmount", 0.0);
        order6.put("shopDiscountAmount", 0.0);
        order6.put("freeshipDiscountAmount", 0.0);
        order6.put("finalAmount", 99000.0);
        order6.put("status", 2);
        order6.put("deliveryAddress", "101 Phố Huế, Q.1, TP.HCM");
        order6.put("receiverName", "Lê Văn B");
        order6.put("receiverPhone", "0912345678");
        order6.put("paymentMethod", 4);
        order6.put("driverName", "Le Van B");
        order6.put("driverPhone", "0912345678");
        order6.put("createdAt", FieldValue.serverTimestamp());
        order6.put("updatedAt", FieldValue.serverTimestamp());
        orders.add(order6);

        List<Map<String, Object>> order7Items = new ArrayList<>();
        Map<String, Object> order7Item1 = new HashMap<>();
        order7Item1.put("foodId", "prod_008");
        order7Item1.put("name", "Mì gà chua cay");
        order7Item1.put("price", 35000.0);
        order7Item1.put("quantity", 2);
        order7Item1.put("imageUrl", "https://images.unsplash.com/photo-1573080496219-bb080dd4f877?w=400&q=80");
        order7Items.add(order7Item1);
        Map<String, Object> order7 = new HashMap<>();
        order7.put("id", "order_007");
        order7.put("userId", "user_002");
        order7.put("storeId", "store_003");
        order7.put("storeName", "Gà Rán KFC Nguyễn Cửu");
        order7.put("code", "FG007");
        order7.put("addressId", null);
        order7.put("items", order7Items);
        order7.put("totalAmount", 88000.0);
        order7.put("deliveryFee", 18000.0);
        order7.put("discountAmount", 0.0);
        order7.put("shopDiscountAmount", 0.0);
        order7.put("freeshipDiscountAmount", 0.0);
        order7.put("finalAmount", 106000.0);
        order7.put("status", 3);
        order7.put("deliveryAddress", "789 Nguyễn Cửu, TP. Thủ Đức");
        order7.put("receiverName", "Quản Trị Viên");
        order7.put("receiverPhone", "0987654321");
        order7.put("paymentMethod", 1);
        order7.put("driverName", "Le Van B");
        order7.put("driverPhone", "0912345678");
        order7.put("createdAt", FieldValue.serverTimestamp());
        order7.put("updatedAt", FieldValue.serverTimestamp());
        orders.add(order7);

        List<Map<String, Object>> order8Items = new ArrayList<>();
        Map<String, Object> order8Item1 = new HashMap<>();
        order8Item1.put("foodId", "prod_016");
        order8Item1.put("name", "Bún chả Hà Nội");
        order8Item1.put("price", 65000.0);
        order8Item1.put("quantity", 2);
        order8Item1.put("imageUrl", "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=400&q=80");
        List<Map<String, Object>> order8Item1Options = new ArrayList<>();
        Map<String, Object> order8Item1Opt = new HashMap<>();
        order8Item1Opt.put("name", "Phần đặc biệt");
        order8Item1Opt.put("price", 15000.0);
        order8Item1Options.add(order8Item1Opt);
        order8Item1.put("options", order8Item1Options);
        order8Items.add(order8Item1);

        Map<String, Object> order8 = new HashMap<>();
        order8.put("id", "order_008");
        order8.put("userId", "user_001");
        order8.put("storeId", "store_005");
        order8.put("storeName", "Quán Bún Chả");
        order8.put("code", "FG008");
        order8.put("addressId", "addr_001");
        order8.put("items", order8Items);
        order8.put("totalAmount", 150000.0);
        order8.put("deliveryFee", 20000.0);
        order8.put("discountAmount", 0.0);
        order8.put("shopDiscountAmount", 0.0);
        order8.put("freeshipDiscountAmount", 0.0);
        order8.put("finalAmount", 170000.0);
        order8.put("status", 3);
        order8.put("deliveryAddress", "123 Lê Văn Việt, TP Thủ Đức");
        order8.put("receiverName", "Khôi");
        order8.put("receiverPhone", "0123456789");
        order8.put("paymentMethod", 2);
        order8.put("driverName", "Lê Văn B");
        order8.put("driverPhone", "0912345678");
        order8.put("createdAt", FieldValue.serverTimestamp());
        order8.put("updatedAt", FieldValue.serverTimestamp());
        orders.add(order8);

        kiemTraVaSeed(collectionName, orders);
    }

    private void seedCustomerProfiles() {
        String collectionName = "customer_profiles";
        List<Map<String, Object>> profiles = Arrays.asList(
                Map.ofEntries(
                        Map.entry("id", "user_001"),
                        Map.entry("loyaltyPoints", 1500),
                        Map.entry("membershipTier", 1),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "user_002"),
                        Map.entry("loyaltyPoints", 300),
                        Map.entry("membershipTier", 0),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "user_003"),
                        Map.entry("loyaltyPoints", 800),
                        Map.entry("membershipTier", 0),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                )
        );
        kiemTraVaSeed(collectionName, profiles);

        seedCustomerAddresses();
        seedCustomerPaymentMethods();
        seedCustomerNotifications();
        seedCustomerCart();
        seedCustomerMyVouchers();
    }

    private void seedCustomerAddresses() {
        String userId = "user_001";
        String collectionName = "customer_profiles/" + userId + "/addresses";
        List<Map<String, Object>> addresses = new ArrayList<>();

        Map<String, Object> addr1 = new HashMap<>();
        addr1.put("id", "addr_001");
        addr1.put("name", "Nhà riêng");
        addr1.put("address", "Ký túc xá UTC2, Quận 9, TP.HCM");
        addr1.put("receiverName", "Khôi");
        addr1.put("receiverPhone", "0123456789");
        addr1.put("lat", 10.8455);
        addr1.put("lng", 106.7939);
        addr1.put("isDefault", true);
        addr1.put("deletedAt", null);
        addr1.put("createdAt", FieldValue.serverTimestamp());
        addr1.put("updatedAt", FieldValue.serverTimestamp());
        addresses.add(addr1);

        Map<String, Object> addr2 = new HashMap<>();
        addr2.put("id", "addr_002");
        addr2.put("name", "Trường học");
        addr2.put("address", "Trường Đại học Giao thông Vận tải, TP. Thủ Đức");
        addr2.put("receiverName", "Khôi");
        addr2.put("receiverPhone", "0123456789");
        addr2.put("lat", 10.8490);
        addr2.put("lng", 106.7890);
        addr2.put("isDefault", false);
        addr2.put("deletedAt", null);
        addr2.put("createdAt", FieldValue.serverTimestamp());
        addr2.put("updatedAt", FieldValue.serverTimestamp());
        addresses.add(addr2);

        kiemTraVaSeed(collectionName, addresses);
    }

    private void seedCustomerPaymentMethods() {
        String userId = "user_001";
        String collectionName = "customer_profiles/" + userId + "/payment_methods";
        List<Map<String, Object>> methods = new ArrayList<>();

        Map<String, Object> pm1 = new HashMap<>();
        pm1.put("id", "pm_001");
        pm1.put("name", "Ví MoMo");
        pm1.put("type", 2);
        pm1.put("details", "Thanh toán qua ví MoMo");
        pm1.put("isDefault", true);
        pm1.put("cardBrand", null);
        pm1.put("last4Digits", null);
        pm1.put("walletBrand", "momo");
        pm1.put("isLinked", true);
        pm1.put("createdAt", FieldValue.serverTimestamp());
        pm1.put("updatedAt", FieldValue.serverTimestamp());
        methods.add(pm1);

        Map<String, Object> pm2 = new HashMap<>();
        pm2.put("id", "pm_002");
        pm2.put("name", "Thẻ Visa");
        pm2.put("type", 3);
        pm2.put("details", "Thanh toán qua thẻ Visa");
        pm2.put("isDefault", false);
        pm2.put("cardBrand", "Visa");
        pm2.put("last4Digits", "1234");
        pm2.put("walletBrand", null);
        pm2.put("isLinked", true);
        pm2.put("createdAt", FieldValue.serverTimestamp());
        pm2.put("updatedAt", FieldValue.serverTimestamp());
        methods.add(pm2);

        kiemTraVaSeed(collectionName, methods);
    }

    private void seedCustomerNotifications() {
        String userId = "user_001";
        String collectionName = "customer_profiles/" + userId + "/notifications";
        List<Map<String, Object>> notifications = new ArrayList<>();

        Map<String, Object> notif1 = new HashMap<>();
        notif1.put("id", "notif_001");
        notif1.put("type", 2);
        notif1.put("title", "Đơn hàng đã được giao thành công");
        notif1.put("body", "Don hang order_001 da duoc giao");
        notif1.put("referenceId", "order_001");
        notif1.put("isRead", false);
        notif1.put("createdAt", FieldValue.serverTimestamp());
        notifications.add(notif1);

        Map<String, Object> notif2 = new HashMap<>();
        notif2.put("id", "notif_002");
        notif2.put("type", 1);
        notif2.put("title", "Khuyến mãi đặc biệt");
        notif2.put("body", "Giam 20% cho don hang dau tien");
        notif2.put("referenceId", "voucher_001");
        notif2.put("isRead", true);
        notif2.put("createdAt", FieldValue.serverTimestamp());
        notifications.add(notif2);

        Map<String, Object> notif3 = new HashMap<>();
        notif3.put("id", "notif_003");
        notif3.put("type", 0);
        notif3.put("title", "Chào mừng đến với FoodGo");
        notif3.put("body", "Cảm ơn bạn đã đăng ký tài khoản tại FoodGo");
        notif3.put("referenceId", null);
        notif3.put("isRead", true);
        notif3.put("createdAt", FieldValue.serverTimestamp());
        notifications.add(notif3);

        kiemTraVaSeed(collectionName, notifications);
    }

    private void seedCustomerCart() {
        String userId = "user_001";
        String collectionName = "customer_profiles/" + userId + "/cart";
        List<Map<String, Object>> cartItems = new ArrayList<>();

        Map<String, Object> ci1 = new HashMap<>();
        ci1.put("id", "cart_item_001");
        ci1.put("userId", "user_001");
        ci1.put("storeId", "store_001");
        ci1.put("foodId", "prod_001");
        ci1.put("name", "Cơm tấm sườn bì chả");
        ci1.put("price", 45000.0);
        ci1.put("quantity", 2);
        ci1.put("size", null);
        ci1.put("sizePrice", 0.0);
        ci1.put("toppings", List.of());
        ci1.put("note", null);
        ci1.put("imageUrl", "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=400&q=80");
        ci1.put("createdAt", FieldValue.serverTimestamp());
        ci1.put("updatedAt", FieldValue.serverTimestamp());
        cartItems.add(ci1);

        Map<String, Object> ci2 = new HashMap<>();
        ci2.put("id", "cart_item_002");
        ci2.put("userId", "user_001");
        ci2.put("storeId", "store_002");
        ci2.put("foodId", "prod_004");
        ci2.put("name", "Trà sữa trà chanh");
        ci2.put("price", 44000.0);
        ci2.put("quantity", 1);
        ci2.put("size", "L");
        ci2.put("sizePrice", 5000.0);
        ci2.put("toppings", List.of(
                Map.of("name", "Trân châu trắng", "price", 10000.0),
                Map.of("name", "Thạch trái cây", "price", 8000.0)
        ));
        ci2.put("note", "Ít đường");
        ci2.put("imageUrl", "https://images.unsplash.com/photo-1558857563-b371033873b8?w=400&q=80");
        ci2.put("createdAt", FieldValue.serverTimestamp());
        ci2.put("updatedAt", FieldValue.serverTimestamp());
        cartItems.add(ci2);

        kiemTraVaSeed(collectionName, cartItems);
    }

    private void seedCustomerMyVouchers() {
        String userId = "user_001";
        String collectionName = "customer_profiles/" + userId + "/my_vouchers";

        Map<String, Object> mv1 = new HashMap<>();
        mv1.put("id", "mv_001");
        mv1.put("title", "Giảm 20K phí giao hàng");
        mv1.put("subtitle", "Áp dụng cho đơn từ 100K");
        mv1.put("code", "FREESHIP20");
        mv1.put("description", "Áp dụng cho đơn từ 100K");
        mv1.put("expiryDate", Timestamp.ofTimeSecondsAndNanos(Instant.now().plus(30, ChronoUnit.DAYS).getEpochSecond(), 0));
        mv1.put("type", 2);
        mv1.put("value", 20000.0);
        mv1.put("minOrderValue", 50000.0);
        mv1.put("isActive", true);
        mv1.put("isFreeship", true);
        mv1.put("imageUrl", "https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?w=400&q=80");
        mv1.put("terms", "Áp dụng cho đơn từ 100K. Không áp dụng đồng thời với voucher khác.");
        mv1.put("createdAt", FieldValue.serverTimestamp());
        mv1.put("updatedAt", FieldValue.serverTimestamp());

        Map<String, Object> mv2 = new HashMap<>();
        mv2.put("id", "mv_002");
        mv2.put("title", "Giảm 10% cho đơn hàng");
        mv2.put("subtitle", "Giảm 10% cho mọi đơn hàng");
        mv2.put("code", "SAVE10");
        mv2.put("description", "Giảm 10% cho mọi đơn hàng");
        mv2.put("expiryDate", Timestamp.ofTimeSecondsAndNanos(Instant.now().plus(30, ChronoUnit.DAYS).getEpochSecond(), 0));
        mv2.put("type", 1);
        mv2.put("value", 10.0);
        mv2.put("minOrderValue", 50000.0);
        mv2.put("isActive", true);
        mv2.put("isFreeship", false);
        mv2.put("imageUrl", "https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?w=400&q=80");
        mv2.put("terms", "Giảm tối đa 30K. Áp dụng cho mọi đơn hàng.");
        mv2.put("createdAt", FieldValue.serverTimestamp());
        mv2.put("updatedAt", FieldValue.serverTimestamp());

        List<Map<String, Object>> myVouchers = Arrays.asList(mv1, mv2);
        kiemTraVaSeed(collectionName, myVouchers);
    }

    private void seedDriverProfiles() {
        String collectionName = "driver_profiles";
        List<Map<String, Object>> profiles = Arrays.asList(
                Map.ofEntries(
                        Map.entry("id", "user_001"),
                        Map.entry("vehiclePlate", "59A-123.45"),
                        Map.entry("vehicleType", "Honda Wave Alpha"),
                        Map.entry("driverLicense", "DL123456789"),
                        Map.entry("isActive", true),
                        Map.entry("rating", 4.9),
                        Map.entry("totalTrips", 150),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "user_003"),
                        Map.entry("vehiclePlate", "59B-678.90"),
                        Map.entry("vehicleType", "Yamaha Sirius"),
                        Map.entry("driverLicense", "DL987654321"),
                        Map.entry("isActive", true),
                        Map.entry("rating", 4.7),
                        Map.entry("totalTrips", 80),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "user_006"),
                        Map.entry("vehiclePlate", "60A-111.22"),
                        Map.entry("vehicleType", "Honda Vision"),
                        Map.entry("driverLicense", "DL456789123"),
                        Map.entry("isActive", true),
                        Map.entry("rating", 4.6),
                        Map.entry("totalTrips", 120),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "user_005"),
                        Map.entry("vehiclePlate", "61C-333.44"),
                        Map.entry("vehicleType", "Yamaha Nozza"),
                        Map.entry("driverLicense", "DL789123456"),
                        Map.entry("isActive", true),
                        Map.entry("rating", 4.8),
                        Map.entry("totalTrips", 200),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                )
        );
        kiemTraVaSeed(collectionName, profiles);

        seedDriverNotifications();
    }

    private void seedDriverNotifications() {
        String userId = "user_001";
        String collectionName = "driver_profiles/" + userId + "/notifications";
        List<Map<String, Object>> notifications = Arrays.asList(
                Map.ofEntries(
                        Map.entry("id", "dnotif_001"),
                        Map.entry("type", 11),
                        Map.entry("title", "Yêu cầu nhận đơn mới"),
                        Map.entry("body", "Ban co don hang moi cho nhan: order_001"),
                        Map.entry("referenceId", "order_001"),
                        Map.entry("isRead", false),
                        Map.entry("createdAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "dnotif_002"),
                        Map.entry("type", 12),
                        Map.entry("title", "Thông báo giao hàng"),
                        Map.entry("body", "Don hang order_001 da duoc giao thanh cong"),
                        Map.entry("referenceId", "order_001"),
                        Map.entry("isRead", true),
                        Map.entry("createdAt", FieldValue.serverTimestamp())
                )
        );
        kiemTraVaSeed(collectionName, notifications);
    }

    private void seedMerchantProfiles() {
        String collectionName = "merchant_profiles";
        List<Map<String, Object>> profiles = Arrays.asList(
                Map.ofEntries(
                        Map.entry("id", "user_001"),
                        Map.entry("businessName", "Com tam Phuc Loc Tho"),
                        Map.entry("businessLicense", "BL123456789"),
                        Map.entry("taxCode", "TAX123456789"),
                        Map.entry("storeIds", Arrays.asList("store_001")),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "user_004"),
                        Map.entry("businessName", "Quán bún chả "),
                        Map.entry("businessLicense", "BL987654321"),
                        Map.entry("taxCode", "TAX987654321"),
                        Map.entry("storeIds", Arrays.asList("store_005")),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                )
        );
        kiemTraVaSeed(collectionName, profiles);

        seedMerchantNotifications();
    }

    private void seedMerchantNotifications() {
        String userId = "user_001";
        String collectionName = "merchant_profiles/" + userId + "/notifications";
        List<Map<String, Object>> notifications = Arrays.asList(
                Map.ofEntries(
                        Map.entry("id", "mnotif_001"),
                        Map.entry("type", 21),
                        Map.entry("title", "Đơn hàng mới từ khách hàng"),
                        Map.entry("body", "Bạn có đơn hàng mới: order_001"),
                        Map.entry("referenceId", "order_001"),
                        Map.entry("isRead", false),
                        Map.entry("createdAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "mnotif_002"),
                        Map.entry("type", 21),
                        Map.entry("title", "Đơn hàng mới từ khách hàng"),
                        Map.entry("body", "Bạn có đơn hàng mới: order_003"),
                        Map.entry("referenceId", "order_003"),
                        Map.entry("isRead", true),
                        Map.entry("createdAt", FieldValue.serverTimestamp())
                )
        );
        kiemTraVaSeed(collectionName, notifications);
    }

    private void seedAdminProfiles() {
        String collectionName = "admin_profiles";
        List<Map<String, Object>> profiles = Arrays.asList(
                Map.ofEntries(
                        Map.entry("id", "user_002"),
                        Map.entry("adminLevel", 1),
                        Map.entry("department", "Bộ phận vận hành"),
                        Map.entry("permissions", List.of("manage_users", "manage_orders", "manage_stores", "view_reports")),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                )
        );
        kiemTraVaSeed(collectionName, profiles);
    }
}
