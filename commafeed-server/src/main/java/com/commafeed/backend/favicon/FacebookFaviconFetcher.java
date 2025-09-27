package com.commafeed.backend.favicon;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

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
public class FacebookFaviconFetcher extends AbstractFaviconFetcher {

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

		byte[] bytes = null;
		String contentType = null;

		try {
			log.debug("Getting Facebook user's icon, {}", url);

			HttpResult iconResult = getter.get(iconUrl);
			bytes = iconResult.content();
			contentType = iconResult.contentType();
		} catch (Exception e) {
			log.debug("Failed to retrieve Facebook icon", e);
		}

		if (!isValidIconResponse(bytes, contentType)) {
			return null;
		}
		return new Favicon(bytes, contentType);
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
