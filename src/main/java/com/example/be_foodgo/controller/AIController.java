package com.example.be_foodgo.controller;

import com.example.be_foodgo.dto.AIChatRequest;
import com.example.be_foodgo.dto.AIRecommendRequest;
import com.example.be_foodgo.exception.ApiResponse;
import com.example.be_foodgo.service.ChatHistoryService;
import com.example.be_foodgo.service.GeminiAIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
@Tag(name = "AI Chatbot", description = "API tro ly AI goi y mon an cho khach hang")
public class AIController {

    private static final Logger log = LoggerFactory.getLogger(AIController.class);

    private final GeminiAIService geminiAIService;
    private final ChatHistoryService chatHistoryService;

    @Autowired
    public AIController(GeminiAIService geminiAIService, ChatHistoryService chatHistoryService) {
        this.geminiAIService = geminiAIService;
        this.chatHistoryService = chatHistoryService;
    }

    @PostMapping("/chat")
    @Operation(
            summary = "Chat voi tro ly AI",
            description = "Gui tin nhan den tro ly AI va nhan phan hoi. Luu lich su cuoc tro chuyen vao Firestore."
    )
    public ResponseEntity<ApiResponse<String>> chat(
            @RequestBody AIChatRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId
    ) {
        if (request == null || request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.thatError(400, "Tin nhan khong duoc de trong.")
            );
        }

        String resolvedUserId = resolveUserId(userId);
        String message = request.getMessage().trim();

        log.info("AI Chat request - userId: '{}', message: '{}'", resolvedUserId, message);

        List<String> history = chatHistoryService.getHistory(resolvedUserId);

        String response = geminiAIService.chat(message, history, resolvedUserId);

        chatHistoryService.appendMessage(resolvedUserId, "User: " + message);
        chatHistoryService.appendMessage(resolvedUserId, "AI: " + response);

        return ResponseEntity.ok(ApiResponse.thatSuccess(response, "Phan hoi tu tro ly AI"));
    }

    @PostMapping("/recommend")
    @Operation(
            summary = "Goi y mon an",
            description = "Goi y mon an dua tren so thich, ngan sach va vi tri cua khach hang."
    )
    public ResponseEntity<ApiResponse<String>> recommendFood(
            @RequestBody AIRecommendRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId
    ) {
        String resolvedUserId = resolveUserId(userId);

        log.info("AI Recommend request - userId: '{}', preference: '{}', budget: {}, lat: {}, lng: {}",
                resolvedUserId, request.getPreference(), request.getBudget(),
                request.getLat(), request.getLng());

        String recommendation = geminiAIService.recommendFood(
                request.getPreference(),
                request.getBudget(),
                request.getLat(),
                request.getLng()
        );

        return ResponseEntity.ok(ApiResponse.thatSuccess(recommendation, "Goi y mon an tu tro ly AI"));
    }

    @DeleteMapping("/chat/clear")
    @Operation(
            summary = "Xoa lich su chat",
            description = "Xoa toan bo lich su cuoc tro chuyen cua nguoi dung tren Firestore."
    )
    public ResponseEntity<ApiResponse<String>> clearChatHistory(
            @RequestHeader(value = "X-User-Id", required = false) String userId
    ) {
        String resolvedUserId = resolveUserId(userId);
        chatHistoryService.clearHistory(resolvedUserId);

        return ResponseEntity.ok(ApiResponse.thatSuccess("Da xoa lich su chat thanh cong.", "Xoa lich su"));
    }

    @GetMapping("/health")
    @Operation(
            summary = "Kiem tra trang thai AI",
            description = "Kiem tra xem he thong AI co dang hoat dong khong."
    )
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.thatSuccess("Tro ly AI Food Go dang san sang!", "Trang thai"));
    }

    private String resolveUserId(String userId) {
        return (userId != null && !userId.isBlank()) ? userId : "anonymous";
    }
}
