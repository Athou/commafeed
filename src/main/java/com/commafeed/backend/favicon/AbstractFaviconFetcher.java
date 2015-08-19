package com.commafeed.backend.favicon;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import com.commafeed.backend.model.Feed;

@Slf4j
public abstract class AbstractFaviconFetcher {

	private static List<String> ICON_MIMETYPE_BLACKLIST = Arrays.asList("application/xml", "text/html");
	private static long MIN_ICON_LENGTH = 100;
	private static long MAX_ICON_LENGTH = 100000;

	protected static int TIMEOUT = 4000;

	public abstract Favicon fetch(Feed feed);

	protected boolean isValidIconResponse(byte[] content, String contentType) {
		if (content == null) {
			return false;
		}

		long length = content.length;

		if (StringUtils.isNotBlank(contentType)) {
			contentType = contentType.split(";")[0];
		}

		if (ICON_MIMETYPE_BLACKLIST.contains(contentType)) {
			log.debug("Content-Type {} is blacklisted", contentType);
			return false;
		}

		if (length < MIN_ICON_LENGTH) {
			log.debug("Length {} below MIN_ICON_LENGTH {}", length, MIN_ICON_LENGTH);
			return false;
		}

		if (length > MAX_ICON_LENGTH) {
			log.debug("Length {} greater than MAX_ICON_LENGTH {}", length, MAX_ICON_LENGTH);
			return false;
		}

		return true;
	}

	@RequiredArgsConstructor
	@Getter
	public static class Favicon {
		private final byte[] icon;
		private final String mediaType;
	}
}
