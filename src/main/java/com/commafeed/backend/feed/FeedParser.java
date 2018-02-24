package com.commafeed.backend.feed;

import java.io.StringReader;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.xml.sax.InputSource;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryContent;
import com.google.common.collect.Iterables;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndLink;
import com.rometools.rome.feed.synd.SyndLinkImpl;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
@Singleton
public class FeedParser {

	private static final String ATOM_10_URI = "http://www.w3.org/2005/Atom";
	private static final Namespace ATOM_10_NS = Namespace.getNamespace(ATOM_10_URI);

	private static final Date START = new Date(86400000);
	private static final Date END = new Date(1000l * Integer.MAX_VALUE - 86400000);

	public FetchedFeed parse(String feedUrl, byte[] xml) throws FeedException {
		FetchedFeed fetchedFeed = new FetchedFeed();
		Feed feed = fetchedFeed.getFeed();
		List<FeedEntry> entries = fetchedFeed.getEntries();

		try {
			Charset encoding = FeedUtils.guessEncoding(xml);
			String xmlString = FeedUtils.trimInvalidXmlCharacters(new String(xml, encoding));
			if (xmlString == null) {
				throw new FeedException("Input string is null for url " + feedUrl);
			}
			xmlString = FeedUtils.replaceHtmlEntitiesWithNumericEntities(xmlString);
			InputSource source = new InputSource(new StringReader(xmlString));
			SyndFeed rss = new SyndFeedInput().build(source);
			handleForeignMarkup(rss);

			fetchedFeed.setTitle(rss.getTitle());
			feed.setPushHub(findHub(rss));
			feed.setPushTopic(findSelf(rss));
			feed.setUrl(feedUrl);
			feed.setLink(rss.getLink());
			List<SyndEntry> items = rss.getEntries();

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
				entry.setUpdated(validateDate(getEntryUpdateDate(item), true));
				entry.setUrl(FeedUtils.truncate(FeedUtils.toAbsoluteUrl(item.getLink(), feed.getLink(), feedUrl), 2048));

				// if link is empty but guid is used as url
				if (StringUtils.isBlank(entry.getUrl()) && StringUtils.startsWith(entry.getGuid(), "http")) {
					entry.setUrl(entry.getGuid());
				}

				FeedEntryContent content = new FeedEntryContent();
				content.setContent(getContent(item));
				content.setCategories(FeedUtils.truncate(
						item.getCategories().stream().map(c -> c.getName()).collect(Collectors.joining(", ")), 4096));
				content.setTitle(getTitle(item));
				content.setAuthor(StringUtils.trimToNull(item.getAuthor()));
				SyndEnclosure enclosure = Iterables.getFirst(item.getEnclosures(), null);
				if (enclosure != null) {
					content.setEnclosureUrl(FeedUtils.truncate(enclosure.getUrl(), 2048));
					content.setEnclosureType(enclosure.getType());
				}
				entry.setContent(content);

				entries.add(entry);
			}
			Date lastEntryDate = null;
			Date publishedDate = validateDate(rss.getPublishedDate(), false);
			if (!entries.isEmpty()) {
				List<Long> sortedTimestamps = FeedUtils.getSortedTimestamps(entries);
				Long timestamp = sortedTimestamps.get(0);
				lastEntryDate = new Date(timestamp);
				publishedDate = (publishedDate == null || publishedDate.before(lastEntryDate)) ? lastEntryDate : publishedDate;
			}
			feed.setLastPublishedDate(publishedDate);
			feed.setAverageEntryInterval(FeedUtils.averageTimeBetweenEntries(entries));
			feed.setLastEntryDate(lastEntryDate);

		} catch (Exception e) {
			throw new FeedException(String.format("Could not parse feed from %s : %s", feedUrl, e.getMessage()), e);
		}
		return fetchedFeed;
	}

	/**
	 * Adds atom links for rss feeds
	 */
	private void handleForeignMarkup(SyndFeed feed) {
		List<Element> foreignMarkup = feed.getForeignMarkup();
		if (foreignMarkup == null) {
			return;
		}
		for (Element element : foreignMarkup) {
			if ("link".equals(element.getName()) && ATOM_10_NS.equals(element.getNamespace())) {
				SyndLink link = new SyndLinkImpl();
				link.setRel(element.getAttributeValue("rel"));
				link.setHref(element.getAttributeValue("href"));
				feed.getLinks().add(link);
			}
		}
	}
	// This function have been changed to protected for testing purposes
	protected Date getEntryUpdateDate(SyndEntry item) {
		Date date = item.getUpdatedDate();
		if (date == null) {
			date = item.getPublishedDate();
		}
		if (date == null) {
			date = new Date();
		}
		return date;
	}
	// This function have been changed to protected for testing purposes
	protected Date validateDate(Date date, boolean nullToNow) {
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
	// This function have been changed to protected for testing purposes
	protected String getContent(SyndEntry item) {
		String content = null;
		if (item.getContents().isEmpty()) {
			content = item.getDescription() == null ? null : item.getDescription().getValue();
		} else {
			content = item.getContents().stream().map(c -> c.getValue()).collect(Collectors.joining(System.lineSeparator()));
		}
		return StringUtils.trimToNull(content);
	}
	// This function have been changed to protected for testing purposes
	protected String getTitle(SyndEntry item) {
		String title = item.getTitle();
		if (StringUtils.isBlank(title)) {
			Date date = item.getPublishedDate();
			if (date != null) {
				title = DateFormat.getInstance().format(date);
			} else {
				title = "(no title)";
			}
		}
		return StringUtils.trimToNull(title);
	}

	private String findHub(SyndFeed feed) {
		for (SyndLink l : feed.getLinks()) {
			if ("hub".equalsIgnoreCase(l.getRel())) {
				log.debug("found hub {} for feed {}", l.getHref(), feed.getLink());
				return l.getHref();
			}
		}
		return null;
	}
	private String findSelf(SyndFeed feed) {
		for (SyndLink l : feed.getLinks()) {
			if ("self".equalsIgnoreCase(l.getRel())) {
				log.debug("found self {} for feed {}", l.getHref(), feed.getLink());
				return l.getHref();
			}
		}
		return null;
	}

}
