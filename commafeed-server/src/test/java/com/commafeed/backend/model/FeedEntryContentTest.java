package com.commafeed.backend.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class FeedEntryContentTest {

	@Nested
	class EquivalentTo {

		@Test
		void shouldReturnFalseWhenComparedWithNull() {
			Assertions.assertFalse(new FeedEntryContent().equivalentTo(null));
		}

		@Test
		void shouldReturnTrueWhenComparedWithIdenticalContent() {
			FeedEntryContent content1 = createContent("title", "content", "author", "categories", "enclosureUrl", "enclosureType",
					"mediaDescription", "mediaThumbnailUrl", 10, 20);
			FeedEntryContent content2 = createContent("title", "content", "author", "categories", "enclosureUrl", "enclosureType",
					"mediaDescription", "mediaThumbnailUrl", 10, 20);
			Assertions.assertTrue(content1.equivalentTo(content2));
		}

		@Test
		void shouldReturnFalseWhenTitleDiffers() {
			FeedEntryContent content1 = createContent("title1", "content", "author", "categories", "enclosureUrl", "enclosureType",
					"mediaDescription", "mediaThumbnailUrl", 10, 20);
			FeedEntryContent content2 = createContent("title2", "content", "author", "categories", "enclosureUrl", "enclosureType",
					"mediaDescription", "mediaThumbnailUrl", 10, 20);
			Assertions.assertFalse(content1.equivalentTo(content2));
		}

		@Test
		void shouldReturnFalseWhenContentDiffers() {
			FeedEntryContent content1 = createContent("title", "content1", "author", "categories", "enclosureUrl", "enclosureType",
					"mediaDescription", "mediaThumbnailUrl", 10, 20);
			FeedEntryContent content2 = createContent("title", "content2", "author", "categories", "enclosureUrl", "enclosureType",
					"mediaDescription", "mediaThumbnailUrl", 10, 20);
			Assertions.assertFalse(content1.equivalentTo(content2));
		}

		@Test
		void shouldReturnFalseWhenAuthorDiffers() {
			FeedEntryContent content1 = createContent("title", "content", "author1", "categories", "enclosureUrl", "enclosureType",
					"mediaDescription", "mediaThumbnailUrl", 10, 20);
			FeedEntryContent content2 = createContent("title", "content", "author2", "categories", "enclosureUrl", "enclosureType",
					"mediaDescription", "mediaThumbnailUrl", 10, 20);
			Assertions.assertFalse(content1.equivalentTo(content2));
		}

		@Test
		void shouldReturnFalseWhenCategoriesDiffer() {
			FeedEntryContent content1 = createContent("title", "content", "author", "categories1", "enclosureUrl", "enclosureType",
					"mediaDescription", "mediaThumbnailUrl", 10, 20);
			FeedEntryContent content2 = createContent("title", "content", "author", "categories2", "enclosureUrl", "enclosureType",
					"mediaDescription", "mediaThumbnailUrl", 10, 20);
			Assertions.assertFalse(content1.equivalentTo(content2));
		}

		@Test
		void shouldReturnFalseWhenEnclosureUrlDiffers() {
			FeedEntryContent content1 = createContent("title", "content", "author", "categories", "enclosureUrl1", "enclosureType",
					"mediaDescription", "mediaThumbnailUrl", 10, 20);
			FeedEntryContent content2 = createContent("title", "content", "author", "categories", "enclosureUrl2", "enclosureType",
					"mediaDescription", "mediaThumbnailUrl", 10, 20);
			Assertions.assertFalse(content1.equivalentTo(content2));
		}

		@Test
		void shouldReturnFalseWhenEnclosureTypeDiffers() {
			FeedEntryContent content1 = createContent("title", "content", "author", "categories", "enclosureUrl", "enclosureType1",
					"mediaDescription", "mediaThumbnailUrl", 10, 20);
			FeedEntryContent content2 = createContent("title", "content", "author", "categories", "enclosureUrl", "enclosureType2",
					"mediaDescription", "mediaThumbnailUrl", 10, 20);
			Assertions.assertFalse(content1.equivalentTo(content2));
		}

		@Test
		void shouldReturnFalseWhenMediaDescriptionDiffers() {
			FeedEntryContent content1 = createContent("title", "content", "author", "categories", "enclosureUrl", "enclosureType",
					"mediaDescription1", "mediaThumbnailUrl", 10, 20);
			FeedEntryContent content2 = createContent("title", "content", "author", "categories", "enclosureUrl", "enclosureType",
					"mediaDescription2", "mediaThumbnailUrl", 10, 20);
			Assertions.assertFalse(content1.equivalentTo(content2));
		}

		@Test
		void shouldReturnFalseWhenMediaThumbnailUrlDiffers() {
			FeedEntryContent content1 = createContent("title", "content", "author", "categories", "enclosureUrl", "enclosureType",
					"mediaDescription", "mediaThumbnailUrl1", 10, 20);
			FeedEntryContent content2 = createContent("title", "content", "author", "categories", "enclosureUrl", "enclosureType",
					"mediaDescription", "mediaThumbnailUrl2", 10, 20);
			Assertions.assertFalse(content1.equivalentTo(content2));
		}

		@Test
		void shouldReturnFalseWhenMediaThumbnailWidthDiffers() {
			FeedEntryContent content1 = createContent("title", "content", "author", "categories", "enclosureUrl", "enclosureType",
					"mediaDescription", "mediaThumbnailUrl", 10, 20);
			FeedEntryContent content2 = createContent("title", "content", "author", "categories", "enclosureUrl", "enclosureType",
					"mediaDescription", "mediaThumbnailUrl", 15, 20);
			Assertions.assertFalse(content1.equivalentTo(content2));
		}

		@Test
		void shouldReturnFalseWhenMediaThumbnailHeightDiffers() {
			FeedEntryContent content1 = createContent("title", "content", "author", "categories", "enclosureUrl", "enclosureType",
					"mediaDescription", "mediaThumbnailUrl", 10, 20);
			FeedEntryContent content2 = createContent("title", "content", "author", "categories", "enclosureUrl", "enclosureType",
					"mediaDescription", "mediaThumbnailUrl", 10, 25);
			Assertions.assertFalse(content1.equivalentTo(content2));
		}

		@Test
		void shouldReturnTrueWhenNullFieldsAreEqual() {
			FeedEntryContent content1 = new FeedEntryContent();
			FeedEntryContent content2 = new FeedEntryContent();
			Assertions.assertTrue(content1.equivalentTo(content2));
		}

		private FeedEntryContent createContent(String title, String content, String author, String categories, String enclosureUrl,
				String enclosureType, String mediaDescription, String mediaThumbnailUrl, Integer mediaThumbnailWidth,
				Integer mediaThumbnailHeight) {
			FeedEntryContent feedEntryContent = new FeedEntryContent();
			feedEntryContent.setTitle(title);
			feedEntryContent.setContent(content);
			feedEntryContent.setAuthor(author);
			feedEntryContent.setCategories(categories);
			feedEntryContent.setEnclosureUrl(enclosureUrl);
			feedEntryContent.setEnclosureType(enclosureType);
			feedEntryContent.setMediaDescription(mediaDescription);
			feedEntryContent.setMediaThumbnailUrl(mediaThumbnailUrl);
			feedEntryContent.setMediaThumbnailWidth(mediaThumbnailWidth);
			feedEntryContent.setMediaThumbnailHeight(mediaThumbnailHeight);
			return feedEntryContent;
		}
	}
}