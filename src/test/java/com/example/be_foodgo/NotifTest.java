package com.example.be_foodgo;

import com.example.be_foodgo.dto.NotificationDTO;
import com.example.be_foodgo.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class NotifTest {

    @Autowired
    private NotificationService notificationService;

    @Test
    public void testNotif() throws Exception {
        NotificationDTO dto = new NotificationDTO();
        dto.setTitle("Test Title");
        dto.setBody("Test Body");
        dto.setType(2);
        
        System.out.println("TESTING notifyMerchantByStoreId...");
        notificationService.notifyMerchantByStoreId("vibe-coding-K64", dto);
        System.out.println("Notify DONE.");

        System.out.println("Fetching notifications for user_001 (merchant)...");
        List<NotificationDTO> notifs = notificationService.getNotificationsByProfile("merchant_profiles", "user_001", null);
        System.out.println("Notifs count for user_001: " + notifs.size());
        
        System.out.println("Fetching notifications for vibe-coding-K64 (merchant)...");
        List<NotificationDTO> notifs2 = notificationService.getNotificationsByProfile("merchant_profiles", "vibe-coding-K64", null);
        System.out.println("Notifs count for vibe-coding-K64: " + notifs2.size());
    }
}
