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
class FacebookFaviconFetcherTest {

	@Mock
	private HttpGetter httpGetter;

	private FacebookFaviconFetcher faviconFetcher;

	@BeforeEach
	void init() {
		faviconFetcher = new FacebookFaviconFetcher(httpGetter);
	}

	@Test
	void testFetchWithValidFacebookUrl() throws Exception {
		Feed feed = new Feed();
		feed.setUrl("https://www.facebook.com/something?id=validUserId");

		byte[] iconBytes = new byte[1000];
		String contentType = "image/png";

		HttpResult httpResult = new HttpResult(iconBytes, contentType, null, null, null, Duration.ZERO);
		Mockito.when(httpGetter.get("https://graph.facebook.com/validUserId/picture?type=square&height=16")).thenReturn(httpResult);

		Favicon result = faviconFetcher.fetch(feed);

		Assertions.assertNotNull(result);
		Assertions.assertEquals(iconBytes, result.icon());
		Assertions.assertTrue(result.mediaType().isCompatible(MediaType.valueOf(contentType)));
	}

	@Test
	void testFetchWithNonFacebookUrl() {
		Feed feed = new Feed();
		feed.setUrl("https://example.com");

		Assertions.assertNull(faviconFetcher.fetch(feed));
		Mockito.verifyNoInteractions(httpGetter);
	}

	@Test
	void testFetchWithFacebookUrlButNoUserId() {
		Feed feed = new Feed();
		feed.setUrl("https://www.facebook.com/something");

		Assertions.assertNull(faviconFetcher.fetch(feed));
		Mockito.verifyNoInteractions(httpGetter);
	}

	@Test
	void testFetchWithHttpGetterException() throws Exception {
		Feed feed = new Feed();
		feed.setUrl("https://www.facebook.com/something?id=validUserId");

		Mockito.when(httpGetter.get("https://graph.facebook.com/validUserId/picture?type=square&height=16"))
				.thenThrow(new RuntimeException("Network error"));

		Assertions.assertNull(faviconFetcher.fetch(feed));
	}

	@Test
	void testFetchWithInvalidIconResponse() throws Exception {
		Feed feed = new Feed();
		feed.setUrl("https://www.facebook.com/something?id=validUserId");

		// Create a byte array that's too small
		byte[] iconBytes = new byte[50];
		String contentType = "image/png";

		HttpResult httpResult = new HttpResult(iconBytes, contentType, null, null, null, Duration.ZERO);
		Mockito.when(httpGetter.get("https://graph.facebook.com/validUserId/picture?type=square&height=16")).thenReturn(httpResult);

		Assertions.assertNull(faviconFetcher.fetch(feed));
	}

	@Test
	void testFetchWithBlacklistedContentType() throws Exception {
		Feed feed = new Feed();
		feed.setUrl("https://www.facebook.com/something?id=validUserId");

		byte[] iconBytes = new byte[1000];
		String contentType = "application/xml"; // Blacklisted content type

		HttpResult httpResult = new HttpResult(iconBytes, contentType, null, null, null, Duration.ZERO);
		Mockito.when(httpGetter.get("https://graph.facebook.com/validUserId/picture?type=square&height=16")).thenReturn(httpResult);

		Assertions.assertNull(faviconFetcher.fetch(feed));
	}
}