package com.example.be_foodgo.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.web.client.RestTemplate;
import java.util.List;

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
    private String databaseUrl;

    @Value("${firebase.database.url:#{null}}")
    private String configuredDatabaseUrl;

    @Value("${firebase.storage.bucket:#{null}}")
    private String storageBucket;

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
            return;
        }

        this.databaseUrl = extractDatabaseUrl();

        if (FirebaseApp.getApps().isEmpty()) {
            try {
                InputStream serviceAccountStream = new ByteArrayInputStream(serviceAccountBytes);
                GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccountStream);

                FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder()
                        .setCredentials(credentials);
                if (databaseUrl != null) {
                    optionsBuilder.setDatabaseUrl(databaseUrl);
                }
                FirebaseOptions options = optionsBuilder.build();

                FirebaseApp.initializeApp(options);
                log.info("Khoi tao Firebase thanh cong. Database URL: {}",
                        databaseUrl != null ? databaseUrl : "(chua cau hinh)");
            } catch (Exception e) {
                log.error("Loi khi khoi tao Firebase: {}", e.getMessage());
                return;
            }
        }

        if (databaseUrl != null) {
            try {
                FirebaseDatabase.getInstance();
                log.info("Firebase Realtime Database san sang. URL: {}", databaseUrl);
            } catch (Exception e) {
                log.error("Loi khi khoi tao Realtime Database: {}", e.getMessage());
            }
        } else {
            log.warn("Khong co DatabaseURL - Realtime Database khong hoat dong.");
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

    private String extractDatabaseUrl() {
        if (configuredDatabaseUrl != null && !configuredDatabaseUrl.isBlank()) {
            return configuredDatabaseUrl;
        }
        try {
            String json = new String(serviceAccountBytes, StandardCharsets.UTF_8);
            int idx = json.indexOf("\"database_url\"");
            if (idx >= 0) {
                int start = json.indexOf('"', idx + 15) + 1;
                int end = json.indexOf('"', start);
                String url = json.substring(start, end);
                if (!url.isBlank()) {
                    return url;
                }
            }
        } catch (Exception e) {
            log.warn("Khong the doc database_url tu service account: {}", e.getMessage());
        }
        return null;
    }

    @Bean
    public FirebaseDatabase firebaseDatabase() {
        if (databaseUrl == null) {
            log.warn("FirebaseDatabase URL chua duoc cau hinh - Realtime Database khong hoat dong.");
            return null;
        }
        try {
            return FirebaseDatabase.getInstance();
        } catch (Exception e) {
            log.error("Loi khi lay FirebaseDatabase instance: {}", e.getMessage());
            return null;
        }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging() {
        FirebaseMessaging instance = FirebaseMessaging.getInstance(FirebaseApp.getInstance());
        log.info("FirebaseMessaging bean da duoc tao thanh cong.");
        return instance;
    }

    @Bean
    public String firebaseDatabaseUrl() {
        return databaseUrl;
    }

    @Bean
    public RestTemplate firebaseRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    public Storage firebaseStorage() {
        try {
            if (serviceAccountBytes == null || serviceAccountBytes.length == 0) {
                log.error("Service account bytes rong! Doc lai tu file.");
                InputStream rawStream = new ClassPathResource(FIREBASE_SERVICE_ACCOUNT_PATH).getInputStream();
                serviceAccountBytes = rawStream.readAllBytes();
                rawStream.close();
            }
            InputStream serviceAccountStream = new ByteArrayInputStream(serviceAccountBytes);
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccountStream)
                    .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));
            
            String projectId = extractProjectId();
            StorageOptions.Builder builder = StorageOptions.newBuilder()
                    .setCredentials(credentials);
            if (projectId != null) {
                builder.setProjectId(projectId);
            }
            Storage storage = builder.build().getService();
            log.info("Firebase Storage bean da duoc tao thanh cong. Project ID: {}", 
                    projectId != null ? projectId : "(default)");
            return storage;
        } catch (IOException e) {
            log.error("Khong the doc file firebase-service-account.json de khoi tao Storage: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize Firebase Storage", e);
        }
    }

    @Bean
    public String firebaseStorageBucket() {
        if (storageBucket != null && !storageBucket.isBlank()) {
            return storageBucket;
        }
        String projectId = extractProjectId();
        return projectId != null ? projectId + ".appspot.com" : null;
    }
}
