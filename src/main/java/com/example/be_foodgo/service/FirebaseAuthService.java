package com.example.be_foodgo.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FirebaseAuthService {

    private static final Logger log = LoggerFactory.getLogger(FirebaseAuthService.class);

    public String layUidTuIdToken(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            log.warn("Firebase ID token la rong.");
            return null;
        }
        try {
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance(FirebaseApp.getInstance());
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(idToken);
            String uid = decodedToken.getUid();
            log.info("Xac thuc Firebase ID token thanh cong - Firebase UID: {}", uid);
            return uid;
        } catch (FirebaseAuthException e) {
            log.warn("Firebase ID token khong hop le: {} - {}", e.getErrorCode(), e.getMessage());
            return null;
        } catch (Exception e) {
            log.warn("Loi khi xac thuc Firebase ID token: {}", e.getMessage());
            return null;
        }
    }

    public FirebaseToken giaiMaIdToken(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            return null;
        }
        try {
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance(FirebaseApp.getInstance());
            return firebaseAuth.verifyIdToken(idToken);
        } catch (FirebaseAuthException e) {
            log.warn("Firebase ID token khong hop le: {} - {}", e.getErrorCode(), e.getMessage());
            return null;
        } catch (Exception e) {
            log.warn("Loi khi giai ma Firebase ID token: {}", e.getMessage());
            return null;
        }
    }
}
