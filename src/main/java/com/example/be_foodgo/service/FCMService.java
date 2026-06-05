package com.example.be_foodgo.service;

import com.example.be_foodgo.model.User;
import com.example.be_foodgo.repository.UserRepository;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FCMService {

    private static final Logger log = LoggerFactory.getLogger(FCMService.class);

    private final FirebaseMessaging firebaseMessaging;
    private final UserRepository userRepository;
    private final Firestore firestore;

    public FCMService(FirebaseMessaging firebaseMessaging, UserRepository userRepository, Firestore firestore) {
        this.firebaseMessaging = firebaseMessaging;
        this.userRepository = userRepository;
        this.firestore = firestore;
    }

    public void registerFCMToken(String userId, String token) {
        if (userId == null || userId.isBlank() || token == null || token.isBlank()) {
            log.warn("UserId hoac Token khong hop le de dang ky FCM.");
            return;
        }

        try {
            User user = userRepository.timTheoId(userId);
            if (user == null) {
                log.warn("Khong tim thay user voi id={}", userId);
                return;
            }

            List<Integer> roles = user.getRoles();
            if (roles == null || roles.isEmpty()) {
                log.warn("User voi id={} khong co bat ky role nao.", userId);
                return;
            }

            Map<String, Object> update = Map.of("fcmToken", token);

            for (Integer role : roles) {
                String collection = null;
                if (role == 1) {
                    collection = "customer_profiles";
                } else if (role == 2) {
                    collection = "driver_profiles";
                } else if (role == 3) {
                    collection = "merchant_profiles";
                } else if (role == 4) {
                    collection = "admin_profiles";
                }

                if (collection != null) {
                    firestore.collection(collection).document(userId)
                            .set(update, SetOptions.merge()).get();
                    log.info("Da luu fcmToken cho userId={} vao collection={}", userId, collection);
                }
            }
        } catch (Exception e) {
            log.error("Loi khi dang ky FCM token cho userId={}: {}", userId, e.getMessage(), e);
        }
    }

    public void sendToDevice(String fcmToken, String title, String body, Map<String, String> data) {
        if (fcmToken == null || fcmToken.isBlank()) {
            log.warn("FCM token rong, khong the gui push notification.");
            return;
        }

        try {
            Message.Builder messageBuilder = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .setNotification(AndroidNotification.builder()
                                    .setChannelId("driver_orders")
                                    .setPriority(AndroidNotification.Priority.HIGH)
                                    .setSound("default")
                                    .build())
                            .build());

            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            String messageId = firebaseMessaging.send(messageBuilder.build());
            log.info("Push notification gui thanh cong. messageId={}, token={}", messageId, fcmToken.substring(0, Math.min(20, fcmToken.length())) + "...");
        } catch (Exception e) {
            log.error("Loi khi gui push notification: {}", e.getMessage());
        }
    }

    public void sendToDriver(String driverId, String title, String body, Map<String, String> data) {
        if (driverId == null || driverId.isBlank()) {
            log.warn("DriverId rong, khong the gui push notification.");
            return;
        }
        
        Map<String, String> dataMap = (data != null) ? new HashMap<>(data) : new HashMap<>();
        if (!dataMap.containsKey("driverId")) {
            dataMap.put("driverId", driverId);
        }
        
        try {
            var doc = firestore.collection("driver_profiles").document(driverId).get().get();
            if (doc.exists()) {
                String fcmToken = doc.getString("fcmToken");
                if (fcmToken != null && !fcmToken.isBlank()) {
                    sendToDevice(fcmToken, title, body, dataMap);
                    log.info("Gui push notification cho tai xe thanh cong: driverId={}, title={}", driverId, title);
                } else {
                    log.warn("Tai xe driverId={} khong co fcmToken.", driverId);
                }
            } else {
                log.warn("Khong tim thay profile tai xe: driverId={}", driverId);
            }
        } catch (Exception e) {
            log.error("Loi khi gui push cho tai xe: driverId={}, error={}", driverId, e.getMessage());
        }
    }
}
