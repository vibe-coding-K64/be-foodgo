package com.example.be_foodgo.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    private static final String FIREBASE_SERVICE_ACCOUNT_PATH = "firebase-service-account.json";
    private byte[] serviceAccountBytes;

    @PostConstruct
    public void khoiTaoFirebase() {
        try (InputStream rawStream = new ClassPathResource(FIREBASE_SERVICE_ACCOUNT_PATH).getInputStream()) {
            this.serviceAccountBytes = rawStream.readAllBytes();

            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccountStream = new ByteArrayInputStream(serviceAccountBytes);
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                        .setDatabaseUrl("https://food-go-17a5d-default-rtdb.asia-southeast1.firebasedatabase.app")
                        .build();
                FirebaseApp.initializeApp(options);
                log.info("Khoi tao Firebase voi Database URL thanh cong.");
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
        try {
            if (serviceAccountBytes == null || serviceAccountBytes.length == 0) {
                log.error("Service account bytes rong! Doc lai tu file.");
                InputStream rawStream = new ClassPathResource(FIREBASE_SERVICE_ACCOUNT_PATH).getInputStream();
                serviceAccountBytes = rawStream.readAllBytes();
                rawStream.close();
            }
            InputStream serviceAccountStream = new ByteArrayInputStream(serviceAccountBytes);
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccountStream);
            String projectId = extractProjectId();

            FirestoreOptions.Builder builder = FirestoreOptions.newBuilder()
                    .setCredentials(credentials);
            if (projectId != null) {
                builder.setProjectId(projectId);
            }
            Firestore db = builder.build().getService();
            log.info("Firestore bean da duoc tao thanh cong. Project ID: {}",
                    Optional.ofNullable(projectId).orElse("(default)"));
            return db;
        } catch (IOException e) {
            log.error("Khong the doc file firebase-service-account.json: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize Firestore", e);
        } catch (Exception e) {
            log.error("Loi khi tao Firestore bean: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize Firestore", e);
        }
    }

    private String extractProjectId() {
        try {
            String json = new String(serviceAccountBytes, StandardCharsets.UTF_8);
            int idx = json.indexOf("\"project_id\"");
            if (idx >= 0) {
                int start = json.indexOf('"', idx + 13) + 1;
                int end = json.indexOf('"', start);
                return json.substring(start, end);
            }
        } catch (Exception e) {
            log.warn("Khong the doc project_id tu service account: {}", e.getMessage());
        }
        return null;
    }

    @Bean
    public FirebaseMessaging firebaseMessaging() {
        FirebaseMessaging instance = FirebaseMessaging.getInstance(FirebaseApp.getInstance());
        log.info("FirebaseMessaging bean da duoc tao thanh cong.");
        return instance;
    }
}
