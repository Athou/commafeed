package com.commafeed.backend.feeds;

import java.io.StringReader;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.jdom.Element;
import org.jdom.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.sun.syndication.feed.synd.SyndLink;
import com.sun.syndication.feed.synd.SyndLinkImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;

public class FeedParser {

	private static Logger log = LoggerFactory.getLogger(FeedParser.class);

	private static final String ATOM_10_URI = "http://www.w3.org/2005/Atom";
	private static final Namespace ATOM_10_NS = Namespace
			.getNamespace(ATOM_10_URI);

	private static final Date START = new Date(86400000);
	private static final Date END = new Date(
			1000l * Integer.MAX_VALUE - 86400000);

	private static final Function<SyndContent, String> CONTENT_TO_STRING = new Function<SyndContent, String>() {
		public String apply(SyndContent content) {
			return content.getValue();
		}
	};

	@SuppressWarnings("unchecked")
	public FetchedFeed parse(String feedUrl, byte[] xml) throws FeedException {
		FetchedFeed fetchedFeed = new FetchedFeed();
		Feed feed = fetchedFeed.getFeed();
		List<FeedEntry> entries = fetchedFeed.getEntries();
		feed.setLastUpdated(new Date());

		try {
			String encoding = FeedUtils.guessEncoding(xml);
			String xmlString = FeedUtils.trimInvalidXmlCharacters(new String(
					xml, encoding));
			if (xmlString == null) {
				throw new FeedException("Input string is null for url "
						+ feedUrl);
			}
			InputSource source = new InputSource(new StringReader(xmlString));
			SyndFeed rss = new SyndFeedInput().build(source);
			handleForeignMarkup(rss);

			fetchedFeed.setTitle(rss.getTitle());
			feed.setPushHub(findHub(rss));
			feed.setPushTopic(findSelf(rss));
			feed.setUrl(feedUrl);
			feed.setLink(rss.getLink());
			List<SyndEntry> items = rss.getEntries();

			if (items.isEmpty()) {
				throw new FeedException("No items in the feed.");
			}

			for (SyndEntry item : items) {
				FeedEntry entry = new FeedEntry();

				String guid = item.getUri();
				if (StringUtils.isBlank(guid)) {
					guid = item.getLink();
				}
				if (StringUtils.isBlank(guid)) {
					// no guid and no link, skip entry
					continue;
				}
				entry.setGuid(FeedUtils.truncate(guid, 2048));
				entry.setUrl(FeedUtils.truncate(
						FeedUtils.toAbsoluteUrl(item.getLink(), feed.getLink()),
						2048));
				entry.setUpdated(validateDate(getEntryUpdateDate(item), true));

				FeedEntryContent content = new FeedEntryContent();
				content.setContent(getContent(item));
				content.setTitle(getTitle(item));
				content.setAuthor(item.getAuthor());
				SyndEnclosure enclosure = (SyndEnclosure) Iterables.getFirst(
						item.getEnclosures(), null);
				if (enclosure != null) {
					content.setEnclosureUrl(FeedUtils.truncate(
							enclosure.getUrl(), 2048));
					content.setEnclosureType(enclosure.getType());
				}
				entry.setContent(content);

				entries.add(entry);
			}
			Date lastEntryDate = null;
			Date publishedDate = validateDate(rss.getPublishedDate(), false);
			if (!entries.isEmpty()) {
				List<Long> sortedTimestamps = FeedUtils
						.getSortedTimestamps(entries);
				Long timestamp = sortedTimestamps.get(0);
				lastEntryDate = new Date(timestamp);
				publishedDate = getFeedPublishedDate(publishedDate, entries);
			}
			feed.setLastPublishedDate(validateDate(publishedDate, true));
			feed.setAverageEntryInterval(FeedUtils
					.averageTimeBetweenEntries(entries));
			feed.setLastEntryDate(lastEntryDate);

		} catch (Exception e) {
			throw new FeedException(String.format(
					"Could not parse feed from %s : %s", feedUrl,
					e.getMessage()), e);
		}
		return fetchedFeed;
	}

	/**
	 * Adds atom links for rss feeds
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void handleForeignMarkup(SyndFeed feed) {
		Object foreignMarkup = feed.getForeignMarkup();
		if (foreignMarkup == null) {
			return;
		}
		if (foreignMarkup instanceof List) {
			List elements = (List) foreignMarkup;
			for (Object object : elements) {
				if (object instanceof Element) {
					Element element = (Element) object;
					if ("link".equals(element.getName())
							&& ATOM_10_NS.equals(element.getNamespace())) {
						SyndLink link = new SyndLinkImpl();
						link.setRel(element.getAttributeValue("rel"));
						link.setHref(element.getAttributeValue("href"));
						feed.getLinks().add(link);
					}
				}
			}
		}
	}

	private Date getFeedPublishedDate(Date publishedDate,
			List<FeedEntry> entries) {

		for (FeedEntry entry : entries) {
			if (publishedDate == null || entry.getUpdated().getTime() > publishedDate.getTime()) {
				publishedDate = entry.getUpdated();
			}
		}
		return publishedDate;
	}

	private Date getEntryUpdateDate(SyndEntry item) {
		Date date = item.getUpdatedDate();
		if (date == null) {
			date = item.getPublishedDate();
		}
		if (date == null) {
			date = new Date();
		}
		return date;
	}

	private Date validateDate(Date date, boolean nullToNow) {
		Date now = new Date();
		if (date == null) {
			return nullToNow ? now : null;
		}
		if (date.before(START) || date.after(END)) {
			return now;
		}

		if (date.after(now)) {
			return now;
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

	private String getTitle(SyndEntry item) {
		String title = item.getTitle();
		if (StringUtils.isBlank(title)) {
			Date date = item.getPublishedDate();
			if (date != null) {
				title = DateFormat.getInstance().format(date);
			} else {
				title = "(no title)";
			}
		}
		return title;
	}

	@SuppressWarnings("unchecked")
	private String findHub(SyndFeed feed) {
		for (SyndLink l : (List<SyndLink>) feed.getLinks()) {
			if ("hub".equalsIgnoreCase(l.getRel())) {
				log.debug("found hub {} for feed {}", l.getHref(),
						feed.getLink());
				return l.getHref();
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private String findSelf(SyndFeed feed) {
		for (SyndLink l : (List<SyndLink>) feed.getLinks()) {
			if ("self".equalsIgnoreCase(l.getRel())) {
				log.debug("found self {} for feed {}", l.getHref(),
						feed.getLink());
				return l.getHref();
			}
		}
		return null;
	}

}
