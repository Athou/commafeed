package com.commafeed.frontend.pages;

import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.handler.TextRequestHandler;

import com.google.common.collect.Lists;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.SyndFeedOutput;

@SuppressWarnings("serial")
public class TestRssPage extends WebPage {

	public TestRssPage() {
		SyndFeed feed = new SyndFeedImpl();
		feed.setFeedType("rss_2.0");
		feed.setTitle("Test RSS");

		feed.setLink("");
		feed.setDescription("New entries everytime it is accessed");

		List<SyndEntry> entries = Lists.newArrayList();
		for (int i = 0; i < 5; i++) {
			SyndEntry entry = new SyndEntryImpl();
			String uuid = UUID.randomUUID().toString();
			entry.setUri(uuid);
			entry.setTitle(uuid);
			entry.setLink("http://www.example.com/" + uuid);
			entry.setPublishedDate(new Date());
			entries.add(entry);
		}
		feed.setEntries(entries);
		SyndFeedOutput output = new SyndFeedOutput();

		StringWriter writer = new StringWriter();
		try {
			output.output(feed, writer);
		} catch (Exception e) {
			writer.write("Could not get feed information");
		}

		try {
			// simulate internet lag
			Thread.sleep(Math.abs(new Random().nextLong() % 5000));
		} catch (InterruptedException e) {
			// do nothing
		}
		getRequestCycle().scheduleRequestHandlerAfterCurrent(
				new TextRequestHandler("text/xml", "UTF-8", writer.toString()));
	}

}
