package com.chat.chat.common.ai;

import lombok.RequiredArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Claude API 연동 서비스
 * 채팅방별 토큰 사용량을 추적해 한도를 넘으면 응답을 차단합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClaudeChatService {

    private final WebClient claudeWebClient;

    @Value("${claude.model:claude-3-5-sonnet-20240620}")
    private String model;

    @Value("${claude.max-tokens:512}")
    private Integer maxTokens;

    @Value("${claude.temperature:0.3}")
    private Double temperature;

    @Value("${claude.system-prompt:친절한 한국어 어시스턴트}")
    private String systemPrompt;

    @Value("${claude.token-limit-per-room:2000}")
    private Integer tokenLimitPerRoom;

    @Value("${claude.timeout-seconds:10}")
    private Long timeoutSeconds;

    private final Map<UUID, AtomicInteger> usageByRoom = new ConcurrentHashMap<>();
    private final Set<UUID> limitNotifiedRooms = ConcurrentHashMap.newKeySet();

    /**
     * 사용자 메시지를 Claude에 전달하고 응답을 반환합니다.
     */
    public ReplyResult requestReply(UUID chatRoomId, String userMessage) {
        AtomicInteger counter = usageByRoom.computeIfAbsent(chatRoomId, id -> new AtomicInteger(0));
        int currentUsage = counter.get();
        if (currentUsage >= tokenLimitPerRoom) {
            boolean limitJustReached = limitNotifiedRooms.add(chatRoomId);
            return ReplyResult.limitReached(currentUsage, tokenLimitPerRoom, limitJustReached);
        }

        Map<String, Object> payload = buildPayload(userMessage);

        try {
            ClaudeApiResponse apiResponse = claudeWebClient.post()
                    .uri("/messages")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(ClaudeApiResponse.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();

            if (apiResponse == null) {
                log.warn("[CLAUDE] 응답이 비어 있습니다.");
                return ReplyResult.failure(currentUsage);
            }

            String replyText = extractText(apiResponse);
            int usedTokens = resolveTokens(apiResponse, userMessage, replyText);
            int updatedUsage = counter.addAndGet(usedTokens);
            boolean limitReached = updatedUsage >= tokenLimitPerRoom;
            boolean limitJustReached = limitReached && limitNotifiedRooms.add(chatRoomId);

            return ReplyResult.success(replyText, updatedUsage, tokenLimitPerRoom, limitReached, limitJustReached);
        } catch (WebClientResponseException e) {
            log.error("[CLAUDE] API 호출 실패: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            return ReplyResult.failure(currentUsage);
        } catch (Exception e) {
            log.error("[CLAUDE] API 호출 중 예외 발생: {}", e.getMessage(), e);
            return ReplyResult.failure(currentUsage);
        }
    }

    public int getTokenLimitPerRoom() {
        return tokenLimitPerRoom;
    }

    private Map<String, Object> buildPayload(String userMessage) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", model);
        payload.put("max_tokens", maxTokens);
        payload.put("temperature", temperature);
        payload.put("system", systemPrompt);
        payload.put("messages", List.of(Map.of(
                "role", "user",
                "content", userMessage
        )));
        return payload;
    }

    private String extractText(ClaudeApiResponse response) {
        if (response.getContent() == null || response.getContent().isEmpty()) {
            return "";
        }
        return response.getContent().stream()
                .filter(block -> "text".equalsIgnoreCase(block.getType()))
                .map(ClaudeContentBlock::getText)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("");
    }

    private int resolveTokens(ClaudeApiResponse response, String userMessage, String replyText) {
        ClaudeUsage usage = response.getUsage();
        if (usage != null && usage.getInputTokens() != null && usage.getOutputTokens() != null) {
            return usage.getInputTokens() + usage.getOutputTokens();
        }
        // 대략적인 토큰 추정 (문자 수 / 4)
        int estimate = ((userMessage != null ? userMessage.length() : 0) +
                (replyText != null ? replyText.length() : 0)) / 4 + 1;
        return Math.max(estimate, 1);
    }

    /**
     * Claude API 응답 DTO
     */
    @lombok.Data
    private static class ClaudeApiResponse {
        private List<ClaudeContentBlock> content;
        private ClaudeUsage usage;
    }

    /**
     * Claude 응답 본문 블록
     */
    @lombok.Data
    private static class ClaudeContentBlock {
        private String type;
        private String text;
    }

    /**
     * Claude 토큰 사용량
     */
    @lombok.Data
    private static class ClaudeUsage {
        @com.fasterxml.jackson.annotation.JsonProperty("input_tokens")
        private Integer inputTokens;
        @com.fasterxml.jackson.annotation.JsonProperty("output_tokens")
        private Integer outputTokens;
    }

    /**
     * 응답 결과
     */
    @Getter
    @AllArgsConstructor
    public static class ReplyResult {
        private final boolean hasReply;
        private final String replyText;
        private final int totalTokensUsed;
        private final int tokenLimit;
        private final boolean limitReached;
        private final boolean limitJustReached;

        public static ReplyResult success(String replyText, int totalTokensUsed, int tokenLimit,
                                          boolean limitReached, boolean limitJustReached) {
            return new ReplyResult(true, replyText, totalTokensUsed, tokenLimit, limitReached, limitJustReached);
        }

        public static ReplyResult limitReached(int totalTokensUsed, int tokenLimit, boolean limitJustReached) {
            return new ReplyResult(false, null, totalTokensUsed, tokenLimit, true, limitJustReached);
        }

        public static ReplyResult failure(int totalTokensUsed) {
            return new ReplyResult(false, null, totalTokensUsed, 0, false, false);
        }
    }
}
