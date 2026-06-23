package com.example.be_foodgo.service;

import com.example.be_foodgo.dto.SearchResultResponse;
import com.example.be_foodgo.dto.SearchResultResponse.OptionDTO;
import com.example.be_foodgo.dto.SearchResultResponse.OptionGroupDTO;
import com.example.be_foodgo.model.Product;
import com.example.be_foodgo.model.Store;
import com.example.be_foodgo.repository.ProductRepository;
import com.example.be_foodgo.repository.StoreRepository;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private Firestore firestore;

    private static final double BAN_KINH_TraiDat = 6371.0;
    private static final double GIOI_HAN_KHOANG_CACH_KM = 10.0;

    public List<SearchResultResponse> search(String query, Double userLat, Double userLng,
                                            String sortBy, String userId)
            throws ExecutionException, InterruptedException {

        log.info("Bat dau tim kiem - query: '{}', userLat: {}, userLng: {}, sortBy: '{}', userId: '{}'",
                query, userLat, userLng, sortBy, userId);

        if (userId != null && query != null && !query.trim().isEmpty()) {
            luuLichSuTimKiem(userId, query.trim());
        }

        List<Store> tatCaStores = storeRepository.layTatCaStores();
        log.info("Tong so cua hang trong he thong: {}", tatCaStores.size());

        Map<String, Double> khoangCachTheoStore = new HashMap<>();
        List<Store> storesTrongPhamVi = new ArrayList<>();

        for (Store store : tatCaStores) {
            if (store.getLat() == null || store.getLng() == null) {
                continue;
            }
            double khoangCach = tinhKhoangCachHaversine(
                    userLat, userLng, store.getLat(), store.getLng());

            if (khoangCach <= GIOI_HAN_KHOANG_CACH_KM) {
                khoangCachTheoStore.put(store.getId(), khoangCach);
                storesTrongPhamVi.add(store);
            }
        }
        log.info("So cua hang trong pham vi {}km: {}", GIOI_HAN_KHOANG_CACH_KM, storesTrongPhamVi.size());

        List<Product> tatCaProducts = productRepository.layTatCaProducts();
        log.info("Tong so san pham trong he thong: {}", tatCaProducts.size());

        Set<String> storeIdsTrongPhamVi = storesTrongPhamVi.stream()
                .map(Store::getId)
                .collect(Collectors.toSet());

        Map<String, String> storeNameMap = storesTrongPhamVi.stream()
                .collect(Collectors.toMap(
                        Store::getId,
                        Store::getName,
                        (existing, replacement) -> existing));

        String queryLowercase = xuLyChuoiTimKiem(query);
        final Set<String> finalStoreIdsTrongPhamVi = storeIdsTrongPhamVi;
        List<Product> productsLoc = tatCaProducts.stream()
                .filter(p -> finalStoreIdsTrongPhamVi.contains(p.getStoreId()))
                .filter(p -> {
                    if (query == null || query.trim().isEmpty()) {
                        return true;
                    }
                    String productNameLowercase = xuLyChuoiTimKiem(p.getName());
                    String storeNameLowercase = xuLyChuoiTimKiem(
                            storeNameMap.getOrDefault(p.getStoreId(), ""));

                    boolean khopTenMon = productNameLowercase.contains(queryLowercase);
                    boolean khopTenCuaHang = storeNameLowercase.contains(queryLowercase);

                    return khopTenMon || khopTenCuaHang;
                })
                .collect(Collectors.toList());

        log.info("So san pham sau khi loc: {}", productsLoc.size());

        List<SearchResultResponse> ketQua = productsLoc.stream()
                .map(p -> {
                    String storeId = p.getStoreId();
                    Store storeGoc = storesTrongPhamVi.stream()
                            .filter(s -> s.getId().equals(storeId))
                            .findFirst()
                            .orElse(null);

                    String storeName = storeGoc != null ? storeGoc.getName() : "";
                    Double rating = storeGoc != null ? storeGoc.getRating() : 0.0;
                    Integer reviewCount = storeGoc != null ? storeGoc.getReviewCount() : 0;
                    Double distance = khoangCachTheoStore.getOrDefault(storeId, null);

                    List<OptionGroupDTO> optionGroupDTOs = null;
                    if (p.getOptionGroups() != null) {
                        optionGroupDTOs = p.getOptionGroups().stream()
                                .map(og -> OptionGroupDTO.builder()
                                        .name(og.getName())
                                        .isSingleSelect(og.getIsSingleSelect())
                                        .isSingleSelect(og.getIsSingleSelect())
                                        .options(og.getOptions() != null
                                                ? og.getOptions().stream()
                                                        .map(o -> OptionDTO.builder()
                                                                .name(o.getName())
                                                                .price(o.getPrice())
                                                                .build())
                                                        .collect(Collectors.toList())
                                                : null)
                                        .build())
                                .collect(Collectors.toList());
                    }

                    return SearchResultResponse.builder()
                            .productId(p.getId())
                            .productName(p.getName())
                            .storeId(storeId)
                            .storeName(storeName)
                            .price(p.getBasePrice())
                            .rating(rating)
                            .reviewCount(reviewCount)
                            .distance(distance)
                            .imageUrl(p.getImageUrl())
                            .optionGroups(optionGroupDTOs)
                            .build();
                })
                .collect(Collectors.toList());

        if (ketQua.isEmpty()) {
            log.info("Khong tim thay san pham nao phu hop voi tu khoa '{}'", query);
            return ketQua;
        }

        sapXepKetQua(ketQua, sortBy);
        log.info("Tim kiem hoan tat - tra ve {} ket qua", ketQua.size());
        return ketQua;
    }

    private void luuLichSuTimKiem(String userId, String query) {
        try {
            String queryNormalized = xuLyChuoiTimKiem(query);
            log.info("[DEBUG] bat dau kiem tra trung - userId: '{}', query: '{}', queryNormalized: '{}'",
                    userId, query, queryNormalized);

            firestore.runTransaction(transaction -> {
                com.google.cloud.firestore.CollectionReference historyRef = firestore
                        .collection("users")
                        .document(userId)
                        .collection("search_history");

                com.google.cloud.firestore.QuerySnapshot snapshot = transaction
                        .get(historyRef.whereEqualTo("keywordNormalized", queryNormalized).limit(1))
                        .get();

                if (!snapshot.getDocuments().isEmpty()) {
                    log.info("[DEBUG] tim thay doc trung trong transaction - id: {}, skip saving",
                            snapshot.getDocuments().get(0).getId());
                    return null;
                }

                DocumentReference docRef = historyRef.document();
                Map<String, Object> data = new HashMap<>();
                data.put("keyword", query);
                data.put("keywordNormalized", queryNormalized);
                data.put("createdAt", com.google.cloud.firestore.FieldValue.serverTimestamp());
                transaction.set(docRef, data);
                log.info("[DEBUG] da luu trong transaction - userId: {}, query: '{}', historyId: {}",
                        userId, query, docRef.getId());
                return null;
            });
            log.info("Da luu lich su tim kiem - userId: {}, query: '{}'", userId, query);
        } catch (Exception e) {
            log.warn("Khong the luu lich su tim kiem - userId: {}, query: '{}', loi: {}",
                    userId, query, e.getMessage());
        }
    }

    private double tinhKhoangCachHaversine(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return BAN_KINH_TraiDat * c;
    }

    private String xuLyChuoiTimKiem(String input) {
        if (input == null) {
            return "";
        }
        String result = input.toLowerCase().trim();
        result = Normalizer.normalize(result, Normalizer.Form.NFD);
        result = DAU_TIENG_VIET_PATTERN.matcher(result).replaceAll("");
        return result;
    }

    private static final Pattern DAU_TIENG_VIET_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private void sapXepKetQua(List<SearchResultResponse> ketQua, String sortBy) {
        if (ketQua == null || ketQua.isEmpty() || sortBy == null || sortBy.trim().isEmpty()) {
            return;
        }
        switch (sortBy) {
            case "priceAsc":
                ketQua.sort(Comparator.comparingDouble(SearchResultResponse::getPrice));
                log.info("Da sap xep theo gia tang dan");
                break;
            case "priceDesc":
                ketQua.sort(Comparator.comparingDouble(SearchResultResponse::getPrice).reversed());
                log.info("Da sap xep theo gia giam dan");
                break;
            case "ratingDesc":
                ketQua.sort(Comparator.<SearchResultResponse>comparingDouble(
                        r -> r.getRating() != null ? r.getRating() : 0.0).reversed());
                log.info("Da sap xep theo danh gia giam dan");
                break;
            default:
                log.info("Khong nhan dang tham so sap xep '{}', bo qua sap xep", sortBy);
        }
    }
}
