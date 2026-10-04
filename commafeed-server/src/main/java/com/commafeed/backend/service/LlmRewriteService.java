package com.commafeed.backend.service;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.HttpClientFactory;
import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.User;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import jakarta.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.StringEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.util.Timeout;

import java.io.IOException;
import java.net.URLEncoder;
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

    private final CloseableHttpClient httpClient;
    private final FeedEntryDAO feedEntryDAO;
    private final FeedSubscriptionDAO feedSubscriptionDAO;
    private final CommaFeedConfiguration config;

    public LlmRewriteService(
            HttpClientFactory httpClientFactory,
            FeedEntryDAO feedEntryDAO,
            FeedSubscriptionDAO feedSubscriptionDAO,
            CommaFeedConfiguration config) {
        this.httpClient = httpClientFactory.newClient(1);
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
        JsonObject requestBody =
                new JsonObject()
                        .put(
                                "contents",
                                new JsonArray()
                                        .add(
                                                new JsonObject()
                                                        .put(
                                                                "parts",
                                                                new JsonArray()
                                                                        .add(
                                                                                new JsonObject()
                                                                                        .put(
                                                                                                "text",
                                                                                                instruction))))));

        HttpPost request =
                new HttpPost(API_URL + URLEncoder.encode(apiKey, StandardCharsets.UTF_8));
        request.setConfig(
                RequestConfig.custom()
                        .setResponseTimeout(Timeout.of(config.httpClient().responseTimeout()))
                        .build());
        request.setEntity(new StringEntity(requestBody.encode(), ContentType.APPLICATION_JSON));

        try {
            return httpClient.execute(
                    request,
                    response -> {
                        if (response.getCode() < 200 || response.getCode() >= 300) {
                            throw new LlmUnavailableException();
                        }
                        if (response.getEntity() == null) {
                            throw new LlmUnavailableException();
                        }
                        String responseBody =
                                new String(
                                        response.getEntity().getContent().readAllBytes(),
                                        StandardCharsets.UTF_8);
                        return parseGeneratedText(responseBody);
                    });
        } catch (IOException | RuntimeException e) {
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
