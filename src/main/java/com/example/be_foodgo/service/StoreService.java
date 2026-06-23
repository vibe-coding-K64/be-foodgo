package com.example.be_foodgo.service;

import com.example.be_foodgo.dto.NearbyStoreResponse;
import com.example.be_foodgo.dto.PaginationInfo;
import com.example.be_foodgo.dto.PopularStoreResponse;
import com.example.be_foodgo.dto.StoreDTO;
import com.example.be_foodgo.dto.DriverProfileResponse;
import com.example.be_foodgo.dto.UpdateDriverProfileRequest;
import com.example.be_foodgo.model.Store;
import com.example.be_foodgo.repository.StoreRepository;
import com.example.be_foodgo.repository.WalletRepository;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.SetOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class StoreService {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private Firestore firestore;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private MapboxService mapboxService;

    public StoreDTO createMerchantStore(String uid, StoreDTO storeDTO) throws Exception {
        String newStoreId = "store_001";
        var docs = firestore.collection("stores").orderBy("id", com.google.cloud.firestore.Query.Direction.DESCENDING).limit(1).get().get().getDocuments();
        if (!docs.isEmpty()) {
            String lastId = docs.get(0).getString("id");
            if (lastId != null && lastId.startsWith("store_")) {
                try {
                    int number = Integer.parseInt(lastId.replace("store_", ""));
                    newStoreId = String.format("store_%03d", number + 1);
                } catch (Exception e) {}
            }
        }

        Store store = new Store();
        store.setId(newStoreId);
        store.setName(storeDTO.getName());
        store.setDescription(storeDTO.getDescription());
        store.setAddress(storeDTO.getAddress());
        store.setRating(storeDTO.getRating() != null ? storeDTO.getRating() : 0.0);
        store.setReviewCount(storeDTO.getReviewCount() != null ? storeDTO.getReviewCount() : 0);
        store.setAvtUrl(storeDTO.getAvtUrl());
        store.setBackUrl(storeDTO.getBackUrl());
        store.setIsOpen(false);
        store.setApprovalStatus("pending");
        store.setRejectReason("");
        store.setAdminLockedReason(null);
        store.setDeliveryTime(storeDTO.getDeliveryTime() != null ? storeDTO.getDeliveryTime() : "20-30 phút");
        store.setDeliveryFee(storeDTO.getDeliveryFee() != null ? storeDTO.getDeliveryFee() : 15000.0);
        store.setCategoryIds(storeDTO.getCategoryIds() != null ? storeDTO.getCategoryIds() : new java.util.ArrayList<>());
        
        // Khởi tạo restaurant_categories mặc định nếu chưa có
        if (storeDTO.getRestaurant_categories() != null) {
            store.setRestaurant_categories(storeDTO.getRestaurant_categories());
        } else {
            java.util.Map<String, Object> defaultCats = new java.util.HashMap<>();
            java.util.Map<String, Object> cat1 = new java.util.HashMap<>();
            cat1.put("name", "Món chính");
            cat1.put("order", 1);
            cat1.put("createdAt", java.time.Instant.now().toString());
            cat1.put("updatedAt", java.time.Instant.now().toString());
            defaultCats.put("rest_cate_001", cat1);
            store.setRestaurant_categories(defaultCats);
        }
        store.setLat(storeDTO.getLat() != null ? storeDTO.getLat() : 0.0);
        store.setLng(storeDTO.getLng() != null ? storeDTO.getLng() : 0.0);
        store.setCreatedAt(new java.util.Date());
        store.setUpdatedAt(new java.util.Date());
        storeRepository.saveStore(store);

        com.google.cloud.firestore.DocumentReference merchantRef = firestore.collection("merchant_profiles").document(uid);
        var merchantDoc = merchantRef.get().get();
        if (merchantDoc.exists()) {
            merchantRef.update("storeIds", com.google.cloud.firestore.FieldValue.arrayUnion(newStoreId));
        } else {
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("id", uid);
            data.put("storeIds", java.util.Arrays.asList(newStoreId));
            data.put("createdAt", com.google.cloud.firestore.FieldValue.serverTimestamp());
            merchantRef.set(data);
        }

        return mapToDTO(store);
    }

    public StoreDTO getStoreById(String id) throws Exception {
        Store store = storeRepository.getStoreById(id);
        if (store == null) {
            // Nếu gian hàng chưa tồn tại (trường hợp khởi tạo rỗng), tạo mặc định
            store = new Store();
            store.setId(id);
            store.setName("Cửa hàng mới");
            store.setIsOpen(true);
            storeRepository.saveStore(store);
        }
        return mapToDTO(store);
    }

    public StoreDTO updateStore(String id, StoreDTO storeDTO) throws ExecutionException, InterruptedException {
        Store store = storeRepository.getStoreById(id);
        if (store == null) {
            store = new Store();
            store.setId(id);
        }

        store.setName(storeDTO.getName());
        store.setAddress(storeDTO.getAddress());
        store.setRating(storeDTO.getRating());
        store.setReviewCount(storeDTO.getReviewCount());
        store.setAvtUrl(storeDTO.getAvtUrl());
        store.setBackUrl(storeDTO.getBackUrl());
        if (store.getAdminLockedReason() != null && storeDTO.getIsOpen()) {
            throw new IllegalArgumentException("Cửa hàng đang bị tạm khóa bởi Admin, không thể mở cửa.");
        }
        if (store.getAdminLockedReason() != null) {
            store.setIsOpen(false);
        } else {
            store.setIsOpen(storeDTO.getIsOpen());
        }
        if (storeDTO.getApprovalStatus() != null) {
            store.setApprovalStatus(storeDTO.getApprovalStatus());
        }
        if (storeDTO.getRejectReason() != null) {
            store.setRejectReason(storeDTO.getRejectReason());
        }
        if (storeDTO.getAdminLockedReason() != null) {
            store.setAdminLockedReason(storeDTO.getAdminLockedReason());
        }
        store.setDeliveryTime(storeDTO.getDeliveryTime());
        store.setDeliveryFee(storeDTO.getDeliveryFee());
        store.setCategoryIds(storeDTO.getCategoryIds());
        store.setRestaurant_categories(storeDTO.getRestaurant_categories());
        store.setLat(storeDTO.getLat());
        store.setLng(storeDTO.getLng());
        store.setUpdatedAt(new java.util.Date());

        storeRepository.saveStore(store);
        return mapToDTO(store);
    }

    public List<StoreDTO> getAllStores() throws Exception {
        List<Store> stores = storeRepository.layTatCaStores();
        List<StoreDTO> dtos = new ArrayList<>();
        for (Store store : stores) {
            dtos.add(mapToDTO(store));
        }
        return dtos;
    }

    /**
     * Admin duyet cua hang: cap nhat approvalStatus = "approved", isOpen = true
     */
    public void approveStore(String storeId) throws Exception {
        com.google.cloud.firestore.DocumentReference docRef = firestore.collection("stores").document(storeId);
        com.google.cloud.firestore.DocumentSnapshot doc = docRef.get().get();
        if (!doc.exists()) {
            throw new IllegalArgumentException("Cua hang khong ton tai: " + storeId);
        }
        Map<String, Object> updates = new HashMap<>();
        updates.put("approvalStatus", "approved");
        updates.put("isOpen", true);
        updates.put("updatedAt", new java.util.Date());
        docRef.update(updates).get();
        log.info("Admin da duyet cua hang: {}", storeId);
    }

    /**
     * Admin tu choi cua hang: cap nhat approvalStatus = "rejected", isOpen = false
     */
    public void rejectStore(String storeId, String reason) throws Exception {
        com.google.cloud.firestore.DocumentReference docRef = firestore.collection("stores").document(storeId);
        com.google.cloud.firestore.DocumentSnapshot doc = docRef.get().get();
        if (!doc.exists()) {
            throw new IllegalArgumentException("Cua hang khong ton tai: " + storeId);
        }
        Map<String, Object> updates = new HashMap<>();
        updates.put("approvalStatus", "rejected");
        updates.put("rejectReason", reason != null ? reason : "Khong dat yeu cau");
        updates.put("isOpen", false);
        updates.put("updatedAt", new java.util.Date());
        docRef.update(updates).get();
        log.info("Admin da tu choi cua hang: {} - Ly do: {}", storeId, reason);
    }

    /**
     * Admin tam khoa cua hang: cap nhat adminLockedReason = reason, isOpen = false
     */
    public void lockStore(String storeId, String reason) throws Exception {
        com.google.cloud.firestore.DocumentReference docRef = firestore.collection("stores").document(storeId);
        com.google.cloud.firestore.DocumentSnapshot doc = docRef.get().get();
        if (!doc.exists()) {
            throw new IllegalArgumentException("Cửa hàng không tồn tại: " + storeId);
        }
        Map<String, Object> updates = new HashMap<>();
        updates.put("adminLockedReason", reason != null && !reason.trim().isEmpty() ? reason : "Vi phạm quy định");
        updates.put("isOpen", false);
        updates.put("updatedAt", new java.util.Date());
        docRef.update(updates).get();
        log.info("Admin da tam khoa cua hang: {} - Ly do: {}", storeId, reason);
    }

    /**
     * Admin mo khoa cua hang: xoa adminLockedReason, isOpen = true
     */
    public void unlockStore(String storeId) throws Exception {
        com.google.cloud.firestore.DocumentReference docRef = firestore.collection("stores").document(storeId);
        com.google.cloud.firestore.DocumentSnapshot doc = docRef.get().get();
        if (!doc.exists()) {
            throw new IllegalArgumentException("Cửa hàng không tồn tại: " + storeId);
        }
        Map<String, Object> updates = new HashMap<>();
        updates.put("adminLockedReason", com.google.cloud.firestore.FieldValue.delete());
        updates.put("isOpen", true);
        updates.put("updatedAt", new java.util.Date());
        docRef.update(updates).get();
        log.info("Admin da mo khoa cua hang: {}", storeId);
    }


    private StoreDTO mapToDTO(Store store) {
        StoreDTO dto = new StoreDTO();
        dto.setId(store.getId());
        dto.setName(store.getName());
        dto.setDescription(store.getDescription());
        dto.setAddress(store.getAddress());
        dto.setRating(store.getRating());
        dto.setReviewCount(store.getReviewCount());
        dto.setAvtUrl(store.getAvtUrl());
        dto.setBackUrl(store.getBackUrl());
        dto.setIsOpen(store.getIsOpen());
        dto.setApprovalStatus(store.getApprovalStatus());
        dto.setRejectReason(store.getRejectReason());
        dto.setAdminLockedReason(store.getAdminLockedReason());
        dto.setDeliveryTime(store.getDeliveryTime());
        dto.setDeliveryFee(store.getDeliveryFee());
        dto.setCategoryIds(store.getCategoryIds());
        dto.setRestaurant_categories(store.getRestaurant_categories());
        dto.setLat(store.getLat());
        dto.setLng(store.getLng());
        return dto;
    }

    public Map<String, Object> getNearbyStores(double lat, double lng, double radius, int limit, String categoryId) throws ExecutionException, InterruptedException {
        List<Store> allStores = storeRepository.findOpenStores();

        double earthRadius = 6371000;

        List<NearbyStoreResponse> nearbyStores = new ArrayList<>();
        for (Store store : allStores) {
            if (store.getLat() == null || store.getLng() == null) {
                continue;
            }
            if (categoryId != null && !categoryId.isEmpty()) {
                List<String> cats = store.getCategoryIds();
                if (cats == null || !cats.contains(categoryId)) {
                    continue;
                }
            }

            double dLat = Math.toRadians(store.getLat() - lat);
            double dLng = Math.toRadians(store.getLng() - lng);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                    + Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(store.getLat()))
                    * Math.sin(dLng / 2) * Math.sin(dLng / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double distance = earthRadius * c;

            if (distance <= radius) {
                double displayDistance;
                if (mapboxService.isEnabled()) {
                    double mapboxDist = mapboxService.getRoadDistanceKm(lat, lng, store.getLat(), store.getLng());
                    displayDistance = mapboxDist > 0 ? mapboxDist : Math.round((distance / 1000.0) * 10.0) / 10.0;
                } else {
                    displayDistance = Math.round((distance / 1000.0) * 10.0) / 10.0;
                }

                nearbyStores.add(NearbyStoreResponse.builder()
                        .id(store.getId())
                        .name(store.getName())
                        .address(store.getAddress())
                        .rating(store.getRating())
                        .reviewCount(store.getReviewCount())
                        .avtUrl(store.getAvtUrl())
                        .deliveryTime(store.getDeliveryTime())
                        .deliveryFee(store.getDeliveryFee())
                        .distance(displayDistance)
                        .isOpen(store.getIsOpen())
                        .categoryIds(store.getCategoryIds())
                        .build());
            }
        }

        nearbyStores.sort((a, b) -> Double.compare(a.getDistance(), b.getDistance()));

        int totalInRadius = nearbyStores.size();
        if (nearbyStores.size() > limit) {
            nearbyStores = nearbyStores.subList(0, limit);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("data", nearbyStores);
        result.put("pagination", PaginationInfo.builder()
                .limit(limit)
                .returned(nearbyStores.size())
                .totalInRadius(totalInRadius)
                .build());

        return result;
    }

    public Map<String, Object> getPopularStores(int limit, String categoryId, double minRating, Double lat, Double lng) throws ExecutionException, InterruptedException {
        List<Store> allStores = storeRepository.findOpenStores();

        List<Store> filtered = new ArrayList<>();
        for (Store store : allStores) {
            if (categoryId != null && !categoryId.isEmpty()) {
                List<String> cats = store.getCategoryIds();
                if (cats == null || !cats.contains(categoryId)) {
                    continue;
                }
            }
            if (store.getRating() != null && store.getRating() < minRating) {
                continue;
            }
            filtered.add(store);
        }

        filtered.sort((a, b) -> {
            int cmpReview = Integer.compare(
                    b.getReviewCount() != null ? b.getReviewCount() : 0,
                    a.getReviewCount() != null ? a.getReviewCount() : 0);
            if (cmpReview != 0) return cmpReview;
            return Double.compare(
                    b.getRating() != null ? b.getRating() : 0,
                    a.getRating() != null ? a.getRating() : 0);
        });

        if (filtered.size() > limit) {
            filtered = filtered.subList(0, limit);
        }

        List<PopularStoreResponse> popularStores = new ArrayList<>();
        for (Store store : filtered) {
            Double distance = null;
            if (lat != null && lng != null && store.getLat() != null && store.getLng() != null) {
                double dLat = Math.toRadians(store.getLat() - lat);
                double dLng = Math.toRadians(store.getLng() - lng);
                double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                        + Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(store.getLat()))
                        * Math.sin(dLng / 2) * Math.sin(dLng / 2);
                double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
                double earthRadius = 6371.0;
                double distanceM = earthRadius * c * 1000.0;
                distance = Math.round((distanceM / 1000.0) * 10.0) / 10.0;
            }

            popularStores.add(PopularStoreResponse.builder()
                    .id(store.getId())
                    .name(store.getName())
                    .address(store.getAddress())
                    .rating(store.getRating())
                    .reviewCount(store.getReviewCount())
                    .avtUrl(store.getAvtUrl())
                    .backUrl(store.getBackUrl())
                    .deliveryTime(store.getDeliveryTime())
                    .deliveryFee(store.getDeliveryFee())
                    .distance(distance)
                    .isOpen(store.getIsOpen())
                    .categoryIds(store.getCategoryIds())
                    .build());
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("data", popularStores);
        result.put("pagination", PaginationInfo.builder()
                .limit(limit)
                .returned(popularStores.size())
                .total(popularStores.size())
                .build());

        return result;
    }

    public void taoDriverProfile(String userId, String fullName, String phoneNumber) throws Exception {
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("id", userId);
        profileData.put("fullName", fullName);
        profileData.put("phoneNumber", phoneNumber);
        profileData.put("isActive", true);
        profileData.put("isAvailable", true);
        profileData.put("currentOrderIds", java.util.List.of());
        profileData.put("rating", 5.0);
        profileData.put("totalTrips", 0);
        profileData.put("driverCommissionPercentage", 80.0);
        profileData.put("balance", 0.0);
        profileData.put("createdAt", FieldValue.serverTimestamp());
        profileData.put("updatedAt", FieldValue.serverTimestamp());

        firestore.collection("driver_profiles").document(userId).set(profileData, SetOptions.merge()).get();
        walletRepository.createDriverWallet(userId);

        log.info("Da tao driver profile va wallet cho tai xe: {}", userId);
    }

    public DriverProfileResponse getDriverProfile(String userId) throws Exception {
        log.info("Bat dau lay driver profile cho userId: {}", userId);
        com.google.cloud.firestore.DocumentSnapshot doc = firestore
                .collection("driver_profiles")
                .document(userId)
                .get()
                .get();
        if (!doc.exists()) {
            log.warn("Khong tim thay driver profile cho userId: {}", userId);
            return null;
        }
        return mapToDriverProfileResponse(doc.getData());
    }

    public DriverProfileResponse updateDriverProfile(String userId, UpdateDriverProfileRequest request) throws Exception {
        log.info("Bat dau cap nhat driver profile cho userId: {}", userId);

        com.google.cloud.firestore.DocumentSnapshot doc = firestore
                .collection("driver_profiles")
                .document(userId)
                .get()
                .get();

        if (!doc.exists()) {
            throw new IllegalArgumentException("Khong tim thay tai xe voi ID: " + userId);
        }

        Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("updatedAt", com.google.cloud.firestore.FieldValue.serverTimestamp());

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            updates.put("fullName", request.getFullName().trim());
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            updates.put("phoneNumber", request.getPhoneNumber().trim());
        }
        if (request.getVehiclePlate() != null && !request.getVehiclePlate().isBlank()) {
            updates.put("vehiclePlate", request.getVehiclePlate().trim().toUpperCase());
        }
        if (request.getVehicleType() != null && !request.getVehicleType().isBlank()) {
            updates.put("vehicleType", request.getVehicleType().trim());
        }
        if (request.getDriverLicense() != null && !request.getDriverLicense().isBlank()) {
            updates.put("driverLicense", request.getDriverLicense().trim().toUpperCase());
        }
        if (request.getPhotoUrl() != null && !request.getPhotoUrl().isBlank()) {
            updates.put("photoUrl", request.getPhotoUrl().trim());
        }

        firestore.collection("driver_profiles")
                .document(userId)
                .update(updates)
                .get();

        log.info("Cap nhat driver profile thanh cong cho userId: {}", userId);

        com.google.cloud.firestore.DocumentSnapshot updatedDoc = firestore
                .collection("driver_profiles")
                .document(userId)
                .get()
                .get();

        return mapToDriverProfileResponse(updatedDoc.getData());
    }

    public DriverProfileResponse updateDriverProfileMultipart(
            String userId,
            UpdateDriverProfileRequest request,
            MultipartFile avatarFile) throws Exception {
        log.info("Bat dau cap nhat driver profile (multipart) cho userId: {}", userId);

        com.google.cloud.firestore.DocumentSnapshot doc = firestore
                .collection("driver_profiles")
                .document(userId)
                .get()
                .get();

        if (!doc.exists()) {
            throw new IllegalArgumentException("Khong tim thay tai xe voi ID: " + userId);
        }

        Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("updatedAt", FieldValue.serverTimestamp());

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            updates.put("fullName", request.getFullName().trim());
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            updates.put("phoneNumber", request.getPhoneNumber().trim());
        }
        if (request.getVehiclePlate() != null && !request.getVehiclePlate().isBlank()) {
            updates.put("vehiclePlate", request.getVehiclePlate().trim().toUpperCase());
        }
        if (request.getVehicleType() != null && !request.getVehicleType().isBlank()) {
            updates.put("vehicleType", request.getVehicleType().trim());
        }
        if (request.getDriverLicense() != null && !request.getDriverLicense().isBlank()) {
            updates.put("driverLicense", request.getDriverLicense().trim().toUpperCase());
        }

        if (avatarFile != null && !avatarFile.isEmpty()) {
            String newPhotoUrl = cloudinaryService.uploadAvatar(avatarFile, userId);
            String oldPhotoUrl = null;
            Object existingPhoto = doc.get("photoUrl");
            if (existingPhoto != null) {
                oldPhotoUrl = existingPhoto.toString();
            }
            if (oldPhotoUrl != null && !oldPhotoUrl.isBlank() && oldPhotoUrl.contains("cloudinary.com")) {
                cloudinaryService.deleteAvatar(oldPhotoUrl);
            }
            updates.put("photoUrl", newPhotoUrl);
        }

        firestore.collection("driver_profiles")
                .document(userId)
                .update(updates)
                .get();

        log.info("Cap nhat driver profile (multipart) thanh cong cho userId: {}", userId);

        com.google.cloud.firestore.DocumentSnapshot updatedDoc = firestore
                .collection("driver_profiles")
                .document(userId)
                .get()
                .get();

        return mapToDriverProfileResponse(updatedDoc.getData());
    }

    public String uploadDriverAvatar(String userId, MultipartFile avatar) throws Exception {
        com.google.cloud.firestore.DocumentSnapshot doc = firestore
                .collection("driver_profiles")
                .document(userId)
                .get()
                .get();

        if (!doc.exists()) {
            throw new IllegalArgumentException("Khong tim thay tai xe voi ID: " + userId);
        }

        String newPhotoUrl = cloudinaryService.uploadAvatar(avatar, userId);

        String oldPhotoUrl = null;
        Object existingPhoto = doc.get("photoUrl");
        if (existingPhoto != null) {
            oldPhotoUrl = existingPhoto.toString();
        }
        if (oldPhotoUrl != null && !oldPhotoUrl.isBlank() && oldPhotoUrl.contains("cloudinary.com")) {
            cloudinaryService.deleteAvatar(oldPhotoUrl);
        }

        firestore.collection("driver_profiles")
                .document(userId)
                .update("photoUrl", newPhotoUrl, "updatedAt", FieldValue.serverTimestamp())
                .get();

        log.info("Upload avatar thanh cong cho userId: {}", userId);
        return newPhotoUrl;
    }

    private DriverProfileResponse mapToDriverProfileResponse(Map<String, Object> data) {
        if (data == null) return null;
        return DriverProfileResponse.builder()
                .id(data.get("id") != null ? data.get("id").toString() : null)
                .fullName(data.get("fullName") != null ? data.get("fullName").toString() : null)
                .phoneNumber(data.get("phoneNumber") != null ? data.get("phoneNumber").toString() : null)
                .vehiclePlate(data.get("vehiclePlate") != null ? data.get("vehiclePlate").toString() : null)
                .vehicleType(data.get("vehicleType") != null ? data.get("vehicleType").toString() : null)
                .driverLicense(data.get("driverLicense") != null ? data.get("driverLicense").toString() : null)
                .photoUrl(data.get("photoUrl") != null ? data.get("photoUrl").toString() : null)
                .rating(data.get("rating") != null ? ((Number) data.get("rating")).doubleValue() : null)
                .totalTrips(data.get("totalTrips") != null ? ((Number) data.get("totalTrips")).intValue() : null)
                .isActive(data.get("isActive") != null ? (Boolean) data.get("isActive") : null)
                .isAvailable(data.get("isAvailable") != null ? (Boolean) data.get("isAvailable") : null)
                .build();
    }

    @SuppressWarnings("unchecked")
    public List<String> getStoreIdsByMerchantId(String merchantId) throws ExecutionException, InterruptedException {
        com.google.cloud.firestore.DocumentReference merchantRef = firestore.collection("merchant_profiles").document(merchantId);
        com.google.cloud.firestore.DocumentSnapshot doc = merchantRef.get().get();
        if (doc.exists()) {
            Object storeIdsObj = doc.get("storeIds");
            if (storeIdsObj instanceof List) {
                return (List<String>) storeIdsObj;
            }
        }
        return new ArrayList<>();
    }
}
