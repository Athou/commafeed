package com.commafeed.backend.model;

import java.sql.Types;
import java.time.Instant;

import org.hibernate.annotations.JdbcTypeCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "FEEDS")
@SuppressWarnings("serial")
@Getter
@Setter
public class Feed extends AbstractModel {

	/**
	 * The url of the feed
	 */
	@Lob
	@Column(length = Integer.MAX_VALUE, nullable = false)
	@JdbcTypeCode(Types.LONGVARCHAR)
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
	@Lob
	@Column(length = Integer.MAX_VALUE)
	@JdbcTypeCode(Types.LONGVARCHAR)
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
	@Lob
	@Column(length = Integer.MAX_VALUE)
	@JdbcTypeCode(Types.LONGVARCHAR)
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
