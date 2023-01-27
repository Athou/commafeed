package com.commafeed.backend.feed;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.commafeed.backend.HttpGetter;
import com.commafeed.backend.HttpGetter.HttpResult;
import com.commafeed.backend.HttpGetter.NotModifiedException;
import com.commafeed.backend.urlprovider.FeedURLProvider;
import com.rometools.rome.io.FeedException;

@ExtendWith(MockitoExtension.class)
class FeedFetcherTest {

	@Mock
	private FeedParser parser;

	@Mock
	private HttpGetter getter;

	@Mock
	private Set<FeedURLProvider> urlProviders;

	private FeedFetcher fetcher;

	@BeforeEach
	void init() {
		fetcher = new FeedFetcher(parser, getter, urlProviders);
	}

	@Test
	void updatesHeaderWhenContentDitNotChange() throws FeedException, IOException, NotModifiedException {
		String url = "https://aaa.com";
		String lastModified = "last-modified-1";
		String etag = "etag-1";
		byte[] content = "content".getBytes();
		String lastContentHash = DigestUtils.sha1Hex(content);

		Mockito.when(getter.getBinary(url, lastModified, etag, 20000))
				.thenReturn(new HttpResult(content, "content-type", "last-modified-2", "etag-2", 20, null));

		NotModifiedException e = Assertions.assertThrows(NotModifiedException.class,
				() -> fetcher.fetch(url, false, lastModified, etag, new Date(), lastContentHash));

		Assertions.assertEquals("last-modified-2", e.getNewLastModifiedHeader());
		Assertions.assertEquals("etag-2", e.getNewEtagHeader());

	}

}
