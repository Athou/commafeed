package com.commafeed.backend.service;

import java.util.Date;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.RequiredArgsConstructor;

import org.apache.commons.codec.digest.DigestUtils;

import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.favicon.AbstractFaviconFetcher;
import com.commafeed.backend.feed.FeedUtils;
import com.commafeed.backend.model.Feed;

@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
@Singleton
public class FeedService {

	private final FeedDAO feedDAO;
	private final Set<AbstractFaviconFetcher> faviconFetchers;

	public synchronized Feed findOrCreate(String url) {
		String normalized = FeedUtils.normalizeURL(url);
		Feed feed = feedDAO.findByUrl(normalized);
		if (feed == null) {
			feed = new Feed();
			feed.setUrl(url);
			feed.setNormalizedUrl(normalized);
			feed.setNormalizedUrlHash(DigestUtils.sha1Hex(normalized));
			feed.setDisabledUntil(new Date(0));
			feedDAO.saveOrUpdate(feed);
		}
		return feed;
	}

	public byte[] fetchFavicon(Feed feed) {
		String url = feed.getLink() != null ? feed.getLink() : feed.getUrl();

		byte[] icon = null;
		for (AbstractFaviconFetcher faviconFetcher : faviconFetchers) {
			icon = faviconFetcher.fetch(url);
			if (icon != null) {
				break;
			}
		}
		return icon;
	}

}
