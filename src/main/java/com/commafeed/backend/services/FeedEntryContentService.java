package com.commafeed.backend.services;

import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;

import com.commafeed.backend.dao.FeedEntryContentDAO;
import com.commafeed.backend.feeds.FeedUtils;
import com.commafeed.backend.model.FeedEntryContent;

public class FeedEntryContentService {

	@Inject
	FeedEntryContentDAO feedEntryContentDAO;

	/**
	 * this is NOT thread-safe
	 */
	public FeedEntryContent findOrCreate(FeedEntryContent content, String baseUrl) {
		
		String contentHash = DigestUtils.sha1Hex(StringUtils.trimToEmpty(content.getContent()));
		String titleHash = DigestUtils.sha1Hex(StringUtils.trimToEmpty(content.getTitle()));
		FeedEntryContent existing = feedEntryContentDAO.findExisting(contentHash, titleHash);
		if (existing == null) {
			content.setContentHash(contentHash);
			content.setTitleHash(titleHash);

			content.setAuthor(FeedUtils.truncate(FeedUtils.handleContent(content.getAuthor(), baseUrl, true), 128));
			content.setTitle(FeedUtils.truncate(FeedUtils.handleContent(content.getTitle(), baseUrl, true), 2048));
			content.setContent(FeedUtils.handleContent(content.getContent(), baseUrl, false));
			existing = content;
			feedEntryContentDAO.saveOrUpdate(existing);
		}
		return existing;
	}
}
