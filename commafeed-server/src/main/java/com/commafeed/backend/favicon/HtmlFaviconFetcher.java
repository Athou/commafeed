package com.commafeed.backend.favicon;

import jakarta.annotation.Priority;
import jakarta.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.commafeed.backend.HttpGetter;
import com.commafeed.backend.HttpGetter.HttpResult;
import com.commafeed.backend.model.Feed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Extracts favicon url from html page.
 */
@Slf4j
@RequiredArgsConstructor
@Singleton
@Priority(1)
public class HtmlFaviconFetcher implements FaviconFetcher {

	private final HttpGetter getter;

	@Override
	public Favicon fetch(Feed feed) {
		String url = feed.getLink();
		if (url == null) {
			return null;
		}

		Document doc;
		try {
			HttpResult result = getter.get(url);
			doc = Jsoup.parse(new String(result.content()), url);
		} catch (Exception e) {
			log.debug("Failed to retrieve page to find icon", e);
			return null;
		}

		Elements icons = doc.select("link[rel~=(?i)^(shortcut|icon|shortcut icon)$]");
		if (icons.isEmpty()) {
			log.debug("No icon found in page {}", url);
			return null;
		}

		String href = icons.getFirst().attr("abs:href");
		if (StringUtils.isBlank(href)) {
			log.debug("No icon found in page");
			return null;
		}

		try {
			HttpResult result = getter.get(href);
			return new Favicon(result.content(), result.contentType());
		} catch (Exception e) {
			log.debug("Failed to retrieve icon found in page {}", href, e);
			return null;
		}
	}
}
