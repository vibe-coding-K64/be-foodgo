package com.example.be_foodgo.service;

import com.example.be_foodgo.dto.VoucherDTO;
import com.example.be_foodgo.model.Voucher;
import com.example.be_foodgo.repository.VoucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class VoucherService {

    @Autowired
    private VoucherRepository voucherRepository;

    public String createVoucher(VoucherDTO voucherDTO) throws ExecutionException, InterruptedException {
        Voucher voucher = new Voucher();
        mapDTOToEntity(voucherDTO, voucher);
        voucher.setId(generateNextVoucherId());
        voucher.setCreatedAt(new Date());
        voucher.setUpdatedAt(new Date());
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

    public String updateVoucher(String id, VoucherDTO voucherDTO) throws ExecutionException, InterruptedException {
        Voucher existingVoucher = voucherRepository.getVoucher(id);
        if (existingVoucher != null) {
            mapDTOToEntity(voucherDTO, existingVoucher);
            existingVoucher.setUpdatedAt(new Date());
            return voucherRepository.updateVoucher(existingVoucher);
        }
        return "Voucher not found";
    }

    public String deleteVoucher(String id) {
        return voucherRepository.deleteVoucher(id);
    }

    private void mapDTOToEntity(VoucherDTO dto, Voucher entity) {
        if (dto.getStoreId() != null) entity.setStoreId(dto.getStoreId());
        if (dto.getCode() != null) entity.setCode(dto.getCode());
        entity.setType(dto.getType());
        entity.setValue(dto.getValue());
        entity.setMinOrderValue(dto.getMinOrderValue());
        entity.setLimitCount(dto.getLimitCount());
        entity.setUsedCount(dto.getUsedCount());
        if (dto.getExpiryDate() != null) entity.setExpiryDate(dto.getExpiryDate());
        entity.setIsActive(dto.getIsActive());
    }
}
