package com.commafeed.backend.feeds;

import java.io.StringReader;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
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

	private static final Date START = new Date(0);
	private static final Date END = new Date(1000l * Integer.MAX_VALUE);

	private static final Function<SyndContent, String> CONTENT_TO_STRING = new Function<SyndContent, String>() {
		public String apply(SyndContent content) {
			return content.getValue();
		}
	};

	@SuppressWarnings("unchecked")
	public FetchedFeed parse(String feedUrl, byte[] xml) throws FeedException {
		FetchedFeed fetchedFeed = new FetchedFeed();
		Feed feed = fetchedFeed.getFeed();
		Collection<FeedEntry> entries = fetchedFeed.getEntries();
		feed.setLastUpdated(Calendar.getInstance().getTime());

		try {
			String encoding = FeedUtils.guessEncoding(xml);
			String xmlString = FeedUtils.trimInvalidXmlCharacters(new String(
					xml, encoding));

			InputSource source = new InputSource(new StringReader(xmlString));

			SyndFeed rss = new SyndFeedInput().build(source);
			fetchedFeed.setTitle(rss.getTitle());
			feed.setUrl(feedUrl);
			feed.setLink(rss.getLink());
			List<SyndEntry> items = rss.getEntries();
			for (SyndEntry item : items) {
				FeedEntry entry = new FeedEntry();
				entry.setGuid(item.getUri());
				entry.setGuidHash(DigestUtils.sha1Hex(item.getUri()));
				entry.setUrl(item.getLink());
				entry.setUpdated(validateDate(getUpdateDate(item)));
				entry.setAuthor(item.getAuthor());

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

				entries.add(entry);
			}
			Date publishedDate = validateDate(rss.getPublishedDate());
			if (publishedDate == null && !feed.getEntries().isEmpty()) {
				FeedEntry first = entries.iterator().next();
				publishedDate = first.getUpdated();
			}
			fetchedFeed.setPublishedDate(publishedDate);

		} catch (Exception e) {
			throw new FeedException(String.format(
					"Could not parse feed from %s : %s", feedUrl,
					e.getMessage()), e);
		}
		return fetchedFeed;
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

	private Date validateDate(Date date) {
		if (date == null) {
			return new Date();
		}
		if (date.before(START) || date.after(END)) {
			return new Date();
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
