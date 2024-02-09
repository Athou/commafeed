package com.commafeed.backend.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "FEEDS")
@SuppressWarnings("serial")
@Getter
@Setter
public class Feed extends AbstractModel {

	// mariadb timestamp range starts at 1970-01-01 00:00:01
	public static final Instant MINIMUM_DISABLED_UNTIL = Instant.EPOCH.plusSeconds(1);

	/**
	 * The url of the feed
	 */
	@Column(length = 2048, nullable = false)
	private String url;

	/**
	 * cache the url after potential http 30x redirects
	 */
	@Column(name = "url_after_redirect", length = 2048, nullable = false)
	private String urlAfterRedirect;

	@Column(length = 2048, nullable = false)
	private String normalizedUrl;

	@Column(length = 40, nullable = false)
	private String normalizedUrlHash;

	/**
	 * The url of the website, extracted from the feed
	 */
	@Column(length = 2048)
	private String link;

	/**
	 * Last time we tried to fetch the feed
	 */
	@Column
	private Instant lastUpdated;

	/**
	 * Last publishedDate value in the feed
	 */
	@Column
	private Instant lastPublishedDate;

	/**
	 * date of the last entry of the feed
	 */
	@Column
	private Instant lastEntryDate;

	/**
	 * error message while retrieving the feed
	 */
	@Column(length = 1024)
	private String message;

	/**
	 * times we failed to retrieve the feed
	 */
	private int errorCount;

	/**
	 * feed refresh is disabled until this date
	 */
	@Column
	private Instant disabledUntil;

	/**
	 * http header returned by the feed
	 */
	@Column(length = 64)
	private String lastModifiedHeader;

	/**
	 * http header returned by the feed
	 */
	@Column(length = 255)
	private String etagHeader;

	/**
	 * average time between entries in the feed
	 */
	private Long averageEntryInterval;

	/**
	 * last hash of the content of the feed xml
	 */
	@Column(length = 40)
	private String lastContentHash;

}
