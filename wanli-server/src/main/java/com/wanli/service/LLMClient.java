package com.wanli.service;

import com.wanli.config.LlmConfig;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Service
public class LLMClient {

    private final WebClient webClient;
    private final LlmConfig config;

    public LLMClient(LlmConfig config) {
        this.config = config;
        this.webClient = WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + config.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public Flux<String> streamChat(String systemPrompt, String userMessage) {
        Map<String, Object> body = Map.of(
            "model", config.getModel(),
            "stream", true,
            "temperature", config.getTemperature(),
            "max_tokens", config.getMaxTokens(),
            "messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userMessage)
            )
        );

        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .filter(data -> !data.equals("[DONE]"))
                .map(this::extractToken);
    }

    private String extractToken(String json) {
        try {
            int contentIdx = json.indexOf("\"content\":\"");
            if (contentIdx == -1) return "";
            contentIdx += 11;
            int endIdx = json.indexOf("\"", contentIdx);
            if (endIdx == -1) return "";
            return json.substring(contentIdx, endIdx)
                    .replace("\\n", "\n")
                    .replace("\\t", "\t")
                    .replace("\\\"", "\"");
        } catch (Exception e) {
            return "";
        }
    }
}
