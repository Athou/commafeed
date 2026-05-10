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
class HtmlFaviconFetcherTest {

	@Mock
	private HttpGetter httpGetter;

	private HtmlFaviconFetcher faviconFetcher;

	@BeforeEach
	void init() {
		faviconFetcher = new HtmlFaviconFetcher(httpGetter);
	}

	@Test
	void testFetchWithNullLink() {
		Feed feed = new Feed();
		feed.setUrl("https://example.com/feed");

		Assertions.assertNull(faviconFetcher.fetch(feed));
		Mockito.verifyNoInteractions(httpGetter);
	}

	@Test
	void testFetchWithValidIconLink() throws Exception {
		Feed feed = new Feed();
		feed.setUrl("https://example.com/feed");
		feed.setLink("https://example.com");

		String html = "<html><head><link rel=\"icon\" href=\"/favicon.png\" /></head><body></body></html>";
		HttpResult pageResult = new HttpResult(html.getBytes(), "text/html", null, null, null, Duration.ZERO);
		Mockito.when(httpGetter.get("https://example.com")).thenReturn(pageResult);

		byte[] iconBytes = new byte[1000];
		String contentType = "image/png";
		HttpResult iconResult = new HttpResult(iconBytes, contentType, null, null, null, Duration.ZERO);
		Mockito.when(httpGetter.get("https://example.com/favicon.png")).thenReturn(iconResult);

		Favicon result = faviconFetcher.fetch(feed);

		Assertions.assertNotNull(result);
		Assertions.assertEquals(iconBytes, result.icon());
		Assertions.assertTrue(result.mediaType().isCompatible(MediaType.valueOf(contentType)));
	}

	@Test
	void testFetchWithShortcutIconLink() throws Exception {
		Feed feed = new Feed();
		feed.setUrl("https://example.com/feed");
		feed.setLink("https://example.com");

		String html = "<html><head><link rel=\"shortcut icon\" href=\"https://example.com/shortcut-favicon.ico\" /></head><body></body></html>";
		HttpResult pageResult = new HttpResult(html.getBytes(), "text/html", null, null, null, Duration.ZERO);
		Mockito.when(httpGetter.get("https://example.com")).thenReturn(pageResult);

		byte[] iconBytes = new byte[1000];
		String contentType = "image/x-icon";
		HttpResult iconResult = new HttpResult(iconBytes, contentType, null, null, null, Duration.ZERO);
		Mockito.when(httpGetter.get("https://example.com/shortcut-favicon.ico")).thenReturn(iconResult);

		Favicon result = faviconFetcher.fetch(feed);

		Assertions.assertNotNull(result);
		Assertions.assertEquals(iconBytes, result.icon());
		Assertions.assertTrue(result.mediaType().isCompatible(MediaType.valueOf(contentType)));
	}

	@Test
	void testFetchWithNoIconInPage() throws Exception {
		Feed feed = new Feed();
		feed.setUrl("https://example.com/feed");
		feed.setLink("https://example.com");

		String html = "<html><head></head><body></body></html>";
		HttpResult pageResult = new HttpResult(html.getBytes(), "text/html", null, null, null, Duration.ZERO);
		Mockito.when(httpGetter.get("https://example.com")).thenReturn(pageResult);

		Assertions.assertNull(faviconFetcher.fetch(feed));
	}

	@Test
	void testFetchWithPageFetchException() throws Exception {
		Feed feed = new Feed();
		feed.setUrl("https://example.com/feed");
		feed.setLink("https://example.com");

		Mockito.when(httpGetter.get("https://example.com")).thenThrow(new RuntimeException("Network error"));

		Assertions.assertNull(faviconFetcher.fetch(feed));
	}

	@Test
	void testFetchWithIconFetchException() throws Exception {
		Feed feed = new Feed();
		feed.setUrl("https://example.com/feed");
		feed.setLink("https://example.com");

		String html = "<html><head><link rel=\"icon\" href=\"https://example.com/favicon.png\" /></head><body></body></html>";
		HttpResult pageResult = new HttpResult(html.getBytes(), "text/html", null, null, null, Duration.ZERO);
		Mockito.when(httpGetter.get("https://example.com")).thenReturn(pageResult);
		Mockito.when(httpGetter.get("https://example.com/favicon.png")).thenThrow(new RuntimeException("Network error"));

		Assertions.assertNull(faviconFetcher.fetch(feed));
	}
}
