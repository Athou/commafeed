package com.commafeed.backend.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryContent;
import com.commafeed.backend.service.FeedEntryFilteringService.FeedEntryFilterException;

class FeedEntryFilteringServiceTest {

	private FeedEntryFilteringService service;

	private FeedEntry entry;
	private FeedEntryContent content;

	@BeforeEach
	public void init() {
		service = new FeedEntryFilteringService();

		entry = new FeedEntry();
		entry.setUrl("https://github.com/Athou/commafeed");

		content = new FeedEntryContent();
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
	void newIsDisabled() throws FeedEntryFilterException {
		Assertions.assertThrows(FeedEntryFilterException.class,
				() -> service.filterMatchesEntry("null eq new ('java.lang.String', 'athou')", entry));
	}

	@Test
	void getClassMethodIsDisabled() throws FeedEntryFilterException {
		Assertions.assertThrows(FeedEntryFilterException.class, () -> service.filterMatchesEntry("null eq ''.getClass()", entry));
	}

	@Test
	void dotClassIsDisabled() throws FeedEntryFilterException {
		Assertions.assertTrue(service.filterMatchesEntry("null eq ''.class", entry));
	}

	@Test
	void cannotLoopForever() throws FeedEntryFilterException {
		Assertions.assertThrows(FeedEntryFilterException.class, () -> service.filterMatchesEntry("while(true) {}", entry));
	}

	@Test
	void handlesNullCorrectly() throws FeedEntryFilterException {
		entry.setUrl(null);
		entry.setContent(new FeedEntryContent());
		Assertions.assertDoesNotThrow(() -> service.filterMatchesEntry("author eq 'athou'", entry));
	}

	@Test
	void incorrectScriptThrowsException() throws FeedEntryFilterException {
		Assertions.assertThrows(FeedEntryFilterException.class, () -> service.filterMatchesEntry("aa eqz bb", entry));
	}

	@Test
	void incorrectReturnTypeThrowsException() throws FeedEntryFilterException {
		Assertions.assertThrows(FeedEntryFilterException.class, () -> service.filterMatchesEntry("1", entry));
	}

}
