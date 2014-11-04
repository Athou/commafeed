package com.commafeed.backend.feed;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
	public void emptyFilterMatchesFilter() {
		FeedEntryFilter filter = new FeedEntryFilter(null);
		Assert.assertTrue(filter.matchesEntry(entry));
	}

	@Test
	public void blankFilterMatchesFilter() {
		FeedEntryFilter filter = new FeedEntryFilter("");
		Assert.assertTrue(filter.matchesEntry(entry));
	}

	@Test
	public void simpleExpression() {
		FeedEntryFilter filter = new FeedEntryFilter("author eq 'athou'");
		Assert.assertTrue(filter.matchesEntry(entry));
	}

	@Test
	public void newIsDisabled() {
		FeedEntryFilter filter = new FeedEntryFilter("null eq new ('java.lang.String', 'athou')");
		Assert.assertTrue(filter.matchesEntry(entry));
	}

	@Test
	public void getClassMethodIsDisabled() {
		FeedEntryFilter filter = new FeedEntryFilter("null eq ''.getClass()");
		Assert.assertTrue(filter.matchesEntry(entry));
	}

	@Test
	public void dotClassIsDisabled() {
		FeedEntryFilter filter = new FeedEntryFilter("null eq ''.class");
		Assert.assertTrue(filter.matchesEntry(entry));
	}

}
