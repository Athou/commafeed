package com.commafeed.backend.service;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.User;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import jakarta.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Singleton
@Slf4j
public class LlmRewriteService {

    private static final String MODEL = "gemini-3.5-flash";
    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/"
                    + MODEL
                    + ":generateContent?key=";

    private final HttpClient httpClient;
    private final FeedEntryDAO feedEntryDAO;
    private final FeedSubscriptionDAO feedSubscriptionDAO;
    private final CommaFeedConfiguration config;

    public LlmRewriteService(
            FeedEntryDAO feedEntryDAO,
            FeedSubscriptionDAO feedSubscriptionDAO,
            CommaFeedConfiguration config) {
        this.httpClient =
                HttpClient.newBuilder()
                        .connectTimeout(config.httpClient().connectTimeout())
                        .build();
        this.feedEntryDAO = feedEntryDAO;
        this.feedSubscriptionDAO = feedSubscriptionDAO;
        this.config = config;
    }

    public RewriteResult rewrite(User user, Long entryId, String target, String prompt)
            throws EntryNotFoundException, LlmUnavailableException {
        FeedEntry entry = feedEntryDAO.findById(entryId);
        if (entry == null || feedSubscriptionDAO.findByFeed(user, entry.getFeed()) == null) {
            throw new EntryNotFoundException();
        }

        String originalEntry = getOriginalEntry(entry, target);
        String generatedAlternative = generate(originalEntry, target, prompt);
        return new RewriteResult(originalEntry, generatedAlternative);
    }

    private String generate(String originalEntry, String target, String prompt)
            throws LlmUnavailableException {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (StringUtils.isBlank(apiKey)) {
            throw new LlmUnavailableException();
        }

        String instruction =
                "Rewrite the following feed entry "
                        + target
                        + " according to the user's instructions. Return only the rewritten text."
                        + "\n\nOriginal:\n"
                        + originalEntry
                        + "\n\nInstructions:\n"
                        + prompt;
        JsonObject part = new JsonObject().put("text", instruction);
        JsonObject content = new JsonObject().put("parts", new JsonArray().add(part));
        JsonObject requestBody = new JsonObject().put("contents", new JsonArray().add(content));

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        API_URL
                                                + URLEncoder.encode(
                                                        apiKey, StandardCharsets.UTF_8)))
                        .timeout(config.httpClient().responseTimeout())
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody.encode()))
                        .build();

        try {
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new LlmUnavailableException();
            }
            return parseGeneratedText(response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Gemini rewrite request interrupted");
            throw new LlmUnavailableException();
        } catch (IOException e) {
            log.warn("Gemini rewrite request failed");
            throw new LlmUnavailableException();
        } catch (RuntimeException e) {
            log.warn("Gemini rewrite request failed");
            throw new LlmUnavailableException();
        }
    }

    private String parseGeneratedText(String responseBody) throws LlmUnavailableException {
        try {
            JsonObject response = new JsonObject(responseBody);
            JsonArray candidates = response.getJsonArray("candidates");
            String generatedText =
                    Optional.ofNullable(candidates)
                            .filter(values -> !values.isEmpty())
                            .map(values -> values.getJsonObject(0))
                            .map(candidate -> candidate.getJsonObject("content"))
                            .map(content -> content.getJsonArray("parts"))
                            .filter(values -> !values.isEmpty())
                            .map(values -> values.getJsonObject(0))
                            .map(part -> part.getString("text"))
                            .orElse(null);
            if (StringUtils.isBlank(generatedText)) {
                throw new LlmUnavailableException();
            }
            return generatedText;
        } catch (RuntimeException e) {
            throw new LlmUnavailableException();
        }
    }

    private String getOriginalEntry(FeedEntry entry, String target) {
        if ("title".equals(target)) {
            return entry.getContent() == null ? "" : entry.getContent().getTitle();
        }
        return entry.getContent() == null ? "" : entry.getContent().getContent();
    }

    public record RewriteResult(String originalEntry, String generatedAlternative) {}

    public static class EntryNotFoundException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    public static class LlmUnavailableException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}
