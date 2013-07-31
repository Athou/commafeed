package com.commafeed.backend.services;

import java.util.Date;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;

import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryContent;

@Stateless
public class FeedUpdateService {

	@Inject
	FeedEntryDAO feedEntryDAO;

	@Inject
	FeedEntryContentService feedEntryContentService;

	/**
	 * this is NOT thread-safe
	 */
	public boolean addEntry(Feed feed, FeedEntry entry) {

		Long existing = feedEntryDAO.findExisting(entry.getGuid(), feed.getId());
		if (existing != null) {
			return false;
		}

		FeedEntryContent content = feedEntryContentService.findOrCreate(entry.getContent(), feed.getLink());
		entry.setGuidHash(DigestUtils.sha1Hex(entry.getGuid()));
		entry.setContent(content);
		entry.setInserted(new Date());
		entry.setFeed(feed);

		feedEntryDAO.saveOrUpdate(entry);
		return true;
	}
}
