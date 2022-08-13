package com.commafeed.backend.favicon;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.commafeed.backend.model.Feed;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractFaviconFetcher {

	protected static final int TIMEOUT = 4000;

	private static final List<String> ICON_MIMETYPE_BLACKLIST = Arrays.asList("application/xml", "text/html");
	private static final long MIN_ICON_LENGTH = 100;
	private static final long MAX_ICON_LENGTH = 100000;

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
