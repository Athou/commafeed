package com.commafeed.backend.feeds;

import java.io.StringReader;
import java.util.Calendar;
import java.util.List;

import javax.ejb.Stateless;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.sun.syndication.feed.synd.SyndContent;
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
				entry.setContent(getContent(item));
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

	@SuppressWarnings("unchecked")
	private String getContent(SyndEntry item) {
		String content = null;
		if (item.getContents().isEmpty()) {
			content = item.getDescription() == null ? null : item
					.getDescription().getValue();
		} else {
			content = StringUtils.join(Collections2.transform(
					item.getContents(), new Function<SyndContent, String>() {
						public String apply(SyndContent content) {
							return content.getValue();
						}
					}), SystemUtils.LINE_SEPARATOR);
		}
		content = handleContent(content);
		return content;
	}

	private String handleContent(String content) {
		Document doc = Jsoup.parse(content, "UTF-8");
		doc.select("a").attr("target", "_blank");
		return doc.outerHtml();
	}
}
