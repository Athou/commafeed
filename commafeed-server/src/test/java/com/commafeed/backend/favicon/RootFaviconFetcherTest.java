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
class RootFaviconFetcherTest {

	@Mock
	private HttpGetter httpGetter;

	private RootFaviconFetcher faviconFetcher;

	@BeforeEach
	void init() {
		faviconFetcher = new RootFaviconFetcher(httpGetter);
	}

	@Test
	void testFetchUsesLinkWhenAvailable() throws Exception {
		Feed feed = new Feed();
		feed.setUrl("https://feeds.example.com/feed");
		feed.setLink("https://www.example.com/blog");

		byte[] iconBytes = new byte[1000];
		String contentType = "image/x-icon";
		HttpResult httpResult = new HttpResult(iconBytes, contentType, null, null, null, Duration.ZERO);
		Mockito.when(httpGetter.get("https://www.example.com/favicon.ico")).thenReturn(httpResult);

		Favicon result = faviconFetcher.fetch(feed);

		Assertions.assertNotNull(result);
		Assertions.assertEquals(iconBytes, result.icon());
		Assertions.assertTrue(result.mediaType().isCompatible(MediaType.valueOf(contentType)));
	}

	@Test
	void testFetchFallsBackToUrlWhenLinkIsNull() throws Exception {
		Feed feed = new Feed();
		feed.setUrl("https://example.com/feed.xml");

		byte[] iconBytes = new byte[1000];
		String contentType = "image/x-icon";
		HttpResult httpResult = new HttpResult(iconBytes, contentType, null, null, null, Duration.ZERO);
		Mockito.when(httpGetter.get("https://example.com/favicon.ico")).thenReturn(httpResult);

		Favicon result = faviconFetcher.fetch(feed);

		Assertions.assertNotNull(result);
		Assertions.assertEquals(iconBytes, result.icon());
		Assertions.assertTrue(result.mediaType().isCompatible(MediaType.valueOf(contentType)));
	}

	@Test
	void testFetchWithHttpGetterException() throws Exception {
		Feed feed = new Feed();
		feed.setUrl("https://example.com/feed.xml");
		feed.setLink("https://example.com");

		Mockito.when(httpGetter.get("https://example.com/favicon.ico")).thenThrow(new RuntimeException("Network error"));

		Assertions.assertNull(faviconFetcher.fetch(feed));
	}
}
