package com.example.be_foodgo.repository;

import com.example.be_foodgo.model.Voucher;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class VoucherRepository {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION_NAME = "vouchers";

    public String saveVoucher(Voucher voucher) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(voucher.getId());
        ApiFuture<WriteResult> collectionsApiFuture = docRef.set(voucher);
        return collectionsApiFuture.get().getUpdateTime().toString();
    }

    public List<String> getAllVoucherIds() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<String> ids = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            ids.add(doc.getId());
        }
        return ids;
    }

    public Voucher getVoucher(String id) throws ExecutionException, InterruptedException {
        DocumentReference documentReference = firestore.collection(COLLECTION_NAME).document(id);
        ApiFuture<DocumentSnapshot> future = documentReference.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            return document.toObject(Voucher.class);
        }
        return null;
    }

    public List<Voucher> getAllVouchers(String storeId) throws ExecutionException, InterruptedException {
        CollectionReference vouchersRef = firestore.collection(COLLECTION_NAME);
        Query query = vouchersRef;
        if (storeId != null && !storeId.isEmpty()) {
            query = query.whereEqualTo("storeId", storeId);
        }
        ApiFuture<QuerySnapshot> future = query.get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        List<Voucher> voucherList = new ArrayList<>();
        for (QueryDocumentSnapshot document : documents) {
            voucherList.add(document.toObject(Voucher.class));
        }
        return voucherList;
    }

    public String updateVoucher(Voucher voucher) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(voucher.getId());
        ApiFuture<WriteResult> collectionsApiFuture = docRef.set(voucher);
        return collectionsApiFuture.get().getUpdateTime().toString();
    }

    public String deleteVoucher(String id) {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
        docRef.delete();
        return "Voucher with ID " + id + " has been deleted";
    }
}
