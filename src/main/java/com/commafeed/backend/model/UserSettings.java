package com.commafeed.backend.model;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "USERSETTINGS")
@SuppressWarnings("serial")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Getter
@Setter
public class UserSettings extends AbstractModel {

	@XmlRootElement
	public enum ReadingMode {
		all, unread
	}

	@XmlRootElement
	public enum ReadingOrder {
		asc, desc
	}

	@XmlRootElement
	public enum ViewMode {
		title, expanded
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ReadingMode readingMode;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ReadingOrder readingOrder;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ViewMode viewMode;

	@Column(name = "user_lang", length = 4)
	private String language;

	private boolean showRead;
	private boolean scrollMarks;
	private boolean socialButtons;

	@Column(length = 32)
	private String theme;

	@Lob
	@Column(length = Integer.MAX_VALUE)
	private String customCss;

}
