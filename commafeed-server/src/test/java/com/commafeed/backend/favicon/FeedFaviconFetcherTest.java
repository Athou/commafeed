package com.commafeed.backend.favicon;

import java.time.Duration;

import jakarta.ws.rs.core.MediaType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.commafeed.backend.HttpGetter;
import com.commafeed.backend.HttpGetter.HttpResult;
import com.commafeed.backend.model.Feed;

@ExtendWith(MockitoExtension.class)
class FeedFaviconFetcherTest {

	@Mock
	private HttpGetter httpGetter;

	private FeedFaviconFetcher faviconFetcher;

	@BeforeEach
	void init() {
		faviconFetcher = new FeedFaviconFetcher(httpGetter);
	}

	@Test
	void testFetchWithNullIconUrl() {
		Feed feed = new Feed();
		feed.setUrl("https://example.com/feed");

		Assertions.assertNull(faviconFetcher.fetch(feed));
		Mockito.verifyNoInteractions(httpGetter);
	}

	@Test
	void testFetchWithValidIconUrl() throws Exception {
		Feed feed = new Feed();
		feed.setUrl("https://example.com/feed");
		feed.setIconUrl("https://example.com/icon.png");

		byte[] iconBytes = new byte[1000];
		String contentType = "image/png";
		HttpResult httpResult = new HttpResult(iconBytes, contentType, null, null, null, Duration.ZERO);
		Mockito.when(httpGetter.get("https://example.com/icon.png")).thenReturn(httpResult);

		Favicon result = faviconFetcher.fetch(feed);

		Assertions.assertNotNull(result);
		Assertions.assertEquals(iconBytes, result.icon());
		Assertions.assertTrue(result.mediaType().isCompatible(MediaType.valueOf(contentType)));
	}

	@Test
	void testFetchWithHttpGetterException() throws Exception {
		Feed feed = new Feed();
		feed.setUrl("https://example.com/feed");
		feed.setIconUrl("https://example.com/icon.png");

		Mockito.when(httpGetter.get("https://example.com/icon.png")).thenThrow(new RuntimeException("Network error"));

		Assertions.assertNull(faviconFetcher.fetch(feed));
	}
}
