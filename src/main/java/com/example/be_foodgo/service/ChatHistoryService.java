package com.example.be_foodgo.service;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class ChatHistoryService {

    private static final Logger log = LoggerFactory.getLogger(ChatHistoryService.class);
    private static final String COLLECTION = "chat_histories";
    private static final int MAX_HISTORY_SIZE = 40;
    private static final int TIMEOUT_SECONDS = 10;

    private final Firestore firestore;

    public ChatHistoryService(Firestore firestore) {
        this.firestore = firestore;
    }

    public List<String> getHistory(String userId) {
        if (userId == null || userId.isBlank()) {
            return Collections.emptyList();
        }
        try {
            DocumentSnapshot snapshot = firestore.collection(COLLECTION)
                    .document(userId)
                    .get()
                    .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (!snapshot.exists()) {
                return new ArrayList<>();
            }

            List<String> messages = (List<String>) snapshot.get("messages");
            return messages != null ? new ArrayList<>(messages) : new ArrayList<>();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Doc lich su chat bi ngat: {}", e.getMessage());
            return new ArrayList<>();
        } catch (ExecutionException | TimeoutException e) {
            log.error("Loi khi doc lich su chat tu Firestore: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public void appendMessage(String userId, String message) {
        if (userId == null || userId.isBlank() || message == null) {
            return;
        }
        try {
            var docRef = firestore.collection(COLLECTION).document(userId);
            DocumentSnapshot snapshot = docRef.get().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            List<String> messages;
            if (snapshot.exists()) {
                messages = (List<String>) snapshot.get("messages");
                if (messages == null) {
                    messages = new ArrayList<>();
                }
            } else {
                messages = new ArrayList<>();
            }

            messages.add(message);
            if (messages.size() > MAX_HISTORY_SIZE) {
                messages = new ArrayList<>(messages.subList(messages.size() - MAX_HISTORY_SIZE, messages.size()));
            }

            Map<String, Object> data = Map.of(
                    "userId", userId,
                    "messages", messages,
                    "updatedAt", System.currentTimeMillis()
            );
            docRef.set(data).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Ghi lich su chat bi ngat: {}", e.getMessage());
        } catch (ExecutionException | TimeoutException e) {
            log.error("Loi khi ghi lich su chat vao Firestore: {}", e.getMessage());
        }
    }

    public void clearHistory(String userId) {
        if (userId == null || userId.isBlank()) {
            return;
        }
        try {
            firestore.collection(COLLECTION)
                    .document(userId)
                    .delete()
                    .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            log.info("Da xoa lich su chat cua userId: '{}'", userId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Xoa lich su chat bi ngat: {}", e.getMessage());
        } catch (ExecutionException | TimeoutException e) {
            log.error("Loi khi xoa lich su chat: {}", e.getMessage());
        }
    }
}
