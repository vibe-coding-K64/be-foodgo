package com.example.be_foodgo.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.messaging.FirebaseMessaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    private static final String FIREBASE_SERVICE_ACCOUNT_PATH = "firebase-service-account.json";

    @PostConstruct
    public void khoiTaoFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccountStream = new ClassPathResource(FIREBASE_SERVICE_ACCOUNT_PATH).getInputStream();
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                        .build();
                FirebaseApp.initializeApp(options);
                log.info("Khoi tao Firebase thanh cong.");
            } else {
                log.info("Firebase da duoc khoi tao truoc do.");
            }
        } catch (IOException e) {
            log.error("Khong the doc file firebase-service-account.json: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Loi khi khoi tao Firebase: {}", e.getMessage());
        }
    }

    @Bean
    public Firestore firestore() {
        Firestore db = FirestoreClient.getFirestore();
        log.info("Firestore bean da duoc tao thanh cong.");
        return db;
    }

    @Bean
    public FirebaseMessaging firebaseMessaging() {
        FirebaseMessaging instance = FirebaseMessaging.getInstance(FirebaseApp.getInstance());
        log.info("FirebaseMessaging bean da duoc tao thanh cong.");
        return instance;
    }
}
