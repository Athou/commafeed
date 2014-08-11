package com.commafeed.backend.service;

import java.util.Date;

import lombok.RequiredArgsConstructor;

import org.apache.commons.codec.digest.DigestUtils;

import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.feed.FeedUtils;
import com.commafeed.backend.model.Feed;

@RequiredArgsConstructor
public class FeedService {

	private final FeedDAO feedDAO;

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

}
