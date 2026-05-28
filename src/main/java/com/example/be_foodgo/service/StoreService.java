package com.example.be_foodgo.service;

import com.example.be_foodgo.dto.StoreDTO;
import com.example.be_foodgo.model.Store;
import com.example.be_foodgo.repository.StoreRepository;
import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

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
}
