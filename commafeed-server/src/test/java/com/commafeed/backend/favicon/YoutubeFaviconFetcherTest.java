package com.commafeed.backend.favicon;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.HttpGetter;
import com.commafeed.backend.HttpGetter.HttpResult;
import com.commafeed.backend.model.Feed;

import jakarta.ws.rs.core.MediaType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import tools.jackson.core.JsonPointer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class YoutubeFaviconFetcherTest {

    @Mock private HttpGetter httpGetter;

    @Mock private CommaFeedConfiguration config;

    @Mock private JsonMapper jsonMapper;

    private YoutubeFaviconFetcher faviconFetcher;

    @BeforeEach
    void init() {
        faviconFetcher = new YoutubeFaviconFetcher(httpGetter, config, jsonMapper);
    }

    @Test
    void testFetchWithNonYoutubeUrl() {
        Feed feed = new Feed();
        feed.setUrl("https://example.com/feed");

        Assertions.assertNull(faviconFetcher.fetch(feed));
        Mockito.verifyNoInteractions(httpGetter, jsonMapper);
    }

    @Test
    void testFetchWithNoGoogleAuthKey() {
        Feed feed = new Feed();
        feed.setUrl("https://youtube.com/feeds/videos.xml?user=someUser");

        Mockito.when(config.googleAuthKey()).thenReturn(Optional.empty());

        Assertions.assertNull(faviconFetcher.fetch(feed));
        Mockito.verify(config).googleAuthKey();
        Mockito.verifyNoInteractions(httpGetter, jsonMapper);
    }

    @Test
    void testFetchForUser() throws Exception {
        Feed feed = new Feed();
        feed.setUrl("https://youtube.com/feeds/videos.xml?user=testUser");

        Mockito.when(config.googleAuthKey()).thenReturn(Optional.of("test-api-key"));

        byte[] apiResponse =
                """
                {"items":[{"snippet":{"thumbnails":{"default":{"url":"https://example.com/icon.png"}}}}]}\
                """
                        .getBytes();
        HttpResult apiHttpResult = new HttpResult(apiResponse, "application/json", null, null, null, Duration.ZERO);
        Mockito.when(
                        httpGetter.get(
                                "https://www.googleapis.com/youtube/v3/channels?part=snippet&key=test-api-key&forUsername=testUser"))
                .thenReturn(apiHttpResult);

        JsonNode jsonNode = new JsonMapper().readTree(apiResponse);
        Mockito.when(jsonMapper.readTree(apiResponse)).thenReturn(jsonNode);

        byte[] iconBytes = new byte[1000];
        String contentType = "image/png";
        HttpResult iconHttpResult = new HttpResult(iconBytes, contentType, null, null, null, Duration.ZERO);
        Mockito.when(httpGetter.get("https://example.com/icon.png")).thenReturn(iconHttpResult);

        Favicon result = faviconFetcher.fetch(feed);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(iconBytes, result.icon());
        Assertions.assertTrue(result.mediaType().isCompatible(MediaType.valueOf(contentType)));
    }

    @Test
    void testFetchForChannel() throws Exception {
        Feed feed = new Feed();
        feed.setUrl("https://youtube.com/feeds/videos.xml?channel_id=testChannelId");

        Mockito.when(config.googleAuthKey()).thenReturn(Optional.of("test-api-key"));

        byte[] apiResponse =
                """
                {"items":[{"snippet":{"thumbnails":{"default":{"url":"https://example.com/icon.png"}}}}]}\
                """
                        .getBytes();
        HttpResult apiHttpResult = new HttpResult(apiResponse, "application/json", null, null, null, Duration.ZERO);
        Mockito.when(
                        httpGetter.get(
                                "https://www.googleapis.com/youtube/v3/channels?part=snippet&key=test-api-key&id=testChannelId"))
                .thenReturn(apiHttpResult);

        JsonNode jsonNode = new JsonMapper().readTree(apiResponse);
        Mockito.when(jsonMapper.readTree(apiResponse)).thenReturn(jsonNode);

        byte[] iconBytes = new byte[1000];
        String contentType = "image/png";
        HttpResult iconHttpResult = new HttpResult(iconBytes, contentType, null, null, null, Duration.ZERO);
        Mockito.when(httpGetter.get("https://example.com/icon.png")).thenReturn(iconHttpResult);

        Favicon result = faviconFetcher.fetch(feed);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(iconBytes, result.icon());
        Assertions.assertTrue(result.mediaType().isCompatible(MediaType.valueOf(contentType)));
    }

    @Test
    void testFetchForPlaylist() throws Exception {
        Feed feed = new Feed();
        feed.setUrl("https://youtube.com/feeds/videos.xml?playlist_id=testPlaylistId");

        Mockito.when(config.googleAuthKey()).thenReturn(Optional.of("test-api-key"));

        byte[] playlistResponse =
                """
                {"items":[{"snippet":{"channelId":"testChannelId"}}]}\
                """
                        .getBytes();
        HttpResult playlistHttpResult =
                new HttpResult(playlistResponse, "application/json", null, null, null, Duration.ZERO);
        Mockito.when(
                        httpGetter.get(
                                "https://www.googleapis.com/youtube/v3/playlists?part=snippet&key=test-api-key&id=testPlaylistId"))
                .thenReturn(playlistHttpResult);

        JsonNode playlistJsonNode = new JsonMapper().readTree(playlistResponse);
        Mockito.when(jsonMapper.readTree(playlistResponse)).thenReturn(playlistJsonNode);

        byte[] channelResponse =
                """
                {"items":[{"snippet":{"thumbnails":{"default":{"url":"https://example.com/icon.png"}}}}]}\
                """
                        .getBytes();
        HttpResult channelHttpResult =
                new HttpResult(channelResponse, "application/json", null, null, null, Duration.ZERO);
        Mockito.when(
                        httpGetter.get(
                                "https://www.googleapis.com/youtube/v3/channels?part=snippet&key=test-api-key&id=testChannelId"))
                .thenReturn(channelHttpResult);

        JsonNode channelJsonNode = new JsonMapper().readTree(channelResponse);
        Mockito.when(jsonMapper.readTree(channelResponse)).thenReturn(channelJsonNode);

        byte[] iconBytes = new byte[1000];
        String contentType = "image/png";
        HttpResult iconHttpResult = new HttpResult(iconBytes, contentType, null, null, null, Duration.ZERO);
        Mockito.when(httpGetter.get("https://example.com/icon.png")).thenReturn(iconHttpResult);

        Favicon result = faviconFetcher.fetch(feed);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(iconBytes, result.icon());
        Assertions.assertTrue(result.mediaType().isCompatible(MediaType.valueOf(contentType)));
    }

    @Test
    void testFetchWithHttpGetterException() throws Exception {
        Feed feed = new Feed();
        feed.setUrl("https://youtube.com/feeds/videos.xml?user=testUser");

        Mockito.when(config.googleAuthKey()).thenReturn(Optional.of("test-api-key"));

        Mockito.when(
                        httpGetter.get(
                                "https://www.googleapis.com/youtube/v3/channels?part=snippet&key=test-api-key&forUsername=testUser"))
                .thenThrow(new IOException("Network error"));

        Assertions.assertNull(faviconFetcher.fetch(feed));
    }

    @Test
    void testFetchWithEmptyApiResponse() throws Exception {
        Feed feed = new Feed();
        feed.setUrl("https://youtube.com/feeds/videos.xml?user=testUser");

        Mockito.when(config.googleAuthKey()).thenReturn(Optional.of("test-api-key"));

        byte[] apiResponse = "{}".getBytes();
        HttpResult apiHttpResult = new HttpResult(apiResponse, "application/json", null, null, null, Duration.ZERO);
        Mockito.when(
                        httpGetter.get(
                                "https://www.googleapis.com/youtube/v3/channels?part=snippet&key=test-api-key&forUsername=testUser"))
                .thenReturn(apiHttpResult);

        JsonNode jsonNode = Mockito.mock(JsonNode.class);
        Mockito.when(jsonMapper.readTree(apiResponse)).thenReturn(jsonNode);
        Mockito.when(jsonNode.at(Mockito.any(JsonPointer.class))).thenReturn(jsonNode);
        Mockito.when(jsonNode.isMissingNode()).thenReturn(true);

        Assertions.assertNull(faviconFetcher.fetch(feed));
    }
}
