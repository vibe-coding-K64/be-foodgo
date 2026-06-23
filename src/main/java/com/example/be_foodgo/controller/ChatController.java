package com.example.be_foodgo.controller;

import com.example.be_foodgo.dto.SendMessageRequest;
import com.example.be_foodgo.exception.ApiResponse;
import com.example.be_foodgo.model.ChatMessage;
import com.example.be_foodgo.model.Conversation;
import com.example.be_foodgo.service.ChatService;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chat", description = "API chat giua khach hang va tai xe")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = "*")
public class ChatController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;
    private final Firestore firestore;

    public ChatController(ChatService chatService, Firestore firestore) {
        super(LoggerFactory.getLogger(ChatController.class));
        this.chatService = chatService;
        this.firestore = firestore;
    }

    private int getUserRole(String userId) {
        try {
            var doc = firestore.collection("users").document(userId).get().get();
            if (doc != null && doc.exists()) {
                Object rolesObj = doc.get("roles");
                if (rolesObj instanceof List<?> roles) {
                    for (Object role : roles) {
                        if (role instanceof Number n) {
                            if (n.longValue() == 1L) return 1;
                            if (n.longValue() == 2L) return 2;
                        } else if (role instanceof String s) {
                            try {
                                long val = Long.parseLong(s);
                                if (val == 1L) return 1;
                                if (val == 2L) return 2;
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Khong the lay role cua user {}: {}", userId, e.getMessage());
        }
        return 0;
    }

    private String getUserName(String userId) {
        try {
            var doc = firestore.collection("users").document(userId).get().get();
            if (doc != null && doc.exists()) {
                return doc.getString("fullName");
            }
        } catch (Exception e) {
            log.warn("Khong the lay ten cua user {}: {}", userId, e.getMessage());
        }
        return "Nguoi dung";
    }

    @GetMapping("/conversations")
    @Operation(summary = "Lay danh sach cuoc tro chuyen", description = "Lay danh sach cuoc tro chuyen cua nguoi dung hien tai")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay danh sach thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc")
    })
    public ResponseEntity<?> getConversations(HttpServletRequest httpRequest) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }
        String userId = holder.userId;
        int role = getUserRole(userId);
        try {
            List<Conversation> conversations = chatService.getConversations(userId, role);
            return ResponseEntity.ok(ApiResponse.thatSuccess(conversations, "Lay danh sach cuoc tro chuyen thanh cong."));
        } catch (Exception e) {
            log.error("Loi khi lay danh sach cuoc tro chuyen: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi. Vui long thu lai sau."));
        }
    }

    @GetMapping("/conversations/order/{orderId}")
    @Operation(summary = "Lay cuoc tro chuyen theo don hang", description = "Lay hoac tao cuoc tro chuyen cho don hang")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay cuoc tro chuyen thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Don hang khong ton tai")
    })
    public ResponseEntity<?> getConversationByOrder(
            HttpServletRequest httpRequest,
            @Parameter(description = "ID don hang", required = true)
            @PathVariable("orderId") String orderId) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }
        String userId = holder.userId;
        int role = getUserRole(userId);
        try {
            Conversation conv = chatService.getOrCreateConversation(orderId, userId, role);
            return ResponseEntity.ok(ApiResponse.thatSuccess(conv, "Lay cuoc tro chuyen thanh cong."));
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Loi khi lay cuoc tro chuyen: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.thatError(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Loi khi lay cuoc tro chuyen: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi. Vui long thu lai sau."));
        }
    }

    @GetMapping("/conversations/{conversationId}/messages")
    @Operation(summary = "Lay tin nhan cua cuoc tro chuyen", description = "Lay danh sach tin nhan cua cuoc tro chuyen")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lay tin nhan thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc")
    })
    public ResponseEntity<?> getMessages(
            HttpServletRequest httpRequest,
            @Parameter(description = "ID cuoc tro chuyen", required = true)
            @PathVariable("conversationId") String conversationId,
            @Parameter(description = "So luong tin nhan muon lay (mac dinh 20)")
            @RequestParam(value = "limit", defaultValue = "20") int limit,
            @Parameter(description = "Timestamp (milliseconds) - chi lay tin nhan truoc thoi diem nay")
            @RequestParam(value = "before", required = false) Long before) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }
        String userId = holder.userId;
        try {
            List<ChatMessage> messages;
            if (before != null) {
                messages = chatService.getMessages(conversationId, limit, before);
            } else {
                messages = chatService.getMessages(conversationId, limit, null);
            }
            chatService.markAsRead(conversationId, userId);
            return ResponseEntity.ok(ApiResponse.thatSuccess(messages, "Lay tin nhan thanh cong."));
        } catch (IllegalArgumentException e) {
            log.warn("Loi khi lay tin nhan: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.thatError(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Loi khi lay tin nhan: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi. Vui long thu lai sau."));
        }
    }

    @PostMapping("/send")
    @Operation(summary = "Gui tin nhan", description = "Gui tin nhan trong cuoc tro chuyen cua don hang")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Gui tin nhan thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chua xac thuc"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Noi dung trong")
    })
    public ResponseEntity<?> sendMessage(
            HttpServletRequest httpRequest,
            @RequestBody SendMessageRequest request) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }
        String userId = holder.userId;
        int role = getUserRole(userId);
        String userName = getUserName(userId);
        try {
            ChatMessage message = chatService.sendMessage(
                    request.getOrderId(), userId, userName, role, request.getContent());
            return ResponseEntity.ok(ApiResponse.thatSuccess(message, "Gui tin nhan thanh cong."));
        } catch (IllegalArgumentException e) {
            log.warn("Loi khi gui tin nhan: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.thatError(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Loi khi gui tin nhan: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi. Vui long thu lai sau."));
        }
    }

    @PutMapping("/conversations/{conversationId}/read")
    @Operation(summary = "Danh dau da doc", description = "Danh dau tat ca tin nhan trong cuoc tro chuyen la da doc")
    public ResponseEntity<?> markAsRead(
            HttpServletRequest httpRequest,
            @PathVariable("conversationId") String conversationId) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }
        try {
            chatService.markAsRead(conversationId, holder.userId);
            return ResponseEntity.ok(ApiResponse.thatSuccess(null, "Da danh dau doc."));
        } catch (Exception e) {
            log.error("Loi khi danh dau doc: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi."));
        }
    }

    @Schema(name = "ApiResponseSchema", description = "Schema co ban cho ApiResponse")
    public static class ApiResponseSchema extends ApiResponse<Void> {
    }
}
