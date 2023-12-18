package com.commafeed.backend.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
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
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastUpdated;

	/**
	 * Last publishedDate value in the feed
	 */
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastPublishedDate;

	/**
	 * date of the last entry of the feed
	 */
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastEntryDate;

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
	@Temporal(TemporalType.TIMESTAMP)
	private Date disabledUntil;

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
