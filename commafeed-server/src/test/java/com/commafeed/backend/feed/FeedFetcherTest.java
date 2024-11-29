package com.commafeed.backend.feed;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.commafeed.backend.Digests;
import com.commafeed.backend.HttpGetter;
import com.commafeed.backend.HttpGetter.HttpResult;
import com.commafeed.backend.HttpGetter.NotModifiedException;
import com.commafeed.backend.feed.parser.FeedParser;
import com.commafeed.backend.urlprovider.FeedURLProvider;

@ExtendWith(MockitoExtension.class)
class FeedFetcherTest {

	@Mock
	private FeedParser parser;

	@Mock
	private HttpGetter getter;

	@Mock
	private List<FeedURLProvider> urlProviders;

	private FeedFetcher fetcher;

	@BeforeEach
	void init() {
		fetcher = new FeedFetcher(parser, getter, urlProviders);
	}

	@Test
	void updatesHeaderWhenContentDitNotChange() throws Exception {
		String url = "https://aaa.com";
		String lastModified = "last-modified-1";
		String etag = "etag-1";
		byte[] content = "content".getBytes();
		String lastContentHash = Digests.sha1Hex(content);

		Mockito.when(getter.get(HttpGetter.HttpRequest.builder(url).lastModified(lastModified).eTag(etag).build()))
				.thenReturn(new HttpResult(content, "content-type", "last-modified-2", "etag-2", null, Duration.ZERO));

		NotModifiedException e = Assertions.assertThrows(NotModifiedException.class,
				() -> fetcher.fetch(url, false, lastModified, etag, Instant.now(), lastContentHash));

		Assertions.assertEquals("last-modified-2", e.getNewLastModifiedHeader());
		Assertions.assertEquals("etag-2", e.getNewEtagHeader());

	}

}
