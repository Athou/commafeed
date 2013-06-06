package com.commafeed.backend.services;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;

import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.model.Feed;

@Singleton
public class FeedService {

	@Inject
	FeedDAO feedDAO;

	@Lock(LockType.WRITE)
	public Feed findOrCreate(String url) {
		Feed feed = feedDAO.findByUrl(url);
		if (feed == null) {
			feed = new Feed();
			feed.setUrl(url);
			feed.setUrlHash(DigestUtils.sha1Hex(url));
			feedDAO.saveOrUpdate(feed);
		}
		return feed;
	}

}
