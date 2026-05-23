package com.example.be_foodgo.controller;

import com.example.be_foodgo.dto.VoucherDTO;
import com.example.be_foodgo.model.Voucher;
import com.example.be_foodgo.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/vouchers")
@CrossOrigin(origins = "*") // Hỗ trợ frontend gọi API
public class VoucherController {

    @Autowired
    private VoucherService voucherService;

    @PostMapping
    public ResponseEntity<String> createVoucher(@RequestBody VoucherDTO voucherDTO) {
        try {
            String updateTime = voucherService.createVoucher(voucherDTO);
            return ResponseEntity.ok(updateTime);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(500).body("Error creating voucher: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Voucher> getVoucher(@PathVariable String id) {
        try {
            Voucher voucher = voucherService.getVoucher(id);
            if (voucher != null) {
                return ResponseEntity.ok(voucher);
            }
            return ResponseEntity.notFound().build();
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Voucher>> getAllVouchers(@RequestParam(required = false) String storeId) {
        try {
            List<Voucher> vouchers = voucherService.getAllVouchers(storeId);
            return ResponseEntity.ok(vouchers);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateVoucher(@PathVariable String id, @RequestBody VoucherDTO voucherDTO) {
        try {
            String updateTime = voucherService.updateVoucher(id, voucherDTO);
            return ResponseEntity.ok(updateTime);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(500).body("Error updating voucher: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteVoucher(@PathVariable String id) {
        String result = voucherService.deleteVoucher(id);
        return ResponseEntity.ok(result);
    }
}
