package com.example.be_foodgo.service;

import com.example.be_foodgo.model.ChatMessage;
import com.example.be_foodgo.model.Conversation;
import com.example.be_foodgo.model.Order;
import com.example.be_foodgo.repository.ChatRepository;
import com.example.be_foodgo.repository.OrderRepository;
import com.example.be_foodgo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    private static final String TYPE_TEXT = "TEXT";

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public Conversation getOrCreateConversation(String orderId, String requestUserId, int requestUserRole)
            throws ExecutionException, InterruptedException, TimeoutException {
        Conversation conv = chatRepository.findConversationByOrderId(orderId);
        if (conv != null) {
            return conv;
        }

        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Don hang khong ton tai.");
        }

        String customerId = order.getUserId();
        String driverId = order.getDriverId();

        if (driverId == null || driverId.isBlank()) {
            throw new IllegalStateException("Don hang chua co tai xe nhan.");
        }

        Map<String, Object> customerProfile = userRepository.findById(customerId);
        Map<String, Object> driverProfile = userRepository.findById(driverId);

        String customerName = customerProfile != null ? (String) customerProfile.get("fullName") : "Khach hang";
        String driverName = driverProfile != null ? (String) driverProfile.get("fullName") : "Tai xe";

        Conversation newConv = new Conversation();
        newConv.setOrderId(orderId);
        newConv.setCustomerId(customerId);
        newConv.setCustomerName(customerName);
        newConv.setDriverId(driverId);
        newConv.setDriverName(driverName);
        newConv.setLastMessage("");
        newConv.setLastMessageAt(System.currentTimeMillis());
        newConv.setUnreadCustomer(0);
        newConv.setUnreadDriver(0);
        newConv.setStatus("active");
        newConv.setCreatedAt(System.currentTimeMillis());
        newConv.setUpdatedAt(System.currentTimeMillis());

        String convId = chatRepository.saveConversation(newConv);
        newConv.setId(convId);
        return newConv;
    }

    public ChatMessage sendMessage(String orderId, String senderId, String senderName, int senderRole, String content)
            throws ExecutionException, InterruptedException, TimeoutException {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Noi dung tin nhan khong duoc de trong.");
        }

        Conversation conv = getOrCreateConversation(orderId, senderId, senderRole);

        ChatMessage message = new ChatMessage();
        message.setConversationId(conv.getId());
        message.setSenderId(senderId);
        message.setSenderName(senderName);
        message.setSenderRole(String.valueOf(senderRole));
        message.setContent(content);
        message.setType(TYPE_TEXT);
        message.setRead(false);
        message.setCreatedAt(System.currentTimeMillis());

        String msgId = chatRepository.saveMessage(message);
        message.setId(msgId);

        Map<String, Object> updateFields = Map.of(
                "lastMessage", content.length() > 100 ? content.substring(0, 100) + "..." : content,
                "lastMessageAt", System.currentTimeMillis(),
                "updatedAt", System.currentTimeMillis()
        );
        chatRepository.updateConversation(conv.getId(), updateFields);

        if (senderRole == 1) {
            updateFields = Map.of("unreadDriver", com.google.cloud.firestore.FieldValue.increment(1));
        } else if (senderRole == 2) {
            updateFields = Map.of("unreadCustomer", com.google.cloud.firestore.FieldValue.increment(1));
        }
        chatRepository.updateConversation(conv.getId(), updateFields);

        pushMessageToRecipients(conv, message, senderId);

        return message;
    }

    private void pushMessageToRecipients(Conversation conv, ChatMessage message, String senderId) {
        try {
            String customerDest = "/user/" + conv.getCustomerId() + "/queue/chat";
            String driverDest = "/user/" + conv.getDriverId() + "/queue/chat";
            Map<String, Object> payload = Map.of(
                    "event", "NEW_MESSAGE",
                    "conversationId", conv.getId(),
                    "orderId", conv.getOrderId(),
                    "message", Map.of(
                            "id", message.getId(),
                            "conversationId", message.getConversationId(),
                            "senderId", message.getSenderId(),
                            "senderName", message.getSenderName(),
                            "senderRole", message.getSenderRole(),
                            "content", message.getContent(),
                            "type", message.getType(),
                            "isRead", message.isRead(),
                            "createdAt", message.getCreatedAt()
                    ),
                    "senderId", senderId
            );

            if (!senderId.equals(conv.getCustomerId())) {
                messagingTemplate.convertAndSend(customerDest, (Object) payload);
                log.info("[Chat] Pushed message to customer queue: {}", customerDest);
            }
            if (!senderId.equals(conv.getDriverId())) {
                messagingTemplate.convertAndSend(driverDest, (Object) payload);
                log.info("[Chat] Pushed message to driver queue: {}", driverDest);
            }
        } catch (Exception e) {
            log.error("[Chat] Failed to push message via WebSocket: {}", e.getMessage());
        }
    }

    public List<ChatMessage> getMessages(String conversationId) throws ExecutionException, InterruptedException, TimeoutException {
        if (conversationId == null || conversationId.isBlank()) {
            throw new IllegalArgumentException("conversationId khong hop le.");
        }
        return chatRepository.findMessagesByConversationId(conversationId);
    }

    public List<ChatMessage> getMessages(String conversationId, int limit, Long beforeTimestamp) throws ExecutionException, InterruptedException, TimeoutException {
        if (conversationId == null || conversationId.isBlank()) {
            throw new IllegalArgumentException("conversationId khong hop le.");
        }
        return chatRepository.findMessagesByConversationIdPaginated(conversationId, limit, beforeTimestamp);
    }

    public List<Conversation> getConversations(String userId, int role) throws ExecutionException, InterruptedException, TimeoutException {
        if (role == 1) {
            return chatRepository.findByCustomerId(userId);
        } else if (role == 2) {
            return chatRepository.findByDriverId(userId);
        }
        return List.of();
    }

    public void markAsRead(String conversationId, String userId) throws ExecutionException, InterruptedException, TimeoutException {
        Conversation conv = chatRepository.findConversationById(conversationId);
        if (conv == null) return;

        chatRepository.markMessagesAsRead(conversationId, userId);

        if (userId.equals(conv.getCustomerId())) {
            chatRepository.updateConversation(conversationId, Map.of("unreadCustomer", 0));
        } else if (userId.equals(conv.getDriverId())) {
            chatRepository.updateConversation(conversationId, Map.of("unreadDriver", 0));
        }
    }

    public Conversation getConversationByOrderId(String orderId) throws ExecutionException, InterruptedException, TimeoutException {
        return chatRepository.findConversationByOrderId(orderId);
    }
}
