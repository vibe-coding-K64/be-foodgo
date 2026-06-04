package com.example.be_foodgo;

import com.example.be_foodgo.dto.StoreDTO;
import com.example.be_foodgo.dto.VoucherDTO;
import com.example.be_foodgo.model.Voucher;
import com.example.be_foodgo.service.StoreService;
import com.example.be_foodgo.service.VoucherService;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BeFoodgoApplicationTests {

    @Autowired
    private StoreService storeService;

    @Autowired
    private VoucherService voucherService;

    @Autowired
    private Firestore firestore;

    @Test
    void contextLoads() {
    }

    @Test
    void testCreateSystemVoucher() throws Exception {
        VoucherDTO voucherDTO = new VoucherDTO();
        voucherDTO.setStoreId("system");
        voucherDTO.setCode("TESTSYS" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        voucherDTO.setTitle("Test System Voucher");
        voucherDTO.setSubtitle("Test System Voucher Subtitle");
        voucherDTO.setType(1);
        voucherDTO.setValue(15.0);
        voucherDTO.setPointsRequired(100);
        voucherDTO.setImageUrl("https://placehold.co/400");
        voucherDTO.setRemaining(50);
        voucherDTO.setMinOrderValue(50000.0);
        voucherDTO.setLimitCount(1);
        voucherDTO.setUsedCount(0);
        voucherDTO.setExpiryDate(new Date(System.currentTimeMillis() + 86400000));
        voucherDTO.setIsActive(true);
        voucherDTO.setIsFreeship(false);

        // Save using admin user user_002
        String result = voucherService.createVoucher(voucherDTO, "user_002");
        assertNotNull(result);

        String createdVoucherId = null;
        try {
            java.util.List<Voucher> vouchers = voucherService.getAllVouchers(null);
            Voucher foundVoucher = null;
            for (Voucher v : vouchers) {
                if (voucherDTO.getCode().equals(v.getCode())) {
                    foundVoucher = v;
                    createdVoucherId = v.getId();
                    break;
                }
            }
            assertNotNull(foundVoucher, "Voucher should be found by code");
            assertNull(foundVoucher.getStoreId(), "System voucher storeId should map to null");
            assertEquals(voucherDTO.getTitle(), foundVoucher.getTitle());
        } finally {
            if (createdVoucherId != null) {
                firestore.collection("vouchers").document(createdVoucherId).delete().get();
            }
        }
    }

    @Test
    void testStoreApprovalWorkflow() throws Exception {
        String testMerchantUid = "test_merchant_" + UUID.randomUUID().toString().substring(0, 8);

        StoreDTO storeDTO = new StoreDTO();
        storeDTO.setName("Test Store Approval Workflow");
        storeDTO.setDescription("A test store to verify approval/rejection logic");
        storeDTO.setAddress("123 Test Street");
        storeDTO.setLat(10.7769);
        storeDTO.setLng(106.7009);

        // 1. Create store - should default to pending and isOpen = false
        StoreDTO createdStore = storeService.createMerchantStore(testMerchantUid, storeDTO);
        assertNotNull(createdStore.getId());
        assertEquals("pending", createdStore.getApprovalStatus());
        assertFalse(createdStore.getIsOpen());

        String storeId = createdStore.getId();

        try {
            // Verify in Firestore
            DocumentReference docRef = firestore.collection("stores").document(storeId);
            assertEquals("pending", docRef.get().get().getString("approvalStatus"));
            assertFalse(docRef.get().get().getBoolean("isOpen"));

            // 2. Approve store
            storeService.approveStore(storeId);
            assertEquals("approved", docRef.get().get().getString("approvalStatus"));
            assertTrue(docRef.get().get().getBoolean("isOpen"));

            // 3. Reject store
            storeService.rejectStore(storeId, "Document invalid");
            assertEquals("rejected", docRef.get().get().getString("approvalStatus"));
            assertEquals("Document invalid", docRef.get().get().getString("rejectReason"));
            assertFalse(docRef.get().get().getBoolean("isOpen"));

        } finally {
            // Clean up
            firestore.collection("stores").document(storeId).delete().get();
            firestore.collection("merchant_profiles").document(testMerchantUid).delete().get();
        }
    }
}
