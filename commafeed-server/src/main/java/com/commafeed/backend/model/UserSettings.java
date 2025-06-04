package com.commafeed.backend.model;

import java.sql.Types;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;

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

	public enum ScrollMode {
		always, never, if_needed
	}

	public enum IconDisplayMode {
		always, never, on_desktop, on_mobile
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
	@JdbcTypeCode(Types.LONGVARCHAR)
	private String customCss;

	@Lob
	@Column(length = Integer.MAX_VALUE)
	@JdbcTypeCode(Types.LONGVARCHAR)
	private String customJs;

	@Column(name = "scroll_speed")
	private int scrollSpeed;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ScrollMode scrollMode;

	private int entriesToKeepOnTopWhenScrolling;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private IconDisplayMode starIconDisplayMode;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private IconDisplayMode externalLinkIconDisplayMode;

	@Column(name = "primary_color", length = 32)
	private String primaryColor;

	private boolean markAllAsReadConfirmation;
	private boolean markAllAsReadNavigateToNextUnread;
	private boolean customContextMenu;
	private boolean mobileFooter;
	private boolean unreadCountTitle;
	private boolean unreadCountFavicon;

	private boolean email;
	private boolean gmail;
	private boolean facebook;
	private boolean twitter;
	private boolean tumblr;
	private boolean pocket;
	private boolean instapaper;
	private boolean buffer;

}
