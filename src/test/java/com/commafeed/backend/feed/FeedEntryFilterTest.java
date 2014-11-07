package com.commafeed.backend.feed;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.commafeed.backend.feed.FeedEntryFilter.FeedEntryFilterException;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryContent;

public class FeedEntryFilterTest {

	private FeedEntry entry;
	private FeedEntryContent content;

	@Before
	public void init() {
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
		FeedEntryFilter filter = new FeedEntryFilter(null);
		Assert.assertTrue(filter.matchesEntry(entry));
	}

	@Test
	public void blankFilterMatchesFilter() throws FeedEntryFilterException {
		FeedEntryFilter filter = new FeedEntryFilter("");
		Assert.assertTrue(filter.matchesEntry(entry));
	}

	@Test
	public void simpleExpression() throws FeedEntryFilterException {
		FeedEntryFilter filter = new FeedEntryFilter("author eq 'athou'");
		Assert.assertTrue(filter.matchesEntry(entry));
	}

	@Test(expected = FeedEntryFilterException.class)
	public void newIsDisabled() throws FeedEntryFilterException {
		FeedEntryFilter filter = new FeedEntryFilter("null eq new ('java.lang.String', 'athou')");
		filter.matchesEntry(entry);
	}

	@Test(expected = FeedEntryFilterException.class)
	public void getClassMethodIsDisabled() throws FeedEntryFilterException {
		FeedEntryFilter filter = new FeedEntryFilter("null eq ''.getClass()");
		filter.matchesEntry(entry);
	}

	@Test
	public void dotClassIsDisabled() throws FeedEntryFilterException {
		FeedEntryFilter filter = new FeedEntryFilter("null eq ''.class");
		Assert.assertTrue(filter.matchesEntry(entry));
	}

	@Test(expected = FeedEntryFilterException.class)
	public void cannotLoopForever() throws FeedEntryFilterException {
		FeedEntryFilter filter = new FeedEntryFilter("while(true) {}");
		filter.matchesEntry(entry);
	}

}
