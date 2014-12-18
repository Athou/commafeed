package com.commafeed.backend.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryContent;
import com.commafeed.backend.service.FeedEntryFilteringService.FeedEntryFilterException;

public class FeedEntryFilteringServiceTest {

	private FeedEntryFilteringService service;

	private FeedEntry entry;
	private FeedEntryContent content;

	@Before
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
	public void emptyFilterMatchesFilter() throws FeedEntryFilterException {
		Assert.assertTrue(service.filterMatchesEntry(null, entry));
	}

	@Test
	public void blankFilterMatchesFilter() throws FeedEntryFilterException {
		Assert.assertTrue(service.filterMatchesEntry("", entry));
	}

	@Test
	public void simpleExpression() throws FeedEntryFilterException {
		Assert.assertTrue(service.filterMatchesEntry("author.toString() eq 'athou'", entry));
	}

	@Test(expected = FeedEntryFilterException.class)
	public void newIsDisabled() throws FeedEntryFilterException {
		service.filterMatchesEntry("null eq new ('java.lang.String', 'athou')", entry);
	}

	@Test(expected = FeedEntryFilterException.class)
	public void getClassMethodIsDisabled() throws FeedEntryFilterException {
		service.filterMatchesEntry("null eq ''.getClass()", entry);
	}

	@Test
	public void dotClassIsDisabled() throws FeedEntryFilterException {
		Assert.assertTrue(service.filterMatchesEntry("null eq ''.class", entry));
	}

	@Test(expected = FeedEntryFilterException.class)
	public void cannotLoopForever() throws FeedEntryFilterException {
		service.filterMatchesEntry("while(true) {}", entry);
	}

	@Test
	public void handlesNullCorrectly() throws FeedEntryFilterException {
		entry.setUrl(null);
		entry.setContent(new FeedEntryContent());
		service.filterMatchesEntry("author eq 'athou'", entry);
	}

	@Test(expected = FeedEntryFilterException.class)
	public void incorrectScriptThrowsException() throws FeedEntryFilterException {
		service.filterMatchesEntry("aa eqz bb", entry);
	}

	@Test(expected = FeedEntryFilterException.class)
	public void incorrectReturnTypeThrowsException() throws FeedEntryFilterException {
		service.filterMatchesEntry("1", entry);
	}

}
