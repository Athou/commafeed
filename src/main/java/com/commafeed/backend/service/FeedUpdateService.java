package com.commafeed.backend.service;

import java.util.Date;

import lombok.AllArgsConstructor;

import org.apache.commons.codec.digest.DigestUtils;

import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryContent;

@AllArgsConstructor
public class FeedUpdateService {

	private final FeedEntryDAO feedEntryDAO;
	private final FeedEntryContentService feedEntryContentService;

	/**
	 * this is NOT thread-safe
	 */
	public boolean addEntry(Feed feed, FeedEntry entry) {

		Long existing = feedEntryDAO.findExisting(entry.getGuid(), feed);
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
