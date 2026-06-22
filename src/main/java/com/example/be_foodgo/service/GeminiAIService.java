package com.example.be_foodgo.service;

import com.example.be_foodgo.constant.AIPrompt;
import com.example.be_foodgo.dto.GeminiRequest;
import com.example.be_foodgo.dto.GeminiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class GeminiAIService {

    private static final Logger log = LoggerFactory.getLogger(GeminiAIService.class);

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent}")
    private String apiUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RAGContextBuilder ragContextBuilder;

    public String chat(String userMessage, List<String> conversationHistory, String userId) {
        if (apiKey == null || apiKey.isBlank() || apiKey.startsWith("YOUR_")) {
            log.warn("Gemini API key chua duoc cau hinh. Tra ve thong bao.");
            return "He thong AI hien dang duoc bao tri. Vui long thu lai sau.";
        }

        try {
            long startTime = System.currentTimeMillis();
            String dynamicContext = ragContextBuilder.buildContext(userMessage, userId);
            long contextMs = System.currentTimeMillis() - startTime;
            log.info("RAG context fetched in {}ms for userId: '{}', message: '{}'", contextMs, userId, userMessage);

            String systemPrompt = AIPrompt.SYSTEM_INSTRUCTION.replace("{DYNAMIC_CONTEXT}", dynamicContext);

            GeminiRequest request = GeminiRequest.builder()
                    .contents(buildContents(systemPrompt, conversationHistory, userMessage))
                    .generationConfig(GeminiRequest.GenerationConfig.builder()
                            .maxOutputTokens(8192)
                            .temperature(0.7f)
                            .topP(0.95f)
                            .topK(40)
                            .build())
                    .safetySettings(buildSafetySettings())
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<GeminiRequest> entity = new HttpEntity<>(request, headers);

            String url = apiUrl + "?key=" + apiKey;
            log.debug("Calling Gemini API: {}", url);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return parseGeminiResponse(response.getBody());
            }

            log.error("Gemini API tra ve trang thai: {}", response.getStatusCode());
            return "Xin loi, he thong AI gap su co. Vui long thu lai sau.";

        } catch (org.springframework.web.client.RestClientException e) {
            log.error("Loi khi goi Gemini API: {}", e.getMessage(), e);
            return "Da xay ra loi khi xu ly yeu cau cua ban. Vui long thu lai sau.";
        }
    }

    public String recommendFood(String preference, Double budget, Double userLat, Double userLng) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Ban la tro ly goi y mon an cua Food Go.\n\n");
        promptBuilder.append("Khach hang can goi y mon an voi thong tin sau:\n");

        if (preference != null && !preference.isBlank()) {
            promptBuilder.append("- So thich: ").append(preference).append("\n");
        }
        if (budget != null) {
            promptBuilder.append("- Ngan sach: ").append(String.format("%.0f", budget)).append(" VND\n");
        }
        if (userLat != null && userLng != null) {
            promptBuilder.append("- Vi tri: ").append(userLat).append(", ").append(userLng).append("\n");
        }

        promptBuilder.append("""
            \nHay goi y 3-5 mon an phu hop nhat, moi mon bao gom:
            - Ten mon an
            - Gia ca uoc tinh (neu co)
            - Ly do tai sao ban chon mon nay
            - Cuu hang goi y (neu biet)

            Tra loi bang tieng Viet, than thien va huu ich.
            """);

        return chat(promptBuilder.toString(), Collections.emptyList(), null);
    }

    private List<GeminiRequest.Content> buildContents(String systemPrompt, List<String> history, String currentMessage) {
        List<GeminiRequest.Content> contents = new ArrayList<>();

        List<GeminiRequest.Part> systemParts = new ArrayList<>();
        systemParts.add(GeminiRequest.Part.builder().text(systemPrompt).build());
        contents.add(GeminiRequest.Content.builder()
                .parts(systemParts)
                .role("user")
                .build());

        for (int i = 0; i < history.size(); i++) {
            String msg = history.get(i);
            boolean isUser = i % 2 == 0;

            List<GeminiRequest.Part> parts = new ArrayList<>();
            parts.add(GeminiRequest.Part.builder().text(msg).build());

            contents.add(GeminiRequest.Content.builder()
                    .parts(parts)
                    .role(isUser ? "user" : "model")
                    .build());
        }

        List<GeminiRequest.Part> userParts = new ArrayList<>();
        userParts.add(GeminiRequest.Part.builder().text(currentMessage).build());
        contents.add(GeminiRequest.Content.builder()
                .parts(userParts)
                .role("user")
                .build());

        return contents;
    }

    private GeminiRequest.SafetySetting[] buildSafetySettings() {
        return new GeminiRequest.SafetySetting[]{
                GeminiRequest.SafetySetting.builder()
                        .category("HARM_CATEGORY_HARASSMENT").threshold("BLOCK_NONE").build(),
                GeminiRequest.SafetySetting.builder()
                        .category("HARM_CATEGORY_HATE_SPEECH").threshold("BLOCK_NONE").build(),
                GeminiRequest.SafetySetting.builder()
                        .category("HARM_CATEGORY_SEXUALLY_EXPLICIT").threshold("BLOCK_NONE").build(),
                GeminiRequest.SafetySetting.builder()
                        .category("HARM_CATEGORY_DANGEROUS_CONTENT").threshold("BLOCK_NONE").build(),
        };
    }

    private String parseGeminiResponse(String responseBody) {
        try {
            GeminiResponse geminiResponse = objectMapper.readValue(responseBody, GeminiResponse.class);

            if (geminiResponse.getCandidates() != null && !geminiResponse.getCandidates().isEmpty()) {
                GeminiResponse.Candidate candidate = geminiResponse.getCandidates().get(0);

                if ("MAX_TOKENS".equals(candidate.getFinishReason()) ||
                        "RECITATION".equals(candidate.getFinishReason())) {
                    log.warn("Gemini tra loi bi cat hoac co noi dung trung lap: {}", candidate.getFinishReason());
                }

                if (candidate.getContent() != null &&
                        candidate.getContent().getParts() != null &&
                        !candidate.getContent().getParts().isEmpty()) {
                    return candidate.getContent().getParts().get(0).getText();
                }
            }

            if (geminiResponse.getPromptFeedback() != null &&
                    geminiResponse.getPromptFeedback().getSafetyRatings() != null) {
                log.warn("Tin nhan bi chan boi safety filter");
                return "Tin nhan cua ban khong the xu ly. Vui long dien lai noi dung khac.";
            }

            return "Xin loi, khong nhan duoc phan hoi tu AI. Vui long thu lai sau.";

        } catch (java.io.IOException e) {
            log.error("Loi IO khi parse Gemini response: {}", e.getMessage());
            return "Da xay ra loi khi xu ly phan hoi tu AI. Vui long thu lai sau.";
        } catch (RuntimeException e) {
            log.error("Loi runtime khi parse Gemini response: {}", e.getMessage());
            return "Da xay ra loi khi xu ly phan hoi tu AI. Vui long thu lai sau.";
        }
    }
}
