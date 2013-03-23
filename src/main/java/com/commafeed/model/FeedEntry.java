package com.commafeed.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;

@Entity
@Table(name = "FEEDENTRIES")
@SuppressWarnings("serial")
public class FeedEntry implements Serializable {

	@Id
	@Column(length = 2048)
	private String guid;

	@ManyToOne
	private Feed feed;

	@Column(length = 256)
	private String title;

	@Lob
	private String content;

	@Column(length = 2048)
	private String url;

	@Temporal(TemporalType.TIMESTAMP)
	private Date updated;

	public String getContent() {
		return StringUtils.newStringUtf8(Base64.decodeBase64(content));
	}

	public void setContent(String content) {
		this.content = Base64.encodeBase64String(StringUtils
				.getBytesUtf8(content));
	}

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

}
