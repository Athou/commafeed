package com.commafeed.backend.favicon;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import jakarta.annotation.Priority;
import jakarta.inject.Singleton;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URIBuilder;

import com.commafeed.backend.HttpGetter;
import com.commafeed.backend.HttpGetter.HttpResult;
import com.commafeed.backend.model.Feed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Singleton
@Priority(3)
public class FacebookFaviconFetcher implements FaviconFetcher {

	private final HttpGetter getter;

	@Override
	public Favicon fetch(Feed feed) {
		String url = feed.getUrl();
		if (!url.toLowerCase().contains("www.facebook.com")) {
			return null;
		}

		String userName = extractUserName(url);
		if (userName == null) {
			return null;
		}

		String iconUrl = String.format("https://graph.facebook.com/%s/picture?type=square&height=16", userName);

		try {
			log.debug("Getting Facebook user's icon, {}", url);

			HttpResult iconResult = getter.get(iconUrl);
			return new Favicon(iconResult.content(), iconResult.contentType());
		} catch (Exception e) {
			log.debug("Failed to retrieve Facebook icon", e);
			return null;
		}
	}

	private String extractUserName(String url) {
		URI uri;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			log.debug("could not parse url", e);
			return null;
		}

		List<NameValuePair> params = new URIBuilder(uri).getQueryParams();
		return params.stream().filter(p -> "id".equals(p.getName())).map(NameValuePair::getValue).findFirst().orElse(null);
	}

}
