package com.example.be_foodgo.seeder;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

//comment nó lại đi
// @Component
public class FirebaseDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(FirebaseDataSeeder.class);

    private final Firestore firestore;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public FirebaseDataSeeder(Firestore firestore, PasswordEncoder passwordEncoder) {
        this.firestore = firestore;
        this.passwordEncoder = passwordEncoder;
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
                if (docId == null) {
                    log.warn("Document trong collection [{}] khong co truong 'id', bo qua.", collectionName);
                    continue;
                }
                batch.set(firestore.collection(collectionName).document(docId), doc);
            }
            List<WriteResult> results = batch.commit().get();
            log.info("Da seed {} document vao collection [{}]. WriteResults: {}", documents.size(), collectionName, results);
        } catch (Exception e) {
            log.error("Loi khi seed collection [{}]: {}", collectionName, e.getMessage(), e);
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
                        Map.entry("role", "merchant"),
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
                        Map.entry("role", "driver"),
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
                        Map.entry("role", "driver"),
                        Map.entry("balance", 1200000.0),
                        Map.entry("totalEarned", 2000000.0),
                        Map.entry("totalWithdrawn", 800000.0),
                        Map.entry("pendingBalance", 0.0),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "wallet_004"),
                        Map.entry("userId", "user_004"),
                        Map.entry("role", "driver"),
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
                        Map.entry("role", "driver"),
                        Map.entry("balance", 750000.0),
                        Map.entry("totalEarned", 1500000.0),
                        Map.entry("totalWithdrawn", 750000.0),
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
                        Map.entry("type", "order_payment"),
                        Map.entry("amount", 76500.0),
                        Map.entry("fee", 11475.0),
                        Map.entry("netAmount", 65025.0),
                        Map.entry("description", "Don hang order_001 - Phien ban tru phi hoa hong"),
                        Map.entry("orderId", "order_001"),
                        Map.entry("status", "completed"),
                        Map.entry("createdAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "trans_002"),
                        Map.entry("walletId", "wallet_002"),
                        Map.entry("userId", "user_001"),
                        Map.entry("type", "delivery_income"),
                        Map.entry("amount", 15000.0),
                        Map.entry("fee", 3000.0),
                        Map.entry("netAmount", 12000.0),
                        Map.entry("description", "Thu nhap giao hang don order_001"),
                        Map.entry("orderId", "order_001"),
                        Map.entry("status", "completed"),
                        Map.entry("createdAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "trans_003"),
                        Map.entry("walletId", "wallet_003"),
                        Map.entry("userId", "user_003"),
                        Map.entry("type", "withdrawal"),
                        Map.entry("amount", 200000.0),
                        Map.entry("fee", 0.0),
                        Map.entry("netAmount", 200000.0),
                        Map.entry("description", "Rut tien ve tai khoan ngan hang"),
                        Map.entry("status", "completed"),
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
                        Map.entry("password", passwordEncoder.encode("password123")),
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
                        Map.entry("password", passwordEncoder.encode("admin123")),
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
                        Map.entry("password", passwordEncoder.encode("driver123")),
                        Map.entry("fullName", "Le Van B"),
                        Map.entry("phoneNumber", "0912345678"),
                        Map.entry("photoUrl", "https://example.com/avatar/driver001.jpg"),
                        Map.entry("roles", Arrays.asList(2)),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp()),
                        Map.entry("isEmailVerified", true)
                ),
                Map.ofEntries(
                        Map.entry("id", "user_004"),
                        Map.entry("email", "taixe2@gmail.com"),
                        Map.entry("password", passwordEncoder.encode("driver456")),
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
                        Map.entry("password", passwordEncoder.encode("driver789")),
                        Map.entry("fullName", "Tran Van D"),
                        Map.entry("phoneNumber", "0934567890"),
                        Map.entry("photoUrl", "https://example.com/avatar/driver003.jpg"),
                        Map.entry("roles", Arrays.asList(2)),
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
                        Map.entry("keyword", "com tam"),
                        Map.entry("keywordNormalized", "com tam"),
                        Map.entry("createdAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "sh_002"),
                        Map.entry("keyword", "tra sua"),
                        Map.entry("keywordNormalized", "tra sua"),
                        Map.entry("createdAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "sh_003"),
                        Map.entry("keyword", "ga ran"),
                        Map.entry("keywordNormalized", "ga ran"),
                        Map.entry("createdAt", FieldValue.serverTimestamp())
                )
        );
        kiemTraVaSeed(collectionName, histories);
    }

    private void seedSystemCategories() {
        String collectionName = "categories";
        List<Map<String, Object>> categories = Arrays.asList(
                createCategoryMap("syscate_001", null, "Com", "restaurant", 1, "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=400&q=80"),
                createCategoryMap("syscate_002", null, "Pho/Bun", "restaurant", 2, "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=400&q=80"),
                createCategoryMap("syscate_003", null, "Tra sua", "local_cafe", 3, "https://images.unsplash.com/photo-1558857563-b371033873b8?w=400&q=80"),
                createCategoryMap("syscate_004", null, "An vat", "fastfood", 4, "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&q=80"),
                createCategoryMap("syscate_005", null, "Ga ran", "fastfood", 5, "https://images.unsplash.com/photo-1626645738196-c2a7c87a8f58?w=400&q=80"),
                createCategoryMap("syscate_006", null, "Mon Han", "restaurant", 6, "https://images.unsplash.com/photo-1559314809-0d155014e29e?w=400&q=80"),
                createCategoryMap("syscate_007", null, "Mon Nhat", "restaurant", 7, "https://images.unsplash.com/photo-1617196034183-421b4040ed20?w=400&q=80"),
                createCategoryMap("syscate_008", null, "Banh mi", "bakery_dining", 8, "https://images.unsplash.com/photo-1605478371119-43802a1c79f5?w=400&q=80"),
                createCategoryMap("syscate_009", null, "Lau/Buffet", "restaurant", 9, "https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=400&q=80"),
                createCategoryMap("syscate_010", null, "Tra cay", "local_cafe", 10, "https://images.unsplash.com/photo-1550508776-b6a354f5f66d?w=400&q=80")
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
                createCategoryMap("stocate_001", "store_001", "Mon chinh", "restaurant", 1, "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=400&q=80"),
                createCategoryMap("stocate_002", "store_001", "Mon phu", "restaurant", 2, "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&q=80"),
                createCategoryMap("stocate_003", "store_001", "Nuoc uong", "local_cafe", 3, "https://images.unsplash.com/photo-1558857563-b371033873b8?w=400&q=80"),
                createCategoryMap("stocate_004", "store_002", "Tra sua", "local_cafe", 1, "https://images.unsplash.com/photo-1558857563-b371033873b8?w=400&q=80"),
                createCategoryMap("stocate_005", "store_002", "Tra trai cay", "local_cafe", 2, "https://images.unsplash.com/photo-1550508776-b6a354f5f66d?w=400&q=80"),
                createCategoryMap("stocate_006", "store_003", "Banh mi", "bakery_dining", 1, "https://images.unsplash.com/photo-1605478371119-43802a1c79f5?w=400&q=80"),
                createCategoryMap("stocate_007", "store_003", "Do an them", "fastfood", 2, "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&q=80")
        );
        kiemTraVaSeed(collectionName, categories);
    }

    private void seedStores() {
        String collectionName = "stores";
        List<Map<String, Object>> stores = Arrays.asList(
                Map.ofEntries(
                        Map.entry("id", "store_001"),
                        Map.entry("name", "Com tam Phuc Loc Tho"),
                        Map.entry("address", "123 Le Van Viet, TP. Thu Duc"),
                        Map.entry("rating", 4.8),
                        Map.entry("reviewCount", 500),
                        Map.entry("avtUrl", "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=400&q=80"),
                        Map.entry("backUrl", "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=800&q=80"),
                        Map.entry("isOpen", true),
                        Map.entry("deliveryTime", "20-30 phut"),
                        Map.entry("deliveryFee", 15000.0),
                        Map.entry("categoryIds", Arrays.asList("cate_001", "cate_004")),
                        Map.entry("lat", 10.8500),
                        Map.entry("lng", 106.7900),
                        Map.entry("restaurant_categories", Map.of(
                                "rest_cate_001", Map.of(
                                        "name", "Mon chinh",
                                        "order", 1,
                                        "createdAt", FieldValue.serverTimestamp(),
                                        "updatedAt", FieldValue.serverTimestamp()
                                ),
                                "rest_cate_002", Map.of(
                                        "name", "Mon phu",
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
                        Map.entry("name", "Tra sua Tocotoco"),
                        Map.entry("address", "456 Nguyen Thi Dinh, TP. Thu Duc"),
                        Map.entry("rating", 4.6),
                        Map.entry("reviewCount", 300),
                        Map.entry("avtUrl", "https://images.unsplash.com/photo-1558857563-b371033873b8?w=400&q=80"),
                        Map.entry("backUrl", "https://images.unsplash.com/photo-1557992260-ec58fa23b80b?w=800&q=80"),
                        Map.entry("isOpen", true),
                        Map.entry("deliveryTime", "15-25 phut"),
                        Map.entry("deliveryFee", 12000.0),
                        Map.entry("categoryIds", Arrays.asList("cate_003", "cate_010")),
                        Map.entry("lat", 10.8520),
                        Map.entry("lng", 106.7850),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "store_003"),
                        Map.entry("name", "Ga ran KFC Nguyen Cuu"),
                        Map.entry("address", "789 Nguyen Cuu, TP. Thu Duc"),
                        Map.entry("rating", 4.5),
                        Map.entry("reviewCount", 800),
                        Map.entry("avtUrl", "https://images.unsplash.com/photo-1626645738196-c2a7c87a8f58?w=400&q=80"),
                        Map.entry("backUrl", "https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?w=800&q=80"),
                        Map.entry("isOpen", true),
                        Map.entry("deliveryTime", "25-35 phut"),
                        Map.entry("deliveryFee", 18000.0),
                        Map.entry("categoryIds", Arrays.asList("cate_005")),
                        Map.entry("lat", 10.8480),
                        Map.entry("lng", 106.7920),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "store_004"),
                        Map.entry("name", "Bun bo Hue Ba Le"),
                        Map.entry("address", "101 Pho Hue, Q.1, TP.HCM"),
                        Map.entry("rating", 4.7),
                        Map.entry("reviewCount", 450),
                        Map.entry("avtUrl", "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=400&q=80"),
                        Map.entry("backUrl", "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=800&q=80"),
                        Map.entry("isOpen", true),
                        Map.entry("deliveryTime", "30-40 phut"),
                        Map.entry("deliveryFee", 20000.0),
                        Map.entry("categoryIds", Arrays.asList("cate_002")),
                        Map.entry("lat", 10.8460),
                        Map.entry("lng", 106.7880),
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
                        Map.entry("categoryName", "Com"),
                        Map.entry("name", "Com tam suon bi cha"),
                        Map.entry("description", "Com tam ngon chuan vi Sai Gon voi suon nuong thom phuc"),
                        Map.entry("basePrice", 45000.0),
                        Map.entry("imageUrl", "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=400&q=80"),
                        Map.entry("isOutOfStock", false),
                        Map.entry("isFeatured", true),
                        Map.entry("optionGroups", List.of(
                                Map.of(
                                        "name", "Kich thuoc",
                                        "options", List.of(
                                                Map.of("name", "Vua", "price", 0.0),
                                                Map.of("name", "Lon", "price", 10000.0)
                                        )
                                )
                        )),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "prod_002"),
                        Map.entry("storeId", "store_001"),
                        Map.entry("categoryId", "cate_001"),
                        Map.entry("categoryName", "Com"),
                        Map.entry("name", "Com tam ga xoi mo"),
                        Map.entry("description", "Com tam voi ga xoi mo giòn tan"),
                        Map.entry("basePrice", 50000.0),
                        Map.entry("imageUrl", "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&q=80"),
                        Map.entry("isOutOfStock", false),
                        Map.entry("isFeatured", false),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "prod_003"),
                        Map.entry("storeId", "store_001"),
                        Map.entry("categoryId", "cate_004"),
                        Map.entry("categoryName", "An vat"),
                        Map.entry("name", "Banh bot loc"),
                        Map.entry("description", "Banh bot loc hap chan, nuong giòn"),
                        Map.entry("basePrice", 25000.0),
                        Map.entry("imageUrl", "https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?w=400&q=80"),
                        Map.entry("isOutOfStock", false),
                        Map.entry("isFeatured", false),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "prod_004"),
                        Map.entry("storeId", "store_002"),
                        Map.entry("categoryId", "cate_003"),
                        Map.entry("categoryName", "Tra sua"),
                        Map.entry("name", "Tra sua trach tang"),
                        Map.entry("description", "Tra sua thom ngat voi tra mach mong va trach tang dai"),
                        Map.entry("basePrice", 29000.0),
                        Map.entry("imageUrl", "https://images.unsplash.com/photo-1558857563-b371033873b8?w=400&q=80"),
                        Map.entry("isOutOfStock", false),
                        Map.entry("isFeatured", true),
                        Map.entry("optionGroups", List.of(
                                Map.of(
                                        "name", "Kich thuoc",
                                        "options", List.of(
                                                Map.of("name", "M", "price", 0.0),
                                                Map.of("name", "L", "price", 5000.0)
                                        )
                                ),
                                Map.of(
                                        "name", "Topping",
                                        "options", List.of(
                                                Map.of("name", "Tran chau", "price", 5000.0),
                                                Map.of("name", "Thach", "price", 3000.0),
                                                Map.of("name", "Pudding", "price", 6000.0)
                                        )
                                )
                        )),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "prod_005"),
                        Map.entry("storeId", "store_002"),
                        Map.entry("categoryId", "cate_010"),
                        Map.entry("categoryName", "Tra cay"),
                        Map.entry("name", "Tra dao cam"),
                        Map.entry("description", "Tra dao cam that huong vi dai"),
                        Map.entry("basePrice", 25000.0),
                        Map.entry("imageUrl", "https://images.unsplash.com/photo-1550508776-b6a354f5f66d?w=400&q=80"),
                        Map.entry("isOutOfStock", false),
                        Map.entry("isFeatured", false),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "prod_006"),
                        Map.entry("storeId", "store_002"),
                        Map.entry("categoryId", "cate_003"),
                        Map.entry("categoryName", "Tra sua"),
                        Map.entry("name", "Tra sua khoai mon"),
                        Map.entry("description", "Tra sua kem duong bui voi khoai mon ngot tan"),
                        Map.entry("basePrice", 33000.0),
                        Map.entry("imageUrl", "https://images.unsplash.com/photo-1557992260-ec58fa23b80b?w=400&q=80"),
                        Map.entry("isOutOfStock", true),
                        Map.entry("isFeatured", false),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "prod_007"),
                        Map.entry("storeId", "store_003"),
                        Map.entry("categoryId", "cate_005"),
                        Map.entry("categoryName", "Ga ran"),
                        Map.entry("name", "Ga lap xuong"),
                        Map.entry("description", "Ga lap xuong giòn oi, thit nong mach, nau tu bot phap"),
                        Map.entry("basePrice", 55000.0),
                        Map.entry("imageUrl", "https://images.unsplash.com/photo-1626645738196-c2a7c87a8f58?w=400&q=80"),
                        Map.entry("isOutOfStock", false),
                        Map.entry("isFeatured", true),
                        Map.entry("optionGroups", List.of(
                                Map.of(
                                        "name", "Phan an",
                                        "options", List.of(
                                                Map.of("name", "1 phan", "price", 0.0),
                                                Map.of("name", "2 phan", "price", 20000.0)
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
                        Map.entry("categoryName", "Ga ran"),
                        Map.entry("name", "Mi ga chua cay"),
                        Map.entry("description", "Mi ga nau chua cay dam da, hau sat nuoi"),
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
                        Map.entry("categoryName", "Ga ran"),
                        Map.entry("name", "Khoai tay chien"),
                        Map.entry("description", "Khoai tay chien giòn that dai"),
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
                        Map.entry("categoryName", "Pho/Bun"),
                        Map.entry("name", "Bun bo Hue"),
                        Map.entry("description", "Bun bo Hue nuoc dung trong, thit bo chin mong, chan cut thom phuc"),
                        Map.entry("basePrice", 45000.0),
                        Map.entry("imageUrl", "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=400&q=80"),
                        Map.entry("isOutOfStock", false),
                        Map.entry("isFeatured", true),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "prod_011"),
                        Map.entry("storeId", "store_004"),
                        Map.entry("categoryId", "cate_002"),
                        Map.entry("categoryName", "Pho/Bun"),
                        Map.entry("name", "Bun mam"),
                        Map.entry("description", "Bun mam dac san Vung Tau voi ca bom va cua"),
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
                        Map.entry("categoryName", "Pho/Bun"),
                        Map.entry("name", "Bun rieu"),
                        Map.entry("description", "Bun rieu cua that ngon voi rieu nau tom chat"),
                        Map.entry("basePrice", 40000.0),
                        Map.entry("imageUrl", "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=400&q=80"),
                        Map.entry("isOutOfStock", false),
                        Map.entry("isFeatured", false),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "prod_013"),
                        Map.entry("storeId", "store_001"),
                        Map.entry("categoryId", "cate_001"),
                        Map.entry("categoryName", "Com"),
                        Map.entry("name", "Com suon tron"),
                        Map.entry("description", "Com suon tron trung thap cam"),
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
                        Map.entry("categoryName", "Tra sua"),
                        Map.entry("name", "Tra sua trai cay"),
                        Map.entry("description", "Tra sua thap cam voi trai cay tuoi ngon"),
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
                        Map.entry("categoryName", "An vat"),
                        Map.entry("name", "Gio cha"),
                        Map.entry("description", "Gio cha bi thom ngon chat luong"),
                        Map.entry("basePrice", 15000.0),
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
        banner1.put("title", "Sieu sale giua thang");
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
        banner2.put("title", "Freeship 0 dong");
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
        banner3.put("title", "Le hoi am thuc");
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
        banner4.put("title", "Uong tra van chiu");
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
        List<Map<String, Object>> vouchers = Arrays.asList(
                createVoucherMap("voucher_001", "Giam 20K cho don tu 100K", "Giam 20K cho don tu 100K", "Ap dung cho tat ca quan an.", null, "GIAM20K", 2, 20000.0, false, 100, "Ap dung cho tat ca quan an.", 100000.0, "2026-05-30T23:59:59Z", 0, 0),
                createVoucherMap("voucher_002", "Freeship Quan ABC", "Freeship Quan ABC", "Chi ap dung tai Quan ABC.", "store_001", "ABC15K", 2, 15000.0, false, 50, "Chi ap dung tai Quan ABC.", 80000.0, "2026-06-01T23:59:59Z", 0, 0),
                createVoucherMap("fs_001", "Mien phi giao hang", "Mien phi giao hang", "Ap dung cho don tu 50K.", null, "FREESHIP", 2, 15000.0, true, 200, "Ap dung cho don tu 50K.", 50000.0, "2026-06-15T23:59:59Z", 0, 0),
                createVoucherMap("fs_002", "Freeship Quan XYZ", "Freeship Quan XYZ", "Chi ap dung tai Quan XYZ.", "store_002", "XYZSHIP", 2, 15000.0, true, 30, "Chi ap dung tai Quan XYZ.", 30000.0, "2026-06-10T23:59:59Z", 0, 0),
                createVoucherMap("sys_voucher_001", "Giam 20K cho don tu 100K", "Giam 20K cho don tu 100K", "Ap dung cho tat ca quan an.", null, "SYSGIAM20K", 2, 20000.0, false, 100, "Ap dung cho tat ca quan an.", 100000.0, "2026-05-30T23:59:59Z", 400, 30),
                createVoucherMap("sys_voucher_002", "Giam 15% cho don tu 150K", "Giam 15% cho don tu 150K", "Giam toi da 40K. Ap dung toan he thong.", null, "SYSGIAM15P", 1, 15.0, false, 75, "Giam toi da 40K. Ap dung toan he thong.", 150000.0, "2026-06-30T23:59:59Z", 500, 30),
                createVoucherMap("sys_fs_001", "Mien phi giao hang", "Mien phi giao hang", "Mien phi giao hang cho don tu 50K.", null, "SYSFREESHIP", 2, 15000.0, true, 200, "Mien phi giao hang cho don tu 50K.", 50000.0, "2026-06-15T23:59:59Z", 300, 30)
        );
        kiemTraVaSeed(collectionName, vouchers);
    }

    private Map<String, Object> createVoucherMap(String id, String name, String title, String subtitle, String storeId, String code,
            int type, double value, boolean isFreeship, int remaining, String terms,
            double minOrderValue, String expiryDate, int pointsRequired, int validityDays) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("title", title);
        map.put("subtitle", subtitle);
        map.put("storeId", storeId);
        map.put("code", code);
        map.put("type", type);
        map.put("value", value);
        map.put("imageUrl", "https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?w=400&q=80");
        map.put("remaining", remaining);
        map.put("isActive", true);
        map.put("isFreeship", isFreeship);
        map.put("terms", terms);
        map.put("minOrderValue", minOrderValue);
        map.put("expiryDate", expiryDate);
        map.put("pointsRequired", pointsRequired);
        map.put("validityDays", validityDays);
        map.put("createdAt", FieldValue.serverTimestamp());
        map.put("updatedAt", FieldValue.serverTimestamp());
        return map;
    }

    private void seedReviews() {
        String collectionName = "reviews";
        List<Map<String, Object>> reviews = Arrays.asList(
                Map.ofEntries(
                        Map.entry("id", "rev_001"),
                        Map.entry("storeId", "store_001"),
                        Map.entry("userId", "user_001"),
                        Map.entry("userName", "Khoi"),
                        Map.entry("userAvatarUrl", "https://example.com/avatar/user001.jpg"),
                        Map.entry("starRating", 5),
                        Map.entry("comment", "Do an rat ngon, giao hang nhanh, dong goi ky luong."),
                        Map.entry("imageUrls", List.of(
                                "https://example.com/review/rev001_1.jpg",
                                "https://example.com/review/rev001_2.jpg"
                        )),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "rev_002"),
                        Map.entry("storeId", "store_001"),
                        Map.entry("userId", "user_002"),
                        Map.entry("userName", "Quan Tri Vien"),
                        Map.entry("userAvatarUrl", "https://example.com/avatar/admin.jpg"),
                        Map.entry("starRating", 4),
                        Map.entry("comment", "Mon an ngon, nhung giao hang tre hon 15 phut."),
                        Map.entry("imageUrls", List.of()),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "rev_003"),
                        Map.entry("storeId", "store_002"),
                        Map.entry("userId", "user_001"),
                        Map.entry("userName", "Khoi"),
                        Map.entry("userAvatarUrl", "https://example.com/avatar/user001.jpg"),
                        Map.entry("starRating", 5),
                        Map.entry("comment", "Tra sua rat ngon, topping nhieu, uong la lanh."),
                        Map.entry("imageUrls", List.of(
                                "https://example.com/review/rev003_1.jpg"
                        )),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "rev_004"),
                        Map.entry("storeId", "store_003"),
                        Map.entry("userId", "user_002"),
                        Map.entry("userName", "Quan Tri Vien"),
                        Map.entry("userAvatarUrl", "https://example.com/avatar/admin.jpg"),
                        Map.entry("starRating", 4),
                        Map.entry("comment", "Ga ran gion, an bieu nhu ham thit ngot."),
                        Map.entry("imageUrls", List.of()),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "rev_005"),
                        Map.entry("storeId", "store_004"),
                        Map.entry("userId", "user_001"),
                        Map.entry("userName", "Khoi"),
                        Map.entry("userAvatarUrl", "https://example.com/avatar/user001.jpg"),
                        Map.entry("starRating", 5),
                        Map.entry("comment", "Bun bo Hue ngon chuan, nuoc dung ngot thanh."),
                        Map.entry("imageUrls", List.of(
                                "https://example.com/review/rev005_1.jpg",
                                "https://example.com/review/rev005_2.jpg"
                        )),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "rev_006"),
                        Map.entry("storeId", "store_001"),
                        Map.entry("userId", "user_003"),
                        Map.entry("userName", "Le Van B"),
                        Map.entry("userAvatarUrl", "https://example.com/avatar/driver001.jpg"),
                        Map.entry("starRating", 4),
                        Map.entry("comment", "Com tam ngon, phan an vua du."),
                        Map.entry("imageUrls", List.of()),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "rev_007"),
                        Map.entry("storeId", "store_002"),
                        Map.entry("userId", "user_003"),
                        Map.entry("userName", "Le Van B"),
                        Map.entry("userAvatarUrl", "https://example.com/avatar/driver001.jpg"),
                        Map.entry("starRating", 5),
                        Map.entry("comment", "Quan nay ban tra sua ngon lam, giao hang cung nhanh."),
                        Map.entry("imageUrls", List.of(
                                "https://example.com/review/rev007_1.jpg"
                        )),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "rev_008"),
                        Map.entry("storeId", "store_003"),
                        Map.entry("userId", "user_001"),
                        Map.entry("userName", "Khoi"),
                        Map.entry("userAvatarUrl", "https://example.com/avatar/user001.jpg"),
                        Map.entry("starRating", 3),
                        Map.entry("comment", "An cung duoc nhung gia ca hoi cao."),
                        Map.entry("imageUrls", List.of()),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                )
        );
        kiemTraVaSeed(collectionName, reviews);
    }

    private void seedOrders() {
        String collectionName = "orders";
        List<Map<String, Object>> orders = new ArrayList<>();

        List<Map<String, Object>> order1Items = new ArrayList<>();
        Map<String, Object> order1Item1 = new HashMap<>();
        order1Item1.put("foodId", "prod_001");
        order1Item1.put("name", "Com tam suon bi cha");
        order1Item1.put("price", 45000.0);
        order1Item1.put("quantity", 2);
        order1Item1.put("imageUrl", "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=400&q=80");
        order1Items.add(order1Item1);
        Map<String, Object> order1 = new HashMap<>();
        order1.put("id", "order_001");
        order1.put("userId", "user_001");
        order1.put("storeId", "store_001");
        order1.put("storeName", "Com tam Phuc Loc Tho");
        order1.put("items", order1Items);
        order1.put("totalAmount", 140000.0);
        order1.put("deliveryFee", 15000.0);
        order1.put("status", 2);
        order1.put("deliveryAddress", "Ky tuc xa UTC2, Quan 9, TP.HCM");
        order1.put("paymentMethod", "momo");
        order1.put("driverId", "user_001");
        order1.put("driverName", "Le Van B");
        order1.put("driverPhone", "0912345678");
        order1.put("vehiclePlate", "59A-123.45");
        order1.put("createdAt", FieldValue.serverTimestamp());
        order1.put("updatedAt", FieldValue.serverTimestamp());
        order1.put("note", "Giao gap");
        orders.add(order1);

        List<Map<String, Object>> order2Items = new ArrayList<>();
        Map<String, Object> order2Item1 = new HashMap<>();
        order2Item1.put("foodId", "prod_004");
        order2Item1.put("name", "Tra sua trach tang");
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
        order2.put("storeName", "Tra sua Tocotoco");
        order2.put("items", order2Items);
        order2.put("totalAmount", 79000.0);
        order2.put("deliveryFee", 12000.0);
        order2.put("status", 3);
        order2.put("deliveryAddress", "Ky tuc xa UTC2, Quan 9, TP.HCM");
        order2.put("paymentMethod", "cash");
        order2.put("driverId", "user_003");
        order2.put("driverName", "Le Van B");
        order2.put("driverPhone", "0912345678");
        order2.put("vehiclePlate", "59A-123.45");
        order2.put("createdAt", FieldValue.serverTimestamp());
        order2.put("updatedAt", FieldValue.serverTimestamp());
        order2.put("note", "Khong banh chan");
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
        order3Item2.put("name", "Khoai tay chien");
        order3Item2.put("price", 20000.0);
        order3Item2.put("quantity", 1);
        order3Item2.put("imageUrl", "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&q=80");
        order3Items.add(order3Item2);
        Map<String, Object> order3 = new HashMap<>();
        order3.put("id", "order_003");
        order3.put("userId", "user_002");
        order3.put("storeId", "store_003");
        order3.put("storeName", "Ga ran KFC Nguyen Cuu");
        order3.put("items", order3Items);
        order3.put("totalAmount", 93000.0);
        order3.put("deliveryFee", 18000.0);
        order3.put("status", 1);
        order3.put("deliveryAddress", "123 Le Van Viet, TP. Thu Duc");
        order3.put("paymentMethod", "momo");
        order3.put("driverId", null);
        order3.put("driverName", null);
        order3.put("driverPhone", null);
        order3.put("vehiclePlate", null);
        order3.put("createdAt", FieldValue.serverTimestamp());
        order3.put("updatedAt", FieldValue.serverTimestamp());
        order3.put("note", "");
        orders.add(order3);

        List<Map<String, Object>> order4Items = new ArrayList<>();
        Map<String, Object> order4Item1 = new HashMap<>();
        order4Item1.put("foodId", "prod_010");
        order4Item1.put("name", "Bun bo Hue");
        order4Item1.put("price", 45000.0);
        order4Item1.put("quantity", 1);
        order4Item1.put("imageUrl", "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=400&q=80");
        order4Items.add(order4Item1);
        Map<String, Object> order4 = new HashMap<>();
        order4.put("id", "order_004");
        order4.put("userId", "user_002");
        order4.put("storeId", "store_004");
        order4.put("storeName", "Bun bo Hue Ba Le");
        order4.put("items", order4Items);
        order4.put("totalAmount", 65000.0);
        order4.put("deliveryFee", 20000.0);
        order4.put("status", 0);
        order4.put("deliveryAddress", "456 Nguyen Thi Dinh, TP. Thu Duc");
        order4.put("paymentMethod", "cash");
        order4.put("driverId", null);
        order4.put("driverName", null);
        order4.put("driverPhone", null);
        order4.put("vehiclePlate", null);
        order4.put("createdAt", FieldValue.serverTimestamp());
        order4.put("updatedAt", FieldValue.serverTimestamp());
        order4.put("note", "Giao sau 30 phut");
        orders.add(order4);

        List<Map<String, Object>> order5Items = new ArrayList<>();
        Map<String, Object> order5Item1 = new HashMap<>();
        order5Item1.put("foodId", "prod_002");
        order5Item1.put("name", "Com tam ga xoi mo");
        order5Item1.put("price", 50000.0);
        order5Item1.put("quantity", 1);
        order5Item1.put("imageUrl", "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=400&q=80");
        order5Items.add(order5Item1);
        Map<String, Object> order5 = new HashMap<>();
        order5.put("id", "order_005");
        order5.put("userId", "user_001");
        order5.put("storeId", "store_001");
        order5.put("storeName", "Com tam Phuc Loc Tho");
        order5.put("items", order5Items);
        order5.put("totalAmount", 65000.0);
        order5.put("deliveryFee", 15000.0);
        order5.put("status", 4);
        order5.put("deliveryAddress", "Ky tuc xa UTC2, Quan 9, TP.HCM");
        order5.put("paymentMethod", "zalo");
        order5.put("driverId", null);
        order5.put("driverName", null);
        order5.put("driverPhone", null);
        order5.put("vehiclePlate", null);
        order5.put("createdAt", FieldValue.serverTimestamp());
        order5.put("updatedAt", FieldValue.serverTimestamp());
        order5.put("note", "Bo qua nuoc mam");
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
        order6Item2.put("name", "Tra sua trai cay");
        order6Item2.put("price", 32000.0);
        order6Item2.put("quantity", 1);
        order6Item2.put("imageUrl", "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&q=80");
        order6Items.add(order6Item2);
        Map<String, Object> order6 = new HashMap<>();
        order6.put("id", "order_006");
        order6.put("userId", "user_003");
        order6.put("storeId", "store_002");
        order6.put("storeName", "Tra sua Tocotoco");
        order6.put("items", order6Items);
        order6.put("totalAmount", 87000.0);
        order6.put("deliveryFee", 12000.0);
        order6.put("status", 2);
        order6.put("deliveryAddress", "101 Pho Hue, Q.1, TP.HCM");
        order6.put("paymentMethod", "card");
        order6.put("driverId", "user_001");
        order6.put("driverName", "Le Van B");
        order6.put("driverPhone", "0912345678");
        order6.put("vehiclePlate", "59A-123.45");
        order6.put("createdAt", FieldValue.serverTimestamp());
        order6.put("updatedAt", FieldValue.serverTimestamp());
        order6.put("note", "");
        orders.add(order6);

        List<Map<String, Object>> order7Items = new ArrayList<>();
        Map<String, Object> order7Item1 = new HashMap<>();
        order7Item1.put("foodId", "prod_008");
        order7Item1.put("name", "Mi ga chua cay");
        order7Item1.put("price", 35000.0);
        order7Item1.put("quantity", 2);
        order7Item1.put("imageUrl", "https://images.unsplash.com/photo-1573080496219-bb080dd4f877?w=400&q=80");
        order7Items.add(order7Item1);
        Map<String, Object> order7 = new HashMap<>();
        order7.put("id", "order_007");
        order7.put("userId", "user_002");
        order7.put("storeId", "store_003");
        order7.put("storeName", "Ga ran KFC Nguyen Cuu");
        order7.put("items", order7Items);
        order7.put("totalAmount", 88000.0);
        order7.put("deliveryFee", 18000.0);
        order7.put("status", 3);
        order7.put("deliveryAddress", "789 Nguyen Cuu, TP. Thu Duc");
        order7.put("paymentMethod", "momo");
        order7.put("driverId", "user_003");
        order7.put("driverName", "Le Van B");
        order7.put("driverPhone", "0912345678");
        order7.put("vehiclePlate", "59A-123.45");
        order7.put("createdAt", FieldValue.serverTimestamp());
        order7.put("updatedAt", FieldValue.serverTimestamp());
        order7.put("note", "Giao nhanh");
        orders.add(order7);

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
        List<Map<String, Object>> addresses = Arrays.asList(
                Map.ofEntries(
                        Map.entry("id", "addr_001"),
                        Map.entry("name", "Nha rieng"),
                        Map.entry("address", "Ky tuc xa UTC2, Quan 9, TP.HCM"),
                        Map.entry("receiverName", "Khoi"),
                        Map.entry("receiverPhone", "0123456789"),
                        Map.entry("lat", 10.8455),
                        Map.entry("lng", 106.7939),
                        Map.entry("isDefault", true),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "addr_002"),
                        Map.entry("name", "Truong hoc"),
                        Map.entry("address", "Truong Dai hoc Giao thong Van tai, TP. Thu Duc"),
                        Map.entry("receiverName", "Khoi"),
                        Map.entry("receiverPhone", "0123456789"),
                        Map.entry("lat", 10.8490),
                        Map.entry("lng", 106.7890),
                        Map.entry("isDefault", false),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                )
        );
        kiemTraVaSeed(collectionName, addresses);
    }

    private void seedCustomerPaymentMethods() {
        String userId = "user_001";
        String collectionName = "customer_profiles/" + userId + "/payment_methods";
        List<Map<String, Object>> methods = new ArrayList<>();

        Map<String, Object> pm1 = new HashMap<>();
        pm1.put("id", "pm_001");
        pm1.put("type", 2);
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
        pm2.put("type", 3);
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
        notif1.put("title", "Don hang da duoc giao thanh cong");
        notif1.put("body", "Don hang order_001 da duoc giao");
        notif1.put("referenceId", "order_001");
        notif1.put("isRead", false);
        notif1.put("createdAt", FieldValue.serverTimestamp());
        notifications.add(notif1);

        Map<String, Object> notif2 = new HashMap<>();
        notif2.put("id", "notif_002");
        notif2.put("type", 1);
        notif2.put("title", "Khuyen mai dac biet");
        notif2.put("body", "Giam 20% cho don hang dau tien");
        notif2.put("referenceId", "voucher_001");
        notif2.put("isRead", true);
        notif2.put("createdAt", FieldValue.serverTimestamp());
        notifications.add(notif2);

        Map<String, Object> notif3 = new HashMap<>();
        notif3.put("id", "notif_003");
        notif3.put("type", 0);
        notif3.put("title", "Chao mung den voi FoodGo");
        notif3.put("body", "Cam on ban da dang ky tai khoan tai FoodGo");
        notif3.put("referenceId", null);
        notif3.put("isRead", true);
        notif3.put("createdAt", FieldValue.serverTimestamp());
        notifications.add(notif3);

        kiemTraVaSeed(collectionName, notifications);
    }

    private void seedCustomerCart() {
        String userId = "user_001";
        String collectionName = "customer_profiles/" + userId + "/cart";
        List<Map<String, Object>> cartItems = Arrays.asList(
                Map.ofEntries(
                        Map.entry("id", "cart_item_001"),
                        Map.entry("storeId", "store_001"),
                        Map.entry("foodId", "prod_001"),
                        Map.entry("name", "Com tam suon bi cha"),
                        Map.entry("price", 45000.0),
                        Map.entry("quantity", 2),
                        Map.entry("imageUrl", "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=400&q=80"),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "cart_item_002"),
                        Map.entry("storeId", "store_002"),
                        Map.entry("foodId", "prod_004"),
                        Map.entry("name", "Tra sua trach tang"),
                        Map.entry("price", 44000.0),
                        Map.entry("quantity", 1),
                        Map.entry("size", "L"),
                        Map.entry("sizePrice", 5000.0),
                        Map.entry("toppings", List.of(
                                Map.of("name", "Tran chau trang", "price", 10000.0),
                                Map.of("name", "Thach trai cay", "price", 8000.0)
                        )),
                        Map.entry("note", "It duong"),
                        Map.entry("imageUrl", "https://images.unsplash.com/photo-1558857563-b371033873b8?w=400&q=80"),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                )
        );
        kiemTraVaSeed(collectionName, cartItems);
    }

    private void seedCustomerMyVouchers() {
        String userId = "user_001";
        String collectionName = "customer_profiles/" + userId + "/my_vouchers";
        List<Map<String, Object>> myVouchers = Arrays.asList(
                Map.ofEntries(
                        Map.entry("id", "mv_001"),
                        Map.entry("name", "Giam 20K phi giao hang"),
                        Map.entry("title", "Giam 20K phi giao hang"),
                        Map.entry("subtitle", "Ap dung cho don tu 50K."),
                        Map.entry("code", "FREESHIP20"),
                        Map.entry("description", "Ap dung cho don tu 100K"),
                        Map.entry("expiryDate", java.time.Instant.parse("2027-12-31T23:59:59Z")),
                        Map.entry("type", 2),
                        Map.entry("value", 20000.0),
                        Map.entry("imageUrl", "https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?w=400&q=80"),
                        Map.entry("terms", "Ap dung cho don tu 50K."),
                        Map.entry("minOrderValue", 50000.0),
                        Map.entry("isActive", true),
                        Map.entry("isFreeship", true),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "mv_002"),
                        Map.entry("name", "Giam 10% cho don hang"),
                        Map.entry("title", "Giam 10% cho don hang"),
                        Map.entry("subtitle", "Giam 10% cho moi don hang."),
                        Map.entry("code", "SAVE10"),
                        Map.entry("description", "Giam 10% cho moi don hang"),
                        Map.entry("expiryDate", java.time.Instant.parse("2027-12-31T23:59:59Z")),
                        Map.entry("type", 1),
                        Map.entry("value", 10.0),
                        Map.entry("imageUrl", "https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?w=400&q=80"),
                        Map.entry("terms", "Giam 10% cho moi don hang."),
                        Map.entry("minOrderValue", 50000.0),
                        Map.entry("isActive", true),
                        Map.entry("isFreeship", false),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                )
        );
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
                        Map.entry("id", "user_004"),
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
                        Map.entry("title", "Yeu cau nhan don moi"),
                        Map.entry("body", "Ban co don hang moi cho nhan: order_001"),
                        Map.entry("referenceId", "order_001"),
                        Map.entry("isRead", false),
                        Map.entry("createdAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "dnotif_002"),
                        Map.entry("type", 12),
                        Map.entry("title", "Thong bao giao hang"),
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
                        Map.entry("storeIds", List.of("store_001")),
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
                        Map.entry("title", "Don hang moi tu khach hang"),
                        Map.entry("body", "Ban co don hang moi: order_001"),
                        Map.entry("referenceId", "order_001"),
                        Map.entry("isRead", false),
                        Map.entry("createdAt", FieldValue.serverTimestamp())
                ),
                Map.ofEntries(
                        Map.entry("id", "mnotif_002"),
                        Map.entry("type", 21),
                        Map.entry("title", "Don hang moi tu khach hang"),
                        Map.entry("body", "Ban co don hang moi: order_003"),
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
                        Map.entry("department", "Bo phan van hanh"),
                        Map.entry("permissions", List.of("manage_users", "manage_orders", "manage_stores", "view_reports")),
                        Map.entry("createdAt", FieldValue.serverTimestamp()),
                        Map.entry("updatedAt", FieldValue.serverTimestamp())
                )
        );
        kiemTraVaSeed(collectionName, profiles);
    }
}
