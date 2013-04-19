package com.commafeed.backend.feeds;

import java.io.ByteArrayInputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.xml.sax.InputSource;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryContent;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEnclosure;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;

public class FeedParser {

	private static final Function<SyndContent, String> CONTENT_TO_STRING = new Function<SyndContent, String>() {
		public String apply(SyndContent content) {
			return content.getValue();
		}
	};

	@SuppressWarnings("unchecked")
	public Feed parse(String feedUrl, byte[] xml) throws FeedException {
		Feed feed = new Feed();
		feed.setLastUpdated(Calendar.getInstance().getTime());

		try {
			InputSource source = new InputSource(new ByteArrayInputStream(xml));
			if (new String(ArrayUtils.subarray(xml, 0, 100)).split(SystemUtils.LINE_SEPARATOR)[0]
					.toUpperCase().contains("ISO-8859-1")) {
				// they probably use word, we need to handle curly quotes and
				// other word special characters
				source.setEncoding("windows-1252");
			}
			SyndFeed rss = new SyndFeedInput().build(source);
			feed.setUrl(feedUrl);
			feed.setTitle(rss.getTitle());
			feed.setLink(rss.getLink());
			List<SyndEntry> items = rss.getEntries();
			for (SyndEntry item : items) {
				FeedEntry entry = new FeedEntry();
				entry.setGuid(item.getUri());
				entry.setGuidHash(DigestUtils.sha1Hex(item.getUri()));
				entry.setUrl(item.getLink());
				entry.setUpdated(getUpdateDate(item));

				FeedEntryContent content = new FeedEntryContent();
				content.setContent(getContent(item));
				content.setTitle(item.getTitle());
				SyndEnclosure enclosure = (SyndEnclosure) Iterables.getFirst(
						item.getEnclosures(), null);
				if (enclosure != null) {
					content.setEnclosureUrl(enclosure.getUrl());
					content.setEnclosureType(enclosure.getType());
				}
				entry.setContent(content);

				feed.getEntries().add(entry);
			}
		} catch (Exception e) {
			throw new FeedException(String.format(
					"Could not parse feed from %s : %s", feedUrl,
					e.getMessage()), e);
		}
		return feed;
	}

	private Date getUpdateDate(SyndEntry item) {
		Date date = item.getUpdatedDate();
		if (date == null) {
			date = item.getPublishedDate();
		}
		if (date == null) {
			date = new Date();
		}
		return date;
	}

	@SuppressWarnings("unchecked")
	private String getContent(SyndEntry item) {
		String content = null;
		if (item.getContents().isEmpty()) {
			content = item.getDescription() == null ? null : item
					.getDescription().getValue();
		} else {
			content = StringUtils.join(Collections2.transform(
					item.getContents(), CONTENT_TO_STRING),
					SystemUtils.LINE_SEPARATOR);
		}
		return content;
	}

}
