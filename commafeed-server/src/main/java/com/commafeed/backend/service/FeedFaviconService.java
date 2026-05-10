package com.commafeed.backend.service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import jakarta.inject.Singleton;
import jakarta.ws.rs.core.MediaType;

import org.apache.commons.lang3.ArrayUtils;

import com.commafeed.backend.favicon.Favicon;
import com.commafeed.backend.favicon.FaviconFetcher;
import com.commafeed.backend.model.Feed;
import com.google.common.io.Resources;

import io.quarkus.arc.All;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class FeedFaviconService {

	private static final Set<MediaType> ICON_MIMETYPE_BLACKLIST = Set.of(MediaType.APPLICATION_XML_TYPE, MediaType.TEXT_HTML_TYPE);
	private static final long MIN_ICON_LENGTH = 100;
	private static final long MAX_ICON_LENGTH = 100000;

	private final List<FaviconFetcher> faviconFetchers;
	private final Favicon defaultFavicon;

	public FeedFaviconService(@All List<FaviconFetcher> faviconFetchers) throws IOException {
		this.faviconFetchers = faviconFetchers;
		this.defaultFavicon = new Favicon(
				Resources.toByteArray(Objects.requireNonNull(getClass().getResource("/images/default_favicon.gif"))), "image/gif");
	}

	public Favicon fetchFavicon(Feed feed) {
		for (FaviconFetcher faviconFetcher : faviconFetchers) {
			Favicon icon = faviconFetcher.fetch(feed);
			if (isFaviconValid(icon)) {
				return icon;
			}
		}
		return defaultFavicon;
	}

	private static boolean isFaviconValid(Favicon favicon) {
		if (favicon == null || ArrayUtils.isEmpty(favicon.icon())) {
			return false;
		}

		long length = favicon.icon().length;
		if (length < MIN_ICON_LENGTH) {
			log.debug("Length {} below MIN_ICON_LENGTH {}", length, MIN_ICON_LENGTH);
			return false;
		}

		if (length > MAX_ICON_LENGTH) {
			log.debug("Length {} greater than MAX_ICON_LENGTH {}", length, MAX_ICON_LENGTH);
			return false;
		}

		if (ICON_MIMETYPE_BLACKLIST.stream().anyMatch(bl -> bl.isCompatible(favicon.mediaType()))) {
			log.debug("Content-Type {} is blacklisted", favicon.mediaType());
			return false;
		}

		return true;
	}
}
