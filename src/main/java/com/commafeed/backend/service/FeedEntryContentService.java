package com.commafeed.backend.service;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import com.commafeed.backend.dao.FeedEntryContentDAO;
import com.commafeed.backend.feed.FeedUtils;
import com.commafeed.backend.model.FeedEntryContent;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
@Singleton
public class FeedEntryContentService {

	private final FeedEntryContentDAO feedEntryContentDAO;

	/**
	 * this is NOT thread-safe
	 */
	public FeedEntryContent findOrCreate(FeedEntryContent content, String baseUrl) {
		content.setAuthor(FeedUtils.truncate(FeedUtils.handleContent(content.getAuthor(), baseUrl, true), 128));
		content.setTitle(FeedUtils.truncate(FeedUtils.handleContent(content.getTitle(), baseUrl, true), 2048));
		content.setContent(FeedUtils.handleContent(content.getContent(), baseUrl, false));
		content.setMediaDescription(FeedUtils.handleContent(content.getMediaDescription(), baseUrl, false));

		String contentHash = DigestUtils.sha1Hex(StringUtils.trimToEmpty(content.getContent()));
		content.setContentHash(contentHash);

		String titleHash = DigestUtils.sha1Hex(StringUtils.trimToEmpty(content.getTitle()));
		content.setTitleHash(titleHash);

		List<FeedEntryContent> existing = feedEntryContentDAO.findExisting(contentHash, titleHash);
		Optional<FeedEntryContent> equivalentContent = existing.stream().filter(c -> content.equivalentTo(c)).findFirst();
		if (equivalentContent.isPresent()) {
			return equivalentContent.get();
		}

		feedEntryContentDAO.saveOrUpdate(content);
		return content;
	}
}
