package com.commafeed.backend.favicon;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.commafeed.backend.HttpGetter;
import com.commafeed.backend.HttpGetter.HttpResult;
import com.commafeed.backend.feed.FeedUtils;
import com.commafeed.backend.model.Feed;

/**
 * Inspired/Ported from https://github.com/potatolondon/getfavicon
 * 
 */
@Slf4j
@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
@Singleton
public class DefaultFaviconFetcher extends AbstractFaviconFetcher {

	private final HttpGetter getter;

	@Override
	public byte[] fetch(Feed feed) {
		byte[] icon = fetch(feed.getLink());
		if (icon == null) {
			icon = fetch(feed.getUrl());
		}
		return icon;
	}

	private byte[] fetch(String url) {
		if (url == null) {
			log.debug("url is null");
			return null;
		}

		int doubleSlash = url.indexOf("//");
		if (doubleSlash == -1) {
			doubleSlash = 0;
		} else {
			doubleSlash += 2;
		}
		int firstSlash = url.indexOf('/', doubleSlash);
		if (firstSlash != -1) {
			url = url.substring(0, firstSlash);
		}

		byte[] icon = getIconAtRoot(url);

		if (icon == null) {
			icon = getIconInPage(url);
		}

		return icon;
	}

	private byte[] getIconAtRoot(String url) {
		byte[] bytes = null;
		String contentType = null;

		try {
			url = FeedUtils.removeTrailingSlash(url) + "/favicon.ico";
			log.debug("getting root icon at {}", url);
			HttpResult result = getter.getBinary(url, TIMEOUT);
			bytes = result.getContent();
			contentType = result.getContentType();
		} catch (Exception e) {
			log.debug("Failed to retrieve iconAtRoot for url {}: ", url);
			log.trace("Failed to retrieve iconAtRoot for url {}: ", url, e);
		}

		if (!isValidIconResponse(bytes, contentType)) {
			bytes = null;
		}
		return bytes;
	}

	private byte[] getIconInPage(String url) {

		Document doc = null;
		try {
			HttpResult result = getter.getBinary(url, TIMEOUT);
			doc = Jsoup.parse(new String(result.getContent()), url);
		} catch (Exception e) {
			log.debug("Failed to retrieve page to find icon");
			log.trace("Failed to retrieve page to find icon", e);
			return null;
		}

		Elements icons = doc.select("link[rel~=(?i)^(shortcut|icon|shortcut icon)$]");

		if (icons.isEmpty()) {
			log.debug("No icon found in page {}", url);
			return null;
		}

		String href = icons.get(0).attr("abs:href");
		if (StringUtils.isBlank(href)) {
			log.debug("No icon found in page");
			return null;
		}

		log.debug("Found unconfirmed iconInPage at {}", href);

		byte[] bytes = null;
		String contentType = null;
		try {
			HttpResult result = getter.getBinary(href, TIMEOUT);
			bytes = result.getContent();
			contentType = result.getContentType();
		} catch (Exception e) {
			log.debug("Failed to retrieve icon found in page {}", href);
			log.trace("Failed to retrieve icon found in page {}", href, e);
			return null;
		}

		if (!isValidIconResponse(bytes, contentType)) {
			log.debug("Invalid icon found for {}", href);
			return null;
		}

		return bytes;
	}
}
