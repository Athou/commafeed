package com.commafeed.backend.service;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.favicon.AbstractFaviconFetcher;
import com.commafeed.backend.favicon.Favicon;
import com.commafeed.backend.feed.FeedUtils;
import com.commafeed.backend.model.Feed;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class FeedService {

	private final FeedDAO feedDAO;
	private final Set<AbstractFaviconFetcher> faviconFetchers;

	private final Favicon defaultFavicon;

	@Inject
	public FeedService(FeedDAO feedDAO, Set<AbstractFaviconFetcher> faviconFetchers) {
		this.feedDAO = feedDAO;
		this.faviconFetchers = faviconFetchers;

		try {
			defaultFavicon = new Favicon(IOUtils.toByteArray(getClass().getResource("/images/default_favicon.gif")), "image/gif");
		} catch (IOException e) {
			throw new RuntimeException("could not load default favicon", e);
		}
	}

	public synchronized Feed findOrCreate(String url) {
		String normalizedUrl = FeedUtils.normalizeURL(url);
		String normalizedUrlHash = DigestUtils.sha1Hex(normalizedUrl);
		Feed feed = feedDAO.findByUrl(normalizedUrl, normalizedUrlHash);
		if (feed == null) {
			feed = new Feed();
			feed.setUrl(url);
			feed.setNormalizedUrl(normalizedUrl);
			feed.setNormalizedUrlHash(normalizedUrlHash);
			feed.setDisabledUntil(new Date(0));
			feedDAO.saveOrUpdate(feed);
		}
		return feed;
	}

	public void save(Feed feed) {
		String normalized = FeedUtils.normalizeURL(feed.getUrl());
		feed.setNormalizedUrl(normalized);
		feed.setNormalizedUrlHash(DigestUtils.sha1Hex(normalized));
		feed.setLastUpdated(new Date());
		feed.setEtagHeader(FeedUtils.truncate(feed.getEtagHeader(), 255));
		feedDAO.saveOrUpdate(feed);
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
