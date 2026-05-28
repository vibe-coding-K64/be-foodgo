package com.example.be_foodgo.service;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class FCMService {

    private static final Logger log = LoggerFactory.getLogger(FCMService.class);

    private final FirebaseMessaging firebaseMessaging;

    public FCMService(FirebaseMessaging firebaseMessaging) {
        this.firebaseMessaging = firebaseMessaging;
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
        data = (data != null) ? data : Map.of();
        if (!data.containsKey("driverId")) {
            data.put("driverId", driverId);
        }
        log.info("Gui push notification cho tai xe: driverId={}, title={}", driverId, title);
    }
}
