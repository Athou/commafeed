package com.commafeed.backend.model;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "FEEDENTRYCONTENTS")
@SuppressWarnings("serial")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class FeedEntryContent extends AbstractModel {

	@Column(length = 2048)
	private String title;

	@Lob
	@Column(length = Integer.MAX_VALUE)
	private String content;

	@Column(length = 2048)
	private String enclosureUrl;

	@Column(length = 255)
	private String enclosureType;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getEnclosureUrl() {
		return enclosureUrl;
	}

	public void setEnclosureUrl(String enclosureUrl) {
		this.enclosureUrl = enclosureUrl;
	}

	public String getEnclosureType() {
		return enclosureType;
	}

	public void setEnclosureType(String enclosureType) {
		this.enclosureType = enclosureType;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
