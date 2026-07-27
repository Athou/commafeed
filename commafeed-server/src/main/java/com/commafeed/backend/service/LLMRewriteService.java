package com.commafeed.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class LLMRewriteService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, String> responseCache = new ConcurrentHashMap<>();

    public String generateAlternative(String originalContent, String target, String prompt) {
        String cacheKey =
                Integer.toHexString((originalContent + "|" + target + "|" + prompt).hashCode());

        if (responseCache.containsKey(cacheKey)) {
            log.info("[LLM_CACHE_HIT] target={}, cacheKey={}", target, cacheKey);
            return responseCache.get(cacheKey);
        }

        String apiKey = System.getProperty("LLM_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = System.getenv("LLM_API_KEY");
        }

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("LLM_API_KEY is not configured");
        }

        String apiUrl =
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash-lite:generateContent";
        String systemPrompt =
                String.format(
                        "You are an expert editor. Rewrite the following article %s based on this instruction: '%s'. Return ONLY the rewritten text without explanations.\n\nOriginal Content:\n%s",
                        target, prompt, originalContent);

        long startTime = System.currentTimeMillis();
        try {
            String requestBody =
                    String.format(
                            """
                    {
                      "contents": [{
                        "parts": [{"text": %s}]
                      }]
                    }
                    """,
                            objectMapper.valueToTree(systemPrompt));

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(apiUrl))
                            .header("Content-Type", "application/json")
                            .header("x-goog-api-key", apiKey.trim())
                            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                            .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long duration = System.currentTimeMillis() - startTime;

            if (response.statusCode() != 200) {
                log.error(
                        "[LLM_ERROR] provider=Gemini status={} durationMs={}",
                        response.statusCode(),
                        duration);
                throw new RuntimeException(
                        "Gemini API returned status "
                                + response.statusCode()
                                + ": "
                                + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());
            String result =
                    root.path("candidates")
                            .get(0)
                            .path("content")
                            .path("parts")
                            .get(0)
                            .path("text")
                            .asText()
                            .trim();

            log.info(
                    "[LLM_SUCCESS] provider=Gemini target={} durationMs={} responseLength={}",
                    target,
                    duration,
                    result.length());

            responseCache.put(cacheKey, result);
            return result;

        } catch (Exception e) {
            log.error("[LLM_EXCEPTION] target={} message={}", target, e.getMessage());
            throw new RuntimeException(
                    "Failed to communicate with Gemini provider: " + e.getMessage(), e);
        }
    }
}
