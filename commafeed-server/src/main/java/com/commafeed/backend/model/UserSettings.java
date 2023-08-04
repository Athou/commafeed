package com.commafeed.backend.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "USERSETTINGS")
@SuppressWarnings("serial")
@Getter
@Setter
public class UserSettings extends AbstractModel {

	public enum ReadingMode {
		all, unread
	}

	public enum ReadingOrder {
		asc, desc
	}

	public enum ViewMode {
		title, cozy, detailed, expanded
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

	@Column(name = "user_lang", length = 4)
	private String language;

	private boolean showRead;
	private boolean scrollMarks;

	@Lob
	@Column(length = Integer.MAX_VALUE)
	@Type(type = "org.hibernate.type.TextType")
	private String customCss;

	@Lob
	@Column(length = Integer.MAX_VALUE)
	@Type(type = "org.hibernate.type.TextType")
	private String customJs;

	@Column(name = "scroll_speed")
	private int scrollSpeed;

	private boolean alwaysScrollToEntry;
	private boolean markAllAsReadConfirmation;
	private boolean customContextMenu;

	private boolean email;
	private boolean gmail;
	private boolean facebook;
	private boolean twitter;
	private boolean tumblr;
	private boolean pocket;
	private boolean instapaper;
	private boolean buffer;

}
