package com.commafeed.backend.favicon;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

import jakarta.ws.rs.core.MediaType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.HttpGetter;
import com.commafeed.backend.HttpGetter.HttpResult;
import com.commafeed.backend.model.Feed;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class YoutubeFaviconFetcherTest {

	@Mock
	private HttpGetter httpGetter;

	@Mock
	private CommaFeedConfiguration config;

	@Mock
	private ObjectMapper objectMapper;

	private YoutubeFaviconFetcher faviconFetcher;

	@BeforeEach
	void init() {
		faviconFetcher = new YoutubeFaviconFetcher(httpGetter, config, objectMapper);
	}

	@Test
	void testFetchWithNonYoutubeUrl() {
		Feed feed = new Feed();
		feed.setUrl("https://example.com/feed");

		Assertions.assertNull(faviconFetcher.fetch(feed));
		Mockito.verifyNoInteractions(httpGetter, objectMapper);
	}

	@Test
	void testFetchWithNoGoogleAuthKey() {
		Feed feed = new Feed();
		feed.setUrl("https://youtube.com/feeds/videos.xml?user=someUser");

		Mockito.when(config.googleAuthKey()).thenReturn(Optional.empty());

		Assertions.assertNull(faviconFetcher.fetch(feed));
		Mockito.verify(config).googleAuthKey();
		Mockito.verifyNoInteractions(httpGetter, objectMapper);
	}

	@Test
	void testFetchForUser() throws Exception {
		Feed feed = new Feed();
		feed.setUrl("https://youtube.com/feeds/videos.xml?user=testUser");

		Mockito.when(config.googleAuthKey()).thenReturn(Optional.of("test-api-key"));

		byte[] apiResponse = """
				{"items":[{"snippet":{"thumbnails":{"default":{"url":"https://example.com/icon.png"}}}}]}""".getBytes();
		HttpResult apiHttpResult = new HttpResult(apiResponse, "application/json", null, null, null, Duration.ZERO);
		Mockito.when(httpGetter.get("https://www.googleapis.com/youtube/v3/channels?part=snippet&key=test-api-key&forUsername=testUser"))
				.thenReturn(apiHttpResult);

		JsonNode jsonNode = new ObjectMapper().readTree(apiResponse);
		Mockito.when(objectMapper.readTree(apiResponse)).thenReturn(jsonNode);

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

		byte[] apiResponse = """
				{"items":[{"snippet":{"thumbnails":{"default":{"url":"https://example.com/icon.png"}}}}]}""".getBytes();
		HttpResult apiHttpResult = new HttpResult(apiResponse, "application/json", null, null, null, Duration.ZERO);
		Mockito.when(httpGetter.get("https://www.googleapis.com/youtube/v3/channels?part=snippet&key=test-api-key&id=testChannelId"))
				.thenReturn(apiHttpResult);

		JsonNode jsonNode = new ObjectMapper().readTree(apiResponse);
		Mockito.when(objectMapper.readTree(apiResponse)).thenReturn(jsonNode);

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

		byte[] playlistResponse = """
				{"items":[{"snippet":{"channelId":"testChannelId"}}]}""".getBytes();
		HttpResult playlistHttpResult = new HttpResult(playlistResponse, "application/json", null, null, null, Duration.ZERO);
		Mockito.when(httpGetter.get("https://www.googleapis.com/youtube/v3/playlists?part=snippet&key=test-api-key&id=testPlaylistId"))
				.thenReturn(playlistHttpResult);

		JsonNode playlistJsonNode = new ObjectMapper().readTree(playlistResponse);
		Mockito.when(objectMapper.readTree(playlistResponse)).thenReturn(playlistJsonNode);

		byte[] channelResponse = """
				{"items":[{"snippet":{"thumbnails":{"default":{"url":"https://example.com/icon.png"}}}}]}""".getBytes();
		HttpResult channelHttpResult = new HttpResult(channelResponse, "application/json", null, null, null, Duration.ZERO);
		Mockito.when(httpGetter.get("https://www.googleapis.com/youtube/v3/channels?part=snippet&key=test-api-key&id=testChannelId"))
				.thenReturn(channelHttpResult);

		JsonNode channelJsonNode = new ObjectMapper().readTree(channelResponse);
		Mockito.when(objectMapper.readTree(channelResponse)).thenReturn(channelJsonNode);

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

		Mockito.when(httpGetter.get("https://www.googleapis.com/youtube/v3/channels?part=snippet&key=test-api-key&forUsername=testUser"))
				.thenThrow(new IOException("Network error"));

		Assertions.assertNull(faviconFetcher.fetch(feed));
	}

	@Test
	void testFetchWithInvalidIconResponse() throws Exception {
		Feed feed = new Feed();
		feed.setUrl("https://youtube.com/feeds/videos.xml?user=testUser");

		Mockito.when(config.googleAuthKey()).thenReturn(Optional.of("test-api-key"));

		byte[] apiResponse = """
				{"items":[{"snippet":{"thumbnails":{"default":{"url":"https://example.com/icon.png"}}}}]}""".getBytes();
		HttpResult apiHttpResult = new HttpResult(apiResponse, "application/json", null, null, null, Duration.ZERO);
		Mockito.when(httpGetter.get("https://www.googleapis.com/youtube/v3/channels?part=snippet&key=test-api-key&forUsername=testUser"))
				.thenReturn(apiHttpResult);

		JsonNode jsonNode = new ObjectMapper().readTree(apiResponse);
		Mockito.when(objectMapper.readTree(apiResponse)).thenReturn(jsonNode);

		// Create a byte array that's too small
		byte[] iconBytes = new byte[50];
		String contentType = "image/png";
		HttpResult iconHttpResult = new HttpResult(iconBytes, contentType, null, null, null, Duration.ZERO);
		Mockito.when(httpGetter.get("https://example.com/icon.png")).thenReturn(iconHttpResult);

		Assertions.assertNull(faviconFetcher.fetch(feed));
	}

	@Test
	void testFetchWithEmptyApiResponse() throws Exception {
		Feed feed = new Feed();
		feed.setUrl("https://youtube.com/feeds/videos.xml?user=testUser");

		Mockito.when(config.googleAuthKey()).thenReturn(Optional.of("test-api-key"));

		byte[] apiResponse = "{}".getBytes();
		HttpResult apiHttpResult = new HttpResult(apiResponse, "application/json", null, null, null, Duration.ZERO);
		Mockito.when(httpGetter.get("https://www.googleapis.com/youtube/v3/channels?part=snippet&key=test-api-key&forUsername=testUser"))
				.thenReturn(apiHttpResult);

		JsonNode jsonNode = Mockito.mock(JsonNode.class);
		Mockito.when(objectMapper.readTree(apiResponse)).thenReturn(jsonNode);
		Mockito.when(jsonNode.at(Mockito.any(JsonPointer.class))).thenReturn(jsonNode);
		Mockito.when(jsonNode.isMissingNode()).thenReturn(true);

		Assertions.assertNull(faviconFetcher.fetch(feed));
	}
}