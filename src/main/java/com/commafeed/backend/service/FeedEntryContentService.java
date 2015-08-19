package com.commafeed.backend.service;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.RequiredArgsConstructor;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import com.commafeed.backend.dao.FeedEntryContentDAO;
import com.commafeed.backend.feed.FeedUtils;
import com.commafeed.backend.model.FeedEntryContent;

@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
@Singleton
public class FeedEntryContentService {

	private final FeedEntryContentDAO feedEntryContentDAO;

	/**
	 * this is NOT thread-safe
	 */
	public FeedEntryContent findOrCreate(FeedEntryContent content, String baseUrl) {

		String contentHash = DigestUtils.sha1Hex(StringUtils.trimToEmpty(content.getContent()));
		String titleHash = DigestUtils.sha1Hex(StringUtils.trimToEmpty(content.getTitle()));
		Long existingId = feedEntryContentDAO.findExisting(contentHash, titleHash);

		FeedEntryContent result = null;
		if (existingId == null) {
			content.setContentHash(contentHash);
			content.setTitleHash(titleHash);

			content.setAuthor(FeedUtils.truncate(FeedUtils.handleContent(content.getAuthor(), baseUrl, true), 128));
			content.setTitle(FeedUtils.truncate(FeedUtils.handleContent(content.getTitle(), baseUrl, true), 2048));
			content.setContent(FeedUtils.handleContent(content.getContent(), baseUrl, false));
			result = content;
			feedEntryContentDAO.saveOrUpdate(result);
		} else {
			result = new FeedEntryContent();
			result.setId(existingId);
		}
		return result;
	}
}
