package com.commafeed.backend.service;

import java.time.Instant;

import jakarta.inject.Singleton;

import com.commafeed.backend.Digests;
import com.commafeed.backend.Urls;
import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.feed.FeedUtils;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.Models;

import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor
public class FeedService {

	private final FeedDAO feedDAO;

	public synchronized Feed findOrCreate(String url) {
		String normalizedUrl = Urls.normalize(url);
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
		String normalized = Urls.normalize(feed.getUrl());
		feed.setNormalizedUrl(normalized);
		feed.setNormalizedUrlHash(Digests.sha1Hex(normalized));
		feed.setLastUpdated(Instant.now());
		feed.setEtagHeader(FeedUtils.truncate(feed.getEtagHeader(), 255));
		feedDAO.merge(feed);
	}

}
