package com.commafeed.backend.service;

import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryContent;
import com.commafeed.backend.service.FeedEntryFilteringService.FeedEntryFilterException;

class FeedEntryFilteringServiceTest {
	private CommaFeedConfiguration config;

	private FeedEntryFilteringService service;

	private FeedEntry entry;

	@BeforeEach
	void init() {
		config = Mockito.mock(CommaFeedConfiguration.class, Mockito.RETURNS_DEEP_STUBS);
		Mockito.when(config.feedRefresh().filteringExpressionEvaluationTimeout()).thenReturn(Duration.ofSeconds(30));

		service = new FeedEntryFilteringService(config);

		entry = new FeedEntry();
		entry.setUrl("https://github.com/Athou/commafeed");

		FeedEntryContent content = new FeedEntryContent();
		content.setAuthor("Athou");
		content.setTitle("Merge pull request #662 from Athou/dw8");
		content.setContent("Merge pull request #662 from Athou/dw8");
		entry.setContent(content);

	}

	@Test
	void emptyFilterMatchesFilter() throws FeedEntryFilterException {
		Assertions.assertTrue(service.filterMatchesEntry(null, entry));
	}

	@Test
	void blankFilterMatchesFilter() throws FeedEntryFilterException {
		Assertions.assertTrue(service.filterMatchesEntry("", entry));
	}

	@Test
	void simpleExpression() throws FeedEntryFilterException {
		Assertions.assertTrue(service.filterMatchesEntry("author.toString() eq 'athou'", entry));
	}

	@Test
	void newIsDisabled() {
		Assertions.assertThrows(FeedEntryFilterException.class,
				() -> service.filterMatchesEntry("null eq new ('java.lang.String', 'athou')", entry));
	}

	@Test
	void getClassMethodIsDisabled() {
		Assertions.assertThrows(FeedEntryFilterException.class, () -> service.filterMatchesEntry("null eq ''.getClass()", entry));
	}

	@Test
	void dotClassIsDisabled() throws FeedEntryFilterException {
		Assertions.assertTrue(service.filterMatchesEntry("null eq ''.class", entry));
	}

	@Test
	void cannotLoopForever() {
		Mockito.when(config.feedRefresh().filteringExpressionEvaluationTimeout()).thenReturn(Duration.ofMillis(200));
		service = new FeedEntryFilteringService(config);

		Assertions.assertThrows(FeedEntryFilterException.class, () -> service.filterMatchesEntry("while(true) {}", entry));
	}

	@Test
	void handlesNullCorrectly() {
		entry.setUrl(null);
		entry.setContent(new FeedEntryContent());
		Assertions.assertDoesNotThrow(() -> service.filterMatchesEntry("author eq 'athou'", entry));
	}

	@Test
	void incorrectScriptThrowsException() {
		Assertions.assertThrows(FeedEntryFilterException.class, () -> service.filterMatchesEntry("aa eqz bb", entry));
	}

	@Test
	void incorrectReturnTypeThrowsException() {
		Assertions.assertThrows(FeedEntryFilterException.class, () -> service.filterMatchesEntry("1", entry));
	}

}
