package com.example.be_foodgo.service;

import com.example.be_foodgo.dto.VoucherDTO;
import com.example.be_foodgo.dto.VoucherListResponse;
import com.example.be_foodgo.model.MyVoucher;
import com.example.be_foodgo.model.Voucher;
import com.example.be_foodgo.repository.VoucherRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class VoucherService {

    private static final Logger log = LoggerFactory.getLogger(VoucherService.class);

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private StoreService storeService;

    public String createVoucher(VoucherDTO voucherDTO, String userId) throws ExecutionException, InterruptedException {
        String storeId = voucherDTO.getStoreId();

        if (storeId != null && !storeId.isBlank()) {
            List<String> ownedStoreIds = storeService.getStoreIdsByMerchantId(userId);
            if (!ownedStoreIds.contains(storeId)) {
                log.warn("User [{}] khong so huu cua hang [{}] - khong the tao voucher", userId, storeId);
                throw new IllegalArgumentException("Khong co quyen tao voucher cho cua hang nay.");
            }
        }

        Voucher voucher = new Voucher();
        mapDTOToEntity(voucherDTO, voucher);
        voucher.setId(generateNextVoucherId());
        voucher.setCreatedAt(new Date());
        voucher.setUpdatedAt(new Date());
        log.info("User [{}] tao voucher [{}] cho cua hang [{}]", userId, voucher.getId(), storeId);
        return voucherRepository.saveVoucher(voucher);
    }

    private String generateNextVoucherId() throws ExecutionException, InterruptedException {
        List<String> ids = voucherRepository.getAllVoucherIds();
        int maxId = 0;
        for (String id : ids) {
            if (id != null && id.startsWith("voucher_")) {
                try {
                    int num = Integer.parseInt(id.substring(8));
                    if (num > maxId) {
                        maxId = num;
                    }
                } catch (NumberFormatException e) {
                }
            }
        }
        return String.format("voucher_%03d", maxId + 1);
    }

    public Voucher getVoucher(String id) throws ExecutionException, InterruptedException {
        return voucherRepository.getVoucher(id);
    }

    public List<Voucher> getAllVouchers(String storeId) throws ExecutionException, InterruptedException {
        return voucherRepository.getAllVouchers(storeId);
    }

    public VoucherListResponse getAvailableVouchers(String userId, String storeId)
            throws ExecutionException, InterruptedException {
        List<MyVoucher> myVouchers = voucherRepository.getAllMyVouchers(userId);
        List<Voucher> vouchers = voucherRepository.getVouchersByStoreId(storeId);
        List<Voucher> freeshipVouchers = voucherRepository.getFreeshipVouchersByStoreId(storeId);

        return VoucherListResponse.builder()
                .myVouchers(myVouchers)
                .vouchers(vouchers)
                .freeshipVouchers(freeshipVouchers)
                .build();
    }

    public String updateVoucher(String id, VoucherDTO voucherDTO, String userId) throws ExecutionException, InterruptedException {
        Voucher existingVoucher = voucherRepository.getVoucher(id);
        if (existingVoucher == null) {
            return "Voucher not found";
        }

        String voucherStoreId = existingVoucher.getStoreId();
        if (voucherStoreId != null && !voucherStoreId.isBlank()) {
            List<String> ownedStoreIds = storeService.getStoreIdsByMerchantId(userId);
            if (!ownedStoreIds.contains(voucherStoreId)) {
                log.warn("User [{}] khong so huu cua hang [{}] - khong the cap nhat voucher [{}]", userId, voucherStoreId, id);
                throw new IllegalArgumentException("Khong co quyen cap nhat voucher nay.");
            }
        }

        mapDTOToEntity(voucherDTO, existingVoucher);
        existingVoucher.setUpdatedAt(new Date());
        log.info("User [{}] cap nhat voucher [{}]", userId, id);
        return voucherRepository.updateVoucher(existingVoucher);
    }

    public String deleteVoucher(String id, String userId) throws ExecutionException, InterruptedException {
        Voucher existingVoucher = voucherRepository.getVoucher(id);
        if (existingVoucher == null) {
            return "Voucher not found";
        }

        String voucherStoreId = existingVoucher.getStoreId();
        if (voucherStoreId != null && !voucherStoreId.isBlank()) {
            List<String> ownedStoreIds = storeService.getStoreIdsByMerchantId(userId);
            if (!ownedStoreIds.contains(voucherStoreId)) {
                log.warn("User [{}] khong so huu cua hang [{}] - khong the xoa voucher [{}]", userId, voucherStoreId, id);
                throw new IllegalArgumentException("Khong co quyen xoa voucher nay.");
            }
        }

        log.info("User [{}] xoa voucher [{}]", userId, id);
        return voucherRepository.deleteVoucher(id);
    }

    private void mapDTOToEntity(VoucherDTO dto, Voucher entity) {
        if (dto.getStoreId() != null) entity.setStoreId(dto.getStoreId());
        if (dto.getTitle() != null) entity.setTitle(dto.getTitle());
        if (dto.getSubtitle() != null) entity.setSubtitle(dto.getSubtitle());
        if (dto.getCode() != null) entity.setCode(dto.getCode());
        entity.setType(dto.getType());
        entity.setValue(dto.getValue());
        entity.setPointsRequired(dto.getStoreId() != null ? 0 : dto.getPointsRequired());
        if (dto.getImageUrl() != null) entity.setImageUrl(dto.getImageUrl());
        entity.setRemaining(dto.getRemaining());
        if (dto.getTerms() != null) entity.setTerms(dto.getTerms());
        entity.setMinOrderValue(dto.getMinOrderValue());
        entity.setLimitCount(dto.getLimitCount());
        entity.setUsedCount(dto.getUsedCount());
        if (dto.getExpiryDate() != null) entity.setExpiryDate(dto.getExpiryDate());
        entity.setIsActive(dto.getIsActive());
        entity.setIsFreeship(dto.getIsFreeship());
    }
}
