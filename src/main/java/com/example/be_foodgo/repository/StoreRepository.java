package com.example.be_foodgo.repository;

import com.example.be_foodgo.model.Store;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ExecutionException;

@Repository
public class StoreRepository {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION_NAME = "stores";

    public Store getStoreById(String id) throws ExecutionException, InterruptedException {
        DocumentReference documentReference = firestore.collection(COLLECTION_NAME).document(id);
        ApiFuture<DocumentSnapshot> future = documentReference.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            Store store = document.toObject(Store.class);
            if (store != null) {
                store.setId(document.getId());
            }
            return store;
        }
        return null;
    }

    public String saveStore(Store store) throws ExecutionException, InterruptedException {
        if (store.getId() == null || store.getId().isEmpty()) {
            // Không nên xảy ra, do gian hàng có ID cứng, nhưng cứ check an toàn
            DocumentReference addedDocRef = firestore.collection(COLLECTION_NAME).document();
            store.setId(addedDocRef.getId());
            ApiFuture<WriteResult> collectionsApiFuture = addedDocRef.set(store);
            return collectionsApiFuture.get().getUpdateTime().toString();
        } else {
            ApiFuture<WriteResult> collectionsApiFuture = firestore.collection(COLLECTION_NAME).document(store.getId()).set(store);
            return collectionsApiFuture.get().getUpdateTime().toString();
        }
    }
}
