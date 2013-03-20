package com.commafeed.backend.feeds;

import java.io.StringReader;
import java.util.Calendar;
import java.util.List;

import javax.ejb.Stateless;

import com.commafeed.model.Feed;
import com.commafeed.model.FeedEntry;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;

@Stateless
public class FeedParser {

	@SuppressWarnings("unchecked")
	public Feed parse(String feedUrl, String xml) throws FeedException {

		Feed feed = new Feed();
		feed.setUrl(feedUrl);
		feed.setLastUpdated(Calendar.getInstance().getTime());

		try {
			SyndFeed rss = new SyndFeedInput().build(new StringReader(xml));

			List<SyndEntry> items = rss.getEntries();
			for (SyndEntry item : items) {
				FeedEntry entry = new FeedEntry();
				entry.setGuid(item.getUri());
				entry.setTitle(item.getTitle());
				entry.setContent(item.getDescription() == null ? null : item
						.getDescription().getValue());
				entry.setUrl(item.getLink());
				entry.setUpdated(item.getUpdatedDate() != null ? item
						.getUpdatedDate() : item.getPublishedDate());

				feed.getEntries().add(entry);
			}
		} catch (Exception e) {
			throw new FeedException("Could not parse feed : " + e.getMessage(),
					e);
		}
		return feed;
	}

}
