package com.commafeed.backend.service;

import java.io.IOException;
import java.util.List;

import jakarta.ws.rs.core.MediaType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.commafeed.backend.favicon.Favicon;
import com.commafeed.backend.favicon.FaviconFetcher;
import com.commafeed.backend.model.Feed;

@ExtendWith(MockitoExtension.class)
class FeedFaviconServiceTest {

	@Mock
	private FaviconFetcher fetcher1;

	@Mock
	private FaviconFetcher fetcher2;

	private FeedFaviconService service;
	private Feed feed;

	@BeforeEach
	void init() throws IOException {
		service = new FeedFaviconService(List.of(fetcher1, fetcher2));
		feed = new Feed();
		feed.setUrl("https://example.com/feed");
	}

	@Test
	void testReturnsFirstValidFavicon() {
		byte[] iconBytes = new byte[1000];
		Favicon validFavicon = new Favicon(iconBytes, "image/png");

		Mockito.when(fetcher1.fetch(feed)).thenReturn(validFavicon);

		Favicon result = service.fetchFavicon(feed);

		Assertions.assertEquals(validFavicon, result);
		Mockito.verify(fetcher1).fetch(feed);
		Mockito.verifyNoInteractions(fetcher2);
	}

	@Test
	void testFallsBackToNextFetcherWhenFirstReturnsNull() {
		byte[] iconBytes = new byte[1000];
		Favicon validFavicon = new Favicon(iconBytes, "image/png");

		Mockito.when(fetcher1.fetch(feed)).thenReturn(null);
		Mockito.when(fetcher2.fetch(feed)).thenReturn(validFavicon);

		Favicon result = service.fetchFavicon(feed);

		Assertions.assertEquals(validFavicon, result);
	}

	@Test
	void testFallsBackToNextFetcherWhenFirstReturnsTooSmallIcon() {
		byte[] tinyIcon = new byte[50];
		Favicon tinyFavicon = new Favicon(tinyIcon, "image/png");

		byte[] validIcon = new byte[1000];
		Favicon validFavicon = new Favicon(validIcon, "image/png");

		Mockito.when(fetcher1.fetch(feed)).thenReturn(tinyFavicon);
		Mockito.when(fetcher2.fetch(feed)).thenReturn(validFavicon);

		Favicon result = service.fetchFavicon(feed);

		Assertions.assertEquals(validFavicon, result);
	}

	@Test
	void testFallsBackToNextFetcherWhenFirstReturnsTooLargeIcon() {
		byte[] hugeIcon = new byte[100001];
		Favicon hugeFavicon = new Favicon(hugeIcon, "image/png");

		byte[] validIcon = new byte[1000];
		Favicon validFavicon = new Favicon(validIcon, "image/png");

		Mockito.when(fetcher1.fetch(feed)).thenReturn(hugeFavicon);
		Mockito.when(fetcher2.fetch(feed)).thenReturn(validFavicon);

		Favicon result = service.fetchFavicon(feed);

		Assertions.assertEquals(validFavicon, result);
	}

	@Test
	void testFallsBackToNextFetcherWhenFirstReturnsBlacklistedContentType() {
		byte[] iconBytes = new byte[1000];
		Favicon xmlFavicon = new Favicon(iconBytes, "application/xml");

		byte[] validIcon = new byte[1000];
		Favicon validFavicon = new Favicon(validIcon, "image/png");

		Mockito.when(fetcher1.fetch(feed)).thenReturn(xmlFavicon);
		Mockito.when(fetcher2.fetch(feed)).thenReturn(validFavicon);

		Favicon result = service.fetchFavicon(feed);

		Assertions.assertEquals(validFavicon, result);
	}

	@Test
	void testFallsBackToNextFetcherWhenFirstReturnsHtmlContentType() {
		byte[] iconBytes = new byte[1000];
		Favicon htmlFavicon = new Favicon(iconBytes, "text/html");

		byte[] validIcon = new byte[1000];
		Favicon validFavicon = new Favicon(validIcon, "image/png");

		Mockito.when(fetcher1.fetch(feed)).thenReturn(htmlFavicon);
		Mockito.when(fetcher2.fetch(feed)).thenReturn(validFavicon);

		Favicon result = service.fetchFavicon(feed);

		Assertions.assertEquals(validFavicon, result);
	}

	@Test
	void testReturnsDefaultFaviconWhenAllFetchersFail() {
		Mockito.when(fetcher1.fetch(feed)).thenReturn(null);
		Mockito.when(fetcher2.fetch(feed)).thenReturn(null);

		Favicon result = service.fetchFavicon(feed);

		Assertions.assertNotNull(result);
		Assertions.assertTrue(result.mediaType().isCompatible(MediaType.valueOf("image/gif")));
		Assertions.assertTrue(result.icon().length > 0);
	}

	@Test
	void testReturnsDefaultFaviconWhenNoFetchersRegistered() throws IOException {
		FeedFaviconService emptyService = new FeedFaviconService(List.of());

		Favicon result = emptyService.fetchFavicon(feed);

		Assertions.assertNotNull(result);
		Assertions.assertTrue(result.mediaType().isCompatible(MediaType.valueOf("image/gif")));
	}
}
