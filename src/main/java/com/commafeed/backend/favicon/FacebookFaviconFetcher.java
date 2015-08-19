package com.commafeed.backend.favicon;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import com.commafeed.backend.HttpGetter;
import com.commafeed.backend.HttpGetter.HttpResult;
import com.commafeed.backend.model.Feed;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
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

			HttpResult iconResult = getter.getBinary(iconUrl, TIMEOUT);
			bytes = iconResult.getContent();
			contentType = iconResult.getContentType();
		} catch (Exception e) {
			log.debug("Failed to retrieve Facebook icon", e);
		}

		if (!isValidIconResponse(bytes, contentType)) {
			return null;
		}
		return new Favicon(bytes, contentType);
	}

	private String extractUserName(String url) {
		URI uri = null;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			log.debug("could not parse url", e);
			return null;
		}
		List<NameValuePair> params = URLEncodedUtils.parse(uri, StandardCharsets.UTF_8.name());
		for (NameValuePair param : params) {
			if ("id".equals(param.getName())) {
				return param.getValue();
			}
		}
		return null;
	}

}
