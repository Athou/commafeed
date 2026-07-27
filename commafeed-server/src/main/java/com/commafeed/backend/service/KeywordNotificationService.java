package com.commafeed.backend.service;

import com.commafeed.backend.dao.FeedEntryKeywordDAO;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryKeyword;
import io.quarkus.arc.Arc;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class KeywordNotificationService {

    private final FeedEntryKeywordDAO keywordDAO;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public void processEntryAsync(FeedEntry entry) {
        CompletableFuture.runAsync(
                () -> {
                    Arc.container().requestContext().activate();
                    try {
                        QuarkusTransaction.requiringNew()
                                .run(
                                        () -> {
                                            List<FeedEntryKeyword> keywords =
                                                    keywordDAO.findAllKeywords();
                                            if (keywords == null || keywords.isEmpty()) {
                                                return;
                                            }
                                            String title =
                                                    entry.getContent() != null
                                                                    && entry.getContent().getTitle()
                                                                            != null
                                                            ? entry.getContent().getTitle()
                                                            : "Новая статья";

                                            String content =
                                                    entry.getContent() != null
                                                                    && entry.getContent()
                                                                                    .getContent()
                                                                            != null
                                                            ? entry.getContent().getContent()
                                                            : "";

                                            String textToSearch =
                                                    (title + " " + content).toLowerCase();

                                            for (FeedEntryKeyword k : keywords) {
                                                if (k.getKeyword() != null
                                                        && textToSearch.contains(
                                                                k.getKeyword().toLowerCase())) {
                                                    log.info(
                                                            "Match found for keyword '{}' in entry '{}'",
                                                            k.getKeyword(),
                                                            entry.getGuid());
                                                    sendNotification(
                                                            k.getTelegramChatId(),
                                                            k.getKeyword(),
                                                            title,
                                                            entry.getUrl());
                                                }
                                            }
                                        });
                    } catch (Exception e) {
                        log.error("Error evaluating keywords for entry " + entry.getId(), e);
                    } finally {
                        Arc.container().requestContext().deactivate();
                    }
                });
    }

    private void sendNotification(String chatId, String keyword, String title, String url) {
        String botToken = System.getProperty("TELEGRAM_BOT_TOKEN");
        if (botToken == null || botToken.isBlank()) {
            botToken = System.getenv("TELEGRAM_BOT_TOKEN");
        }

        if (botToken != null && !botToken.isBlank() && chatId != null && !chatId.isBlank()) {
            String telegramUrl = "https://api.telegram.org/bot" + botToken + "/sendMessage";

            String message =
                    String.format("🔔 *Keyword Match: %s*\n\n📌 *%s*\n🔗 %s", keyword, title, url);
            String body =
                    String.format(
                            "{\"chat_id\":\"%s\",\"text\":%s,\"parse_mode\":\"Markdown\"}",
                            chatId, escapeJson(message));

            try {
                HttpRequest request =
                        HttpRequest.newBuilder()
                                .uri(URI.create(telegramUrl))
                                .header("Content-Type", "application/json")
                                .POST(HttpRequest.BodyPublishers.ofString(body))
                                .build();

                httpClient
                        .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(
                                res ->
                                        log.info(
                                                "Telegram notification sent, status: {}",
                                                res.statusCode()));
            } catch (Exception e) {
                log.error("Failed to dispatch Telegram notification", e);
            }
        } else {
            log.info(
                    "[NOTIFICATION MOCK MATCH] Keyword: '{}' | Entry Title: '{}' | URL: {}",
                    keyword,
                    title,
                    url);
        }
    }

    private String escapeJson(String text) {
        if (text == null) {
            return "\"\"";
        }
        return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
    }
}
