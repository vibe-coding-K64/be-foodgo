package com.example.be_foodgo.controller;

import com.example.be_foodgo.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Controller
public class ChatWebSocketController {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketController.class);

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat/send")
    public void sendMessage(@Payload Map<String, Object> payload, Principal principal) {
        String senderId = principal != null ? principal.getName() : null;
        if (senderId == null || senderId.isBlank()) {
            log.warn("[ChatWS] Gui tin nhan that bai: principal null");
            return;
        }

        String orderId = payload.get("orderId") != null ? payload.get("orderId").toString() : null;
        String content = payload.get("content") != null ? payload.get("content").toString() : null;

        if (orderId == null || orderId.isBlank() || content == null || content.isBlank()) {
            log.warn("[ChatWS] Gui tin nhan that bai: orderId hoac content trong. orderId={}, contentLen={}",
                    orderId, content != null ? content.length() : "null");
            return;
        }

        log.info("[ChatWS] Gui tin nhan: senderId={}, orderId={}, contentLen={}",
                senderId, orderId, content.length());

        try {
            int role = 0;
            if (senderId != null) {
                role = 2;
            }
            String senderName = senderId;
            var message = chatService.sendMessage(orderId, senderId, senderName, role, content);
            log.info("[ChatWS] Gui tin nhan thanh cong: msgId={}", message.getId());
        } catch (Exception e) {
            log.error("[ChatWS] Loi khi gui tin nhan: {}", e.getMessage());
        }
    }

    @MessageExceptionHandler
    public void handleException(Exception e) {
        log.error("[ChatWS] Exception in chat websocket: {}", e.getMessage());
    }
}
