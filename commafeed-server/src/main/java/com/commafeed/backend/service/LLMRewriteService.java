package com.commafeed.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@ApplicationScoped
public class LLMRewriteService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateAlternative(String originalContent, String target, String prompt) {
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

            if (response.statusCode() != 200) {
                throw new RuntimeException(
                        "Gemini API returned status "
                                + response.statusCode()
                                + ": "
                                + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());
            return root.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText()
                    .trim();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to communicate with Gemini provider: " + e.getMessage(), e);
        }
    }
}
