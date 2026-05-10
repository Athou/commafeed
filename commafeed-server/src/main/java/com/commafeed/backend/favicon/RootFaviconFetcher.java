package com.commafeed.backend.favicon;

import java.net.URI;

import jakarta.annotation.Priority;
import jakarta.inject.Singleton;

import com.commafeed.backend.HttpGetter;
import com.commafeed.backend.HttpGetter.HttpResult;
import com.commafeed.backend.model.Feed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Fetches favicon from root of the domain (e.g. https://example.com/favicon.ico)
 */
@Slf4j
@RequiredArgsConstructor
@Singleton
@Priority(0)
public class RootFaviconFetcher implements FaviconFetcher {

	private final HttpGetter getter;

	@Override
	public Favicon fetch(Feed feed) {
		String url = feed.getLink();
		if (url == null) {
			url = feed.getUrl();
		}

		URI uri = URI.create(url);
		String faviconUrl = "%s://%s/favicon.ico".formatted(uri.getScheme(), uri.getHost());
		try {
			log.debug("getting root icon at {}", faviconUrl);
			HttpResult result = getter.get(faviconUrl);
			return new Favicon(result.content(), result.contentType());
		} catch (Exception e) {
			log.debug("Failed to retrieve iconAtRoot for url {}: ", url, e);
			return null;
		}
	}
}
