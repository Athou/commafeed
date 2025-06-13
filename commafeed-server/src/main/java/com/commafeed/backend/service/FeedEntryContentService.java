package com.commafeed.backend.service;

import java.util.Optional;

import jakarta.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import com.commafeed.backend.Digests;
import com.commafeed.backend.dao.FeedEntryContentDAO;
import com.commafeed.backend.feed.FeedUtils;
import com.commafeed.backend.feed.parser.FeedParserResult.Content;
import com.commafeed.backend.feed.parser.FeedParserResult.Enclosure;
import com.commafeed.backend.feed.parser.FeedParserResult.Media;
import com.commafeed.backend.model.FeedEntryContent;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
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
			feedEntryContentDAO.persist(entryContent);
			return entryContent;
		}
	}

	private FeedEntryContent buildContent(Content content, String baseUrl) {
		FeedEntryContent entryContent = new FeedEntryContent();
		entryContent.setTitleHash(Digests.sha1Hex(StringUtils.trimToEmpty(content.title())));
		entryContent.setContentHash(Digests.sha1Hex(StringUtils.trimToEmpty(content.content())));
		entryContent.setTitle(FeedUtils.truncate(cleaningService.clean(content.title(), baseUrl, true), 2048));
		entryContent.setContent(cleaningService.clean(content.content(), baseUrl, false));
		entryContent.setAuthor(FeedUtils.truncate(cleaningService.clean(content.author(), baseUrl, true), 128));
		entryContent.setCategories(FeedUtils.truncate(content.categories(), 4096));
		entryContent.setDirection(
				FeedUtils.isRTL(content.title(), content.content()) ? FeedEntryContent.Direction.rtl : FeedEntryContent.Direction.ltr);

		Enclosure enclosure = content.enclosure();
		if (enclosure != null) {
			entryContent.setEnclosureUrl(FeedUtils.truncate(enclosure.url(), 2048));
			entryContent.setEnclosureType(enclosure.type());
		}

		Media media = content.media();
		if (media != null) {
			entryContent.setMediaDescription(cleaningService.clean(media.description(), baseUrl, false));
			entryContent.setMediaThumbnailUrl(FeedUtils.truncate(media.thumbnailUrl(), 2048));
			entryContent.setMediaThumbnailWidth(media.thumbnailWidth());
			entryContent.setMediaThumbnailHeight(media.thumbnailHeight());
		}

		return entryContent;
	}

}
