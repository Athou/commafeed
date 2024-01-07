package com.commafeed.backend.service;

import java.util.List;
import java.util.Optional;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;

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
		String title = FeedUtils.truncate(cleaningService.clean(content.title(), baseUrl, true), 2048);
		String titleHash = DigestUtils.sha1Hex(StringUtils.trimToEmpty(title));

		String contentString = cleaningService.clean(content.content(), baseUrl, false);
		String contentHash = DigestUtils.sha1Hex(StringUtils.trimToEmpty(contentString));

		List<FeedEntryContent> existing = feedEntryContentDAO.findExisting(contentHash, titleHash);
		Optional<FeedEntryContent> equivalentContent = existing.stream()
				.filter(c -> isEquivalent(c, content, title, contentString))
				.findFirst();
		if (equivalentContent.isPresent()) {
			return equivalentContent.get();
		}

		FeedEntryContent entryContent = new FeedEntryContent();
		entryContent.setTitle(title);
		entryContent.setTitleHash(titleHash);
		entryContent.setContent(contentString);
		entryContent.setContentHash(contentHash);
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

		feedEntryContentDAO.saveOrUpdate(entryContent);
		return entryContent;
	}

	private boolean isEquivalent(FeedEntryContent content, Content c, String title, String contentString) {
		EqualsBuilder builder = new EqualsBuilder().append(content.getTitle(), title)
				.append(content.getContent(), contentString)
				.append(content.getAuthor(), c.author())
				.append(content.getCategories(), c.categories());

		if (c.enclosure() != null) {
			builder.append(content.getEnclosureUrl(), c.enclosure().url()).append(content.getEnclosureType(), c.enclosure().type());
		}

		if (c.media() != null) {
			builder.append(content.getMediaDescription(), c.media().description())
					.append(content.getMediaThumbnailUrl(), c.media().thumbnailUrl())
					.append(content.getMediaThumbnailWidth(), c.media().thumbnailWidth())
					.append(content.getMediaThumbnailHeight(), c.media().thumbnailHeight());
		}

		return builder.build();
	}

}
