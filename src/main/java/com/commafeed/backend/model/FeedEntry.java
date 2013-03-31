package com.commafeed.backend.model;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.codec.binary.StringUtils;
import org.hibernate.annotations.Index;

@Entity
@Table(name = "FEEDENTRIES")
@SuppressWarnings("serial")
public class FeedEntry extends AbstractModel {

	@Column(length = 2048, nullable = false, unique = true)
	private String guid;

	@ManyToOne
	@JoinColumn(nullable = false)
	private Feed feed;

	@Column(length = 2048)
	private String title;

	@Lob
	private byte[] content;

	@Column(length = 2048)
	private String url;

	@Temporal(TemporalType.TIMESTAMP)
	@Index(name = "updated_index")
	private Date updated;

	@OneToMany(mappedBy = "entry")
	private Set<FeedEntryStatus> statuses;

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return StringUtils.newStringUtf8(content);
	}

	public void setContent(String content) {
		this.content = StringUtils.getBytesUtf8(content);
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Date getUpdated() {
		return updated;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	public Feed getFeed() {
		return feed;
	}

	public void setFeed(Feed feed) {
		this.feed = feed;
	}

	public Set<FeedEntryStatus> getStatuses() {
		return statuses;
	}

	public void setStatuses(Set<FeedEntryStatus> statuses) {
		this.statuses = statuses;
	}

}
