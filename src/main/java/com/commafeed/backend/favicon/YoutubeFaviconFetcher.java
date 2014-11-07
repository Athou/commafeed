package com.commafeed.backend.favicon;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.commafeed.backend.HttpGetter;
import com.commafeed.backend.HttpGetter.HttpResult;
import com.commafeed.backend.model.Feed;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
@Singleton
public class YoutubeFaviconFetcher extends AbstractFaviconFetcher {

	private final HttpGetter getter;

	@Override
	public byte[] fetch(Feed feed) {
		String url = feed.getUrl();

		if (!url.toLowerCase().contains("://gdata.youtube.com/")) {
			return null;
		}

		String userName = extractUserName(url);
		if (userName == null) {
			return null;
		}

		String profileUrl = "https://gdata.youtube.com/feeds/users/" + userName;

		byte[] bytes = null;
		String contentType = null;

		try {
			log.debug("Getting YouTube user's icon, {}", url);

			// initial get to translate username to obscure user thumbnail URL
			HttpResult profileResult = getter.getBinary(profileUrl, TIMEOUT);
			Document doc = Jsoup.parse(new String(profileResult.getContent()), profileUrl);

			Elements thumbnails = doc.select("media|thumbnail");
			if (thumbnails.isEmpty()) {
				return null;
			}
			String thumbnailUrl = thumbnails.get(0).attr("abs:url");

			// final get to actually retrieve the thumbnail
			HttpResult iconResult = getter.getBinary(thumbnailUrl, TIMEOUT);
			bytes = iconResult.getContent();
			contentType = iconResult.getContentType();
		} catch (Exception e) {
			log.debug("Failed to retrieve YouTube icon", e);
		}

		if (!isValidIconResponse(bytes, contentType)) {
			bytes = null;
		}
		return bytes;
	}

	private String extractUserName(String url) {
		int apiOrBase = url.indexOf("/users/");
		if (apiOrBase == -1) {
			return null;
		}

		int userEndSlash = url.indexOf('/', apiOrBase + "/users/".length());
		if (userEndSlash == -1) {
			return null;
		}

		return url.substring(apiOrBase + "/users/".length(), userEndSlash);
	}

}
