package com.example.be_foodgo.repository;

import com.example.be_foodgo.model.ChatMessage;
import com.example.be_foodgo.model.Conversation;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Repository
public class ChatRepository {

    private static final String CONVERSATIONS_COL = "conversations";
    private static final String MESSAGES_COL = "messages";
    private static final int TIMEOUT_SECONDS = 10;

    @Autowired
    private Firestore firestore;

    public String saveConversation(Conversation conv) throws ExecutionException, InterruptedException {
        ApiFuture<DocumentReference> ref = firestore.collection(CONVERSATIONS_COL).add(conv);
        return ref.get().getId();
    }

    public void updateConversation(String convId, Map<String, Object> fields) throws ExecutionException, InterruptedException, TimeoutException {
        firestore.collection(CONVERSATIONS_COL).document(convId).update(fields).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    public Conversation findConversationById(String convId) throws ExecutionException, InterruptedException, TimeoutException {
        DocumentSnapshot doc = firestore.collection(CONVERSATIONS_COL).document(convId).get().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!doc.exists()) return null;
        return documentToConversation(doc);
    }

    public Conversation findConversationByOrderId(String orderId) throws ExecutionException, InterruptedException, TimeoutException {
        QuerySnapshot snap = firestore.collection(CONVERSATIONS_COL)
                .whereEqualTo("orderId", orderId)
                .limit(1)
                .get()
                .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (snap.isEmpty()) return null;
        return documentToConversation(snap.getDocuments().get(0));
    }

    public List<Conversation> findByCustomerId(String customerId) throws ExecutionException, InterruptedException, TimeoutException {
        QuerySnapshot snap = firestore.collection(CONVERSATIONS_COL)
                .whereEqualTo("customerId", customerId)
                .orderBy("lastMessageAt", Query.Direction.DESCENDING)
                .get()
                .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        List<Conversation> result = new ArrayList<>();
        for (DocumentSnapshot doc : snap.getDocuments()) {
            Conversation c = documentToConversation(doc);
            if (c != null) result.add(c);
        }
        return result;
    }

    public List<Conversation> findByDriverId(String driverId) throws ExecutionException, InterruptedException, TimeoutException {
        QuerySnapshot snap = firestore.collection(CONVERSATIONS_COL)
                .whereEqualTo("driverId", driverId)
                .orderBy("lastMessageAt", Query.Direction.DESCENDING)
                .get()
                .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        List<Conversation> result = new ArrayList<>();
        for (DocumentSnapshot doc : snap.getDocuments()) {
            Conversation c = documentToConversation(doc);
            if (c != null) result.add(c);
        }
        return result;
    }

    public String saveMessage(ChatMessage msg) throws ExecutionException, InterruptedException {
        ApiFuture<DocumentReference> ref = firestore.collection(MESSAGES_COL).add(msg);
        return ref.get().getId();
    }

    public List<ChatMessage> findMessagesByConversationId(String conversationId) throws ExecutionException, InterruptedException, TimeoutException {
        QuerySnapshot snap = firestore.collection(MESSAGES_COL)
                .whereEqualTo("conversationId", conversationId)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get()
                .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        List<ChatMessage> result = new ArrayList<>();
        for (DocumentSnapshot doc : snap.getDocuments()) {
            ChatMessage m = documentToMessage(doc);
            if (m != null) result.add(m);
        }
        return result;
    }

    public List<ChatMessage> findMessagesByConversationIdPaginated(String conversationId, int limit, Long beforeTimestamp) throws ExecutionException, InterruptedException, TimeoutException {
        com.google.cloud.firestore.Query query = firestore.collection(MESSAGES_COL)
                .whereEqualTo("conversationId", conversationId)
                .orderBy("createdAt", Query.Direction.DESCENDING);

        if (beforeTimestamp != null) {
            query = query.whereLessThan("createdAt", beforeTimestamp);
        }

        QuerySnapshot snap = query
                .limit(limit)
                .get()
                .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        List<ChatMessage> result = new ArrayList<>();
        for (DocumentSnapshot doc : snap.getDocuments()) {
            ChatMessage m = documentToMessage(doc);
            if (m != null) result.add(m);
        }
        java.util.Collections.reverse(result);
        return result;
    }

    public void markMessagesAsRead(String conversationId, String readerId) throws ExecutionException, InterruptedException, TimeoutException {
        QuerySnapshot snap = firestore.collection(MESSAGES_COL)
                .whereEqualTo("conversationId", conversationId)
                .whereEqualTo("isRead", false)
                .get()
                .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        for (DocumentSnapshot doc : snap.getDocuments()) {
            ChatMessage m = documentToMessage(doc);
            if (m != null && !m.getSenderId().equals(readerId)) {
                doc.getReference().update("isRead", true).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            }
        }
    }

    public void deleteConversation(String convId) throws ExecutionException, InterruptedException, TimeoutException {
        firestore.collection(CONVERSATIONS_COL).document(convId).delete().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    private Conversation documentToConversation(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;
        Conversation c = new Conversation();
        c.setId(doc.getId());
        c.setOrderId(doc.getString("orderId"));
        c.setCustomerId(doc.getString("customerId"));
        c.setCustomerName(doc.getString("customerName"));
        c.setDriverId(doc.getString("driverId"));
        c.setDriverName(doc.getString("driverName"));
        c.setLastMessage(doc.getString("lastMessage"));
        c.setLastMessageAt(toTimestampMillis(doc.get("lastMessageAt")));
        c.setUnreadCustomer(doc.getLong("unreadCustomer") != null ? doc.getLong("unreadCustomer").intValue() : 0);
        c.setUnreadDriver(doc.getLong("unreadDriver") != null ? doc.getLong("unreadDriver").intValue() : 0);
        c.setStatus(doc.getString("status"));
        c.setCreatedAt(toTimestampMillis(doc.get("createdAt")));
        c.setUpdatedAt(toTimestampMillis(doc.get("updatedAt")));
        return c;
    }

    private long toTimestampMillis(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number) return ((Number) value).longValue();
        if (value instanceof com.google.cloud.Timestamp) {
            return ((com.google.cloud.Timestamp) value).toDate().getTime();
        }
        return 0L;
    }

    private ChatMessage documentToMessage(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;
        ChatMessage m = new ChatMessage();
        m.setId(doc.getId());
        m.setConversationId(doc.getString("conversationId"));
        m.setSenderId(doc.getString("senderId"));
        m.setSenderName(doc.getString("senderName"));
        m.setSenderRole(doc.getString("senderRole"));
        m.setContent(doc.getString("content"));
        m.setType(doc.getString("type"));
        m.setRead(doc.getBoolean("isRead") != null ? doc.getBoolean("isRead") : false);
        m.setCreatedAt(toTimestampMillis(doc.get("createdAt")));
        return m;
    }
}
