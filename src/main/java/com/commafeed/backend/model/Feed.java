package com.commafeed.backend.model;

import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Index;

import com.google.common.collect.Sets;

@Entity
@Table(name = "FEEDS")
@org.hibernate.annotations.Table(appliesTo = "FEEDS", indexes = { @Index(name = "disabled_lastupdated_index", columnNames = {
		"disabledUntil", "lastUpdated" }), })
@SuppressWarnings("serial")
public class Feed extends AbstractModel {

	/**
	 * The url of the feed
	 */
	@Column(length = 2048, nullable = false)
	private String url;

	@Column(length = 40, nullable = false)
	@Index(name = "urlHash_index")
	private String urlHash;

	/**
	 * The url of the website, extracted from the feed
	 */
	@Column(length = 2048)
	private String link;

	/**
	 * Last time we tried to fetch the feed
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Index(name = "lastupdated_index")
	private Date lastUpdated;

	/**
	 * Last time we successfully refreshed the feed
	 */
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastUpdateSuccess;

	@Column(length = 1024)
	private String message;

	private int errorCount;

	@Temporal(TemporalType.TIMESTAMP)
	@Index(name = "disableduntil_index")
	private Date disabledUntil;

	@Column(length = 64)
	private String lastModifiedHeader;

	@Column(length = 255)
	private String etagHeader;

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "feed", cascade = {
			CascadeType.PERSIST, CascadeType.MERGE })
	private FeedPushInfo pushInfo;

	@ManyToMany(mappedBy = "feeds")
	private Set<FeedEntry> entries = Sets.newHashSet();

	@OneToMany(mappedBy = "feed")
	private Set<FeedSubscription> subscriptions;

	public Feed() {

	}

	public Feed(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public Set<FeedEntry> getEntries() {
		return entries;
	}

	public void setEntries(Set<FeedEntry> entries) {
		this.entries = entries;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Set<FeedSubscription> getSubscriptions() {
		return subscriptions;
	}

	public void setSubscriptions(Set<FeedSubscription> subscriptions) {
		this.subscriptions = subscriptions;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public int getErrorCount() {
		return errorCount;
	}

	public void setErrorCount(int errorCount) {
		this.errorCount = errorCount;
	}

	public Date getDisabledUntil() {
		return disabledUntil;
	}

	public void setDisabledUntil(Date disabledUntil) {
		this.disabledUntil = disabledUntil;
	}

	public String getUrlHash() {
		return urlHash;
	}

	public void setUrlHash(String urlHash) {
		this.urlHash = urlHash;
	}

	public String getLastModifiedHeader() {
		return lastModifiedHeader;
	}

	public void setLastModifiedHeader(String lastModifiedHeader) {
		this.lastModifiedHeader = lastModifiedHeader;
	}

	public String getEtagHeader() {
		return etagHeader;
	}

	public void setEtagHeader(String etagHeader) {
		this.etagHeader = etagHeader;
	}

	public Date getLastUpdateSuccess() {
		return lastUpdateSuccess;
	}

	public void setLastUpdateSuccess(Date lastUpdateSuccess) {
		this.lastUpdateSuccess = lastUpdateSuccess;
	}

	public FeedPushInfo getPushInfo() {
		return pushInfo;
	}

	public void setPushInfo(FeedPushInfo pushInfo) {
		this.pushInfo = pushInfo;
	}

}
