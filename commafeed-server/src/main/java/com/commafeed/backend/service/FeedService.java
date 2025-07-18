package com.commafeed.backend.service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import jakarta.inject.Singleton;

import com.commafeed.backend.Digests;
import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.favicon.AbstractFaviconFetcher;
import com.commafeed.backend.favicon.Favicon;
import com.commafeed.backend.feed.FeedUtils;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.Models;
import com.google.common.io.Resources;

import io.quarkus.arc.All;

@Singleton
public class FeedService {

	private final FeedDAO feedDAO;
	private final List<AbstractFaviconFetcher> faviconFetchers;
	private final Favicon defaultFavicon;

	public FeedService(FeedDAO feedDAO, @All List<AbstractFaviconFetcher> faviconFetchers) throws IOException {
		this.feedDAO = feedDAO;
		this.faviconFetchers = faviconFetchers;
		this.defaultFavicon = new Favicon(
				Resources.toByteArray(Objects.requireNonNull(getClass().getResource("/images/default_favicon.gif"))), "image/gif");
	}

	public synchronized Feed findOrCreate(String url) {
		String normalizedUrl = FeedUtils.normalizeURL(url);
		String normalizedUrlHash = Digests.sha1Hex(normalizedUrl);
		Feed feed = feedDAO.findByUrl(normalizedUrl, normalizedUrlHash);
		if (feed == null) {
			feed = new Feed();
			feed.setUrl(url);
			feed.setNormalizedUrl(normalizedUrl);
			feed.setNormalizedUrlHash(normalizedUrlHash);
			feed.setDisabledUntil(Models.MINIMUM_INSTANT);
			feedDAO.persist(feed);
		}
		return feed;
	}

	public void update(Feed feed) {
		String normalized = FeedUtils.normalizeURL(feed.getUrl());
		feed.setNormalizedUrl(normalized);
		feed.setNormalizedUrlHash(Digests.sha1Hex(normalized));
		feed.setLastUpdated(Instant.now());
		feed.setEtagHeader(FeedUtils.truncate(feed.getEtagHeader(), 255));
		feedDAO.merge(feed);
	}

	public Favicon fetchFavicon(Feed feed) {

		Favicon icon = null;
		for (AbstractFaviconFetcher faviconFetcher : faviconFetchers) {
			icon = faviconFetcher.fetch(feed);
			if (icon != null) {
				break;
			}
		}
		if (icon == null) {
			icon = defaultFavicon;
		}
		return icon;
	}

}
