package com.commafeed.backend.feeds;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.HttpGetter;
import com.commafeed.backend.HttpGetter.HttpResult;

/**
 * Inspired/Ported from https://github.com/potatolondon/getfavicon
 * 
 */
public class FaviconFetcher {

	private static Logger log = LoggerFactory.getLogger(FeedFetcher.class);

	private static long MIN_ICON_LENGTH = 100;
	private static long MAX_ICON_LENGTH = 20000;

	protected static List<String> ICON_MIMETYPES = Arrays.asList(
			"image/x-icon", "image/vnd.microsoft.icon", "image/ico",
			"image/icon", "text/ico", "application/ico", "image/x-ms-bmp",
			"image/x-bmp", "image/gif", "image/png", "image/jpeg");
	private static List<String> ICON_MIMETYPE_BLACKLIST = Arrays.asList(
			"application/xml", "text/html");

	@Inject
	HttpGetter getter;

	public byte[] fetch(String targetPath) {
		byte[] icon = getIconAtRoot(targetPath);

		if (icon == null) {
			icon = getIconInPage(targetPath);
		}

		return icon;
	}

	private byte[] getIconAtRoot(String targetPath) {
		byte[] bytes = null;
		String contentType = null;

		try {
			String url = FeedUtils.removeTrailingSlash(targetPath)
					+ "/favicon.ico";
			log.debug("getting root icon at {}", url);
			HttpResult result = getter.getBinary(url);
			bytes = result.getContent();
			contentType = result.getContentType();
		} catch (Exception e) {
			log.info("Failed to retrieve iconAtRoot: " + e.getMessage(), e);
		}

		if (!isValidIconResponse(bytes, contentType)) {
			bytes = null;
		}
		return bytes;
	}

	boolean isValidIconResponse(byte[] content, String contentType) {
		long length = content.length;

		if (!contentType.isEmpty()) {
			contentType = contentType.split(";")[0];
		}

		if (ICON_MIMETYPE_BLACKLIST.contains(contentType)) {
			log.info("Content-Type {} is blacklisted", contentType);
			return false;
		}

		if (length < MIN_ICON_LENGTH) {
			log.info("Length {} below MIN_ICON_LENGTH {}", length,
					MIN_ICON_LENGTH);
			return false;
		}

		if (length > MAX_ICON_LENGTH) {
			log.info("Length {} greater than MAX_ICON_LENGTH {}", length,
					MAX_ICON_LENGTH);
			return false;
		}

		return true;
	}

	private byte[] getIconInPage(String targetPath) {
		log.info("iconInPage, trying " + targetPath);

		Document doc;
		try {
			HttpResult result = getter.getBinary(targetPath);
			doc = Jsoup.parse(new String(result.getContent()), targetPath);
		} catch (Exception e) {
			log.info("Failed to retrieve page to find icon");
			return null;
		}

		Elements icons = doc
				.select("link[rel~=(?i)^(shortcut|icon|shortcut icon)$]");

		if (icons.isEmpty()) {
			log.info("No icon found in page");
			return null;
		}

		String href = icons.get(0).attr("abs:href");
		if (StringUtils.isBlank(href)) {
			log.info("No icon found in page");
			return null;
		}

		log.info("Found unconfirmed iconInPage at {}", href);

		byte[] bytes = null;
		String contentType = null;
		try {
			HttpResult result = getter.getBinary(href);
			bytes = result.getContent();
			contentType = result.getContentType();
		} catch (Exception e) {
			log.info("Failed to retrieve icon found in page {}", href);
			return null;
		}

		if (!isValidIconResponse(bytes, contentType)) {
			log.info("Invalid icon found for {}", href);
			return null;
		}

		return bytes;
	}

}
