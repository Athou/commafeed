package com.commafeed.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryContent;
import com.commafeed.backend.service.FeedEntryFilteringService.FeedEntryFilterException;

public class FeedEntryFilteringServiceTest {

	private FeedEntryFilteringService service;

	private FeedEntry entry;
	private FeedEntryContent content;

	@BeforeEach
	public void setUp() {
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
	public void emptyFilterMatchesFilter() throws FeedEntryFilterException {
		assertTrue(service.filterMatchesEntry(null, entry));
	}

	@Test
	public void blankFilterMatchesFilter() throws FeedEntryFilterException {
		assertTrue(service.filterMatchesEntry("", entry));
	}

	@Test
	public void simpleExpression() throws FeedEntryFilterException {
		assertTrue(service.filterMatchesEntry("author.toString() eq 'athou'", entry));
	}

	@Test
	public void newIsDisabled() {
		assertThrows(FeedEntryFilterException.class,
				() -> service.filterMatchesEntry("null eq new ('java.lang.String', 'athou')", entry));
	}

	@Test
	public void getClassMethodIsDisabled() {
		assertThrows(FeedEntryFilterException.class,
				() -> service.filterMatchesEntry("null eq ''.getClass()", entry));
	}

	@Test
	public void dotClassIsDisabled() throws FeedEntryFilterException {
		assertTrue(service.filterMatchesEntry("null eq ''.class", entry));
	}

	@Test
	public void cannotLoopForever() {
		assertThrows(FeedEntryFilterException.class,
				() -> service.filterMatchesEntry("while(true) {}", entry));
	}

	@Test
	public void handlesNullCorrectly() throws FeedEntryFilterException {
		entry.setUrl(null);
		entry.setContent(new FeedEntryContent());
		service.filterMatchesEntry("author eq 'athou'", entry);
	}

	@Test
	public void incorrectScriptThrowsException() {
		assertThrows(FeedEntryFilterException.class,
				() -> service.filterMatchesEntry("aa eqz bb", entry));
	}

	@Test
	public void incorrectReturnTypeThrowsException() {
		assertThrows(FeedEntryFilterException.class,
				() -> service.filterMatchesEntry("1", entry));
	}

}
