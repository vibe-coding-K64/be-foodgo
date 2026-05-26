package com.example.be_foodgo.service;

import com.example.be_foodgo.dto.NearbyStoreResponse;
import com.example.be_foodgo.dto.PaginationInfo;
import com.example.be_foodgo.dto.PopularStoreResponse;
import com.example.be_foodgo.dto.StoreDTO;
import com.example.be_foodgo.model.Store;
import com.example.be_foodgo.repository.StoreRepository;
import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class StoreService {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private Firestore firestore;

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
        store.setTaxCode(storeDTO.getTaxCode());
        store.setBusinessLicense(storeDTO.getBusinessLicense());
        store.setCoverImageUrl(storeDTO.getCoverImageUrl());
        store.setLogoUrl(storeDTO.getLogoUrl());
        store.setBankName(storeDTO.getBankName());
        store.setBankAccountNumber(storeDTO.getBankAccountNumber());
        store.setAcceptingOrders(true);
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
            store.setAcceptingOrders(true);
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
        store.setDescription(storeDTO.getDescription());
        store.setAddress(storeDTO.getAddress());
        store.setTaxCode(storeDTO.getTaxCode());
        store.setBusinessLicense(storeDTO.getBusinessLicense());
        store.setCoverImageUrl(storeDTO.getCoverImageUrl());
        store.setLogoUrl(storeDTO.getLogoUrl());
        store.setBankName(storeDTO.getBankName());
        store.setBankAccountNumber(storeDTO.getBankAccountNumber());
        store.setAcceptingOrders(storeDTO.isAcceptingOrders());

        storeRepository.saveStore(store);
        return mapToDTO(store);
    }

    private StoreDTO mapToDTO(Store store) {
        StoreDTO dto = new StoreDTO();
        dto.setId(store.getId());
        dto.setName(store.getName());
        dto.setDescription(store.getDescription());
        dto.setAddress(store.getAddress());
        dto.setTaxCode(store.getTaxCode());
        dto.setBusinessLicense(store.getBusinessLicense());
        dto.setCoverImageUrl(store.getCoverImageUrl());
        dto.setLogoUrl(store.getLogoUrl());
        dto.setBankName(store.getBankName());
        dto.setBankAccountNumber(store.getBankAccountNumber());
        dto.setAcceptingOrders(store.isAcceptingOrders());
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
                nearbyStores.add(NearbyStoreResponse.builder()
                        .id(store.getId())
                        .name(store.getName())
                        .address(store.getAddress())
                        .rating(store.getRating())
                        .reviewCount(store.getReviewCount())
                        .avtUrl(store.getAvtUrl())
                        .deliveryTime(store.getDeliveryTime())
                        .deliveryFee(store.getDeliveryFee())
                        .distance(Math.round(distance * 10.0) / 10.0)
                        .isOpen(store.isOpen())
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

    public Map<String, Object> getPopularStores(int limit, String categoryId, double minRating) throws ExecutionException, InterruptedException {
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

        List<PopularStoreResponse> popularStores = filtered.stream().map(store ->
                PopularStoreResponse.builder()
                        .id(store.getId())
                        .name(store.getName())
                        .address(store.getAddress())
                        .rating(store.getRating())
                        .reviewCount(store.getReviewCount())
                        .avtUrl(store.getAvtUrl())
                        .backUrl(store.getBackUrl())
                        .deliveryTime(store.getDeliveryTime())
                        .deliveryFee(store.getDeliveryFee())
                        .isOpen(store.isOpen())
                        .categoryIds(store.getCategoryIds())
                        .build()
        ).collect(Collectors.toList());

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
}
