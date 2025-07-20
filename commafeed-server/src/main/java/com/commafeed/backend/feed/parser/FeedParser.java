package com.commafeed.backend.feed.parser;

import java.io.StringReader;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.inject.Singleton;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.xml.sax.InputSource;

import com.commafeed.backend.Urls;
import com.commafeed.backend.feed.parser.FeedParserResult.Content;
import com.commafeed.backend.feed.parser.FeedParserResult.Enclosure;
import com.commafeed.backend.feed.parser.FeedParserResult.Entry;
import com.commafeed.backend.feed.parser.FeedParserResult.Media;
import com.rometools.modules.mediarss.MediaEntryModule;
import com.rometools.modules.mediarss.MediaModule;
import com.rometools.modules.mediarss.types.MediaGroup;
import com.rometools.modules.mediarss.types.Metadata;
import com.rometools.modules.mediarss.types.Thumbnail;
import com.rometools.rome.feed.synd.SyndCategory;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndLink;
import com.rometools.rome.feed.synd.SyndLinkImpl;
import com.rometools.rome.io.SyndFeedInput;

import lombok.RequiredArgsConstructor;

/**
 * Parses raw xml into a FeedParserResult object
 */
@RequiredArgsConstructor
@Singleton
public class FeedParser {

	private static final Namespace ATOM_10_NS = Namespace.getNamespace("http://www.w3.org/2005/Atom");

	private static final Instant START = Instant.ofEpochMilli(86400000);
	private static final Instant END = Instant.ofEpochMilli(1000L * Integer.MAX_VALUE - 86400000);

	private final EncodingDetector encodingDetector;
	private final FeedCleaner feedCleaner;

	public FeedParserResult parse(String feedUrl, byte[] xml) throws FeedParsingException {
		try {
			Charset encoding = encodingDetector.getEncoding(xml);
			String xmlString = feedCleaner.trimInvalidXmlCharacters(new String(xml, encoding));
			if (xmlString == null) {
				throw new FeedParsingException("Input string is null for url " + feedUrl);
			}
			xmlString = feedCleaner.replaceHtmlEntitiesWithNumericEntities(xmlString);
			xmlString = feedCleaner.removeDoctypeDeclarations(xmlString);

			InputSource source = new InputSource(new StringReader(xmlString));
			SyndFeed feed = new SyndFeedInput().build(source);
			handleForeignMarkup(feed);

			String title = feed.getTitle();
			String link = feed.getLink();
			List<Entry> entries = buildEntries(feed, feedUrl);
			Instant lastEntryDate = entries.stream().findFirst().map(Entry::published).orElse(null);
			Instant lastPublishedDate = toValidInstant(feed.getPublishedDate(), false);
			if (lastPublishedDate == null || lastEntryDate != null && lastPublishedDate.isBefore(lastEntryDate)) {
				lastPublishedDate = lastEntryDate;
			}
			Long averageEntryInterval = averageTimeBetweenEntries(entries);

			return new FeedParserResult(title, link, lastPublishedDate, averageEntryInterval, lastEntryDate, entries);
		} catch (FeedParsingException e) {
			throw e;
		} catch (Exception e) {
			throw new FeedParsingException(String.format("Could not parse feed from %s : %s", feedUrl, e.getMessage()), e);
		}
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

	private List<Entry> buildEntries(SyndFeed feed, String feedUrl) {
		List<Entry> entries = new ArrayList<>();

		for (SyndEntry item : feed.getEntries()) {
			String guid = item.getUri();
			if (StringUtils.isBlank(guid)) {
				guid = item.getLink();
			}
			if (StringUtils.isBlank(guid)) {
				// no guid and no link, skip entry
				continue;
			}

			String url = buildEntryUrl(feed, feedUrl, item);
			if (StringUtils.isBlank(url) && Urls.isAbsolute(guid)) {
				// if link is empty but guid is used as url, use guid
				url = guid;
			}

			Instant publishedDate = buildEntryPublishedDate(item);
			Content content = buildContent(item);

			entries.add(new Entry(guid, url, publishedDate, content));
		}

		entries.sort(Comparator.comparing(Entry::published).reversed());
		return entries;
	}

	private Content buildContent(SyndEntry item) {
		String title = getTitle(item);
		String content = getContent(item);
		String author = StringUtils.trimToNull(item.getAuthor());
		String categories = StringUtils
				.trimToNull(item.getCategories().stream().map(SyndCategory::getName).collect(Collectors.joining(", ")));

		Enclosure enclosure = buildEnclosure(item);
		Media media = buildMedia(item);
		return new Content(title, content, author, categories, enclosure, media);
	}

	private Enclosure buildEnclosure(SyndEntry item) {
		SyndEnclosure enclosure = item.getEnclosures().stream().findFirst().orElse(null);
		if (enclosure == null) {
			return null;
		}

		return new Enclosure(enclosure.getUrl(), enclosure.getType());
	}

	private Instant buildEntryPublishedDate(SyndEntry item) {
		Date date = item.getPublishedDate();
		if (date == null) {
			date = item.getUpdatedDate();
		}
		return toValidInstant(date, true);
	}

	private String buildEntryUrl(SyndFeed feed, String feedUrl, SyndEntry item) {
		String url = StringUtils.trimToNull(StringUtils.normalizeSpace(item.getLink()));
		if (url == null || Urls.isAbsolute(url)) {
			// url is absolute, nothing to do
			return url;
		}

		// url is relative, trying to resolve it
		String feedLink = StringUtils.trimToNull(StringUtils.normalizeSpace(feed.getLink()));
		return Urls.toAbsolute(url, feedLink, feedUrl);
	}

	private Instant toValidInstant(Date date, boolean nullToNow) {
		Instant now = Instant.now();
		if (date == null) {
			return nullToNow ? now : null;
		}

		Instant instant = date.toInstant();
		if (instant.isBefore(START) || instant.isAfter(END)) {
			return now;
		}

		if (instant.isAfter(now)) {
			return now;
		}
		return instant;
	}

	private String getContent(SyndEntry item) {
		String content;
		if (item.getContents().isEmpty()) {
			content = item.getDescription() == null ? null : item.getDescription().getValue();
		} else {
			content = item.getContents().stream().map(SyndContent::getValue).collect(Collectors.joining(System.lineSeparator()));
		}
		return StringUtils.trimToNull(content);
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
		return StringUtils.trimToNull(title);
	}

	private Media buildMedia(SyndEntry item) {
		MediaEntryModule module = (MediaEntryModule) item.getModule(MediaModule.URI);
		if (module == null) {
			return null;
		}

		Media media = buildMedia(module.getMetadata());
		if (media == null && ArrayUtils.isNotEmpty(module.getMediaGroups())) {
			MediaGroup group = module.getMediaGroups()[0];
			media = buildMedia(group.getMetadata());
		}

		return media;
	}

	private Media buildMedia(Metadata metadata) {
		if (metadata == null) {
			return null;
		}

		String description = metadata.getDescription();

		String thumbnailUrl = null;
		Integer thumbnailWidth = null;
		Integer thumbnailHeight = null;
		if (ArrayUtils.isNotEmpty(metadata.getThumbnail())) {
			Thumbnail thumbnail = metadata.getThumbnail()[0];
			thumbnailWidth = thumbnail.getWidth();
			thumbnailHeight = thumbnail.getHeight();
			if (thumbnail.getUrl() != null) {
				thumbnailUrl = thumbnail.getUrl().toString();
			}
		}

		if (description == null && thumbnailUrl == null) {
			return null;
		}

		return new Media(description, thumbnailUrl, thumbnailWidth, thumbnailHeight);
	}

	private Long averageTimeBetweenEntries(List<Entry> entries) {
		if (entries.isEmpty() || entries.size() == 1) {
			return null;
		}

		SummaryStatistics stats = new SummaryStatistics();
		for (int i = 0; i < entries.size() - 1; i++) {
			long diff = Math.abs(entries.get(i).published().toEpochMilli() - entries.get(i + 1).published().toEpochMilli());
			stats.addValue(diff);
		}
		return (long) stats.getMean();
	}

	public static class FeedParsingException extends Exception {
		private static final long serialVersionUID = 1L;

		public FeedParsingException(String message) {
			super(message);
		}

		public FeedParsingException(String message, Throwable cause) {
			super(message, cause);
		}
	}

}
