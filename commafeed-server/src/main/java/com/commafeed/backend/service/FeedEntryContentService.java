package com.commafeed.backend.service;

import java.util.Optional;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import com.commafeed.backend.dao.FeedEntryContentDAO;
import com.commafeed.backend.feed.FeedUtils;
import com.commafeed.backend.feed.parser.FeedParserResult.Content;
import com.commafeed.backend.feed.parser.FeedParserResult.Enclosure;
import com.commafeed.backend.feed.parser.FeedParserResult.Media;
import com.commafeed.backend.model.FeedEntryContent;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
@Singleton
public class FeedEntryContentService {

	private final FeedEntryContentDAO feedEntryContentDAO;
	private final FeedEntryContentCleaningService cleaningService;

	/**
	 * this is NOT thread-safe
	 */
	public FeedEntryContent findOrCreate(Content content, String baseUrl) {
		FeedEntryContent entryContent = buildContent(content, baseUrl);
		Optional<FeedEntryContent> existing = feedEntryContentDAO.findExisting(entryContent.getContentHash(), entryContent.getTitleHash())
				.stream()
				.filter(entryContent::equivalentTo)
				.findFirst();
		if (existing.isPresent()) {
			return existing.get();
		} else {
			feedEntryContentDAO.saveOrUpdate(entryContent);
			return entryContent;
		}
	}

	private FeedEntryContent buildContent(Content content, String baseUrl) {
		FeedEntryContent entryContent = new FeedEntryContent();
		entryContent.setTitleHash(DigestUtils.sha1Hex(StringUtils.trimToEmpty(content.title())));
		entryContent.setContentHash(DigestUtils.sha1Hex(StringUtils.trimToEmpty(content.content())));
		entryContent.setTitle(FeedUtils.truncate(cleaningService.clean(content.title(), baseUrl, true), 2048));
		entryContent.setContent(cleaningService.clean(content.content(), baseUrl, false));
		entryContent.setAuthor(FeedUtils.truncate(cleaningService.clean(content.author(), baseUrl, true), 128));
		entryContent.setCategories(FeedUtils.truncate(content.categories(), 4096));

		Enclosure enclosure = content.enclosure();
		if (enclosure != null) {
			entryContent.setEnclosureUrl(enclosure.url());
			entryContent.setEnclosureType(enclosure.type());
		}

		Media media = content.media();
		if (media != null) {
			entryContent.setMediaDescription(cleaningService.clean(media.description(), baseUrl, false));
			entryContent.setMediaThumbnailUrl(media.thumbnailUrl());
			entryContent.setMediaThumbnailWidth(media.thumbnailWidth());
			entryContent.setMediaThumbnailHeight(media.thumbnailHeight());
		}

		return entryContent;
	}

}
