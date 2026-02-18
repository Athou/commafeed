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

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "USERSETTINGS")
@SuppressWarnings("serial")
@Getter
@Setter
public class UserSettings extends AbstractModel {

	public enum ReadingMode {
		@JsonProperty("all")
		ALL,

		@JsonProperty("unread")
		UNREAD;

		// method called for query parameters
		public static ReadingMode fromString(final String s) {
			return ReadingMode.valueOf(s.toUpperCase());
		}
	}

	public enum ReadingOrder {
		@JsonProperty("asc")
		ASC,

		@JsonProperty("desc")
		DESC;

		// method called for query parameters
		public static ReadingOrder fromString(final String s) {
			return ReadingOrder.valueOf(s.toUpperCase());
		}
	}

	public enum ScrollMode {
		@JsonProperty("always")
		ALWAYS,

		@JsonProperty("never")
		NEVER,

		@JsonProperty("if_needed")
		IF_NEEDED
	}

	public enum IconDisplayMode {
		@JsonProperty("always")
		ALWAYS,

		@JsonProperty("never")
		NEVER,

		@JsonProperty("on_desktop")
		ON_DESKTOP,

		@JsonProperty("on_mobile")
		ON_MOBILE
	}

	public enum PushNotificationType {
		@JsonProperty("ntfy")
		NTFY,

		@JsonProperty("gotify")
		GOTIFY,

		@JsonProperty("pushover")
		PUSHOVER
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
	private boolean disablePullToRefresh;

	@Enumerated(EnumType.STRING)
	@Column(name = "push_notification_type", length = 16)
	private PushNotificationType pushNotificationType;

	@Column(name = "push_notification_server_url", length = 1024)
	private String pushNotificationServerUrl;

	@Column(name = "push_notification_user_id", length = 512)
	private String pushNotificationUserId;

	@Column(name = "push_notification_user_secret", length = 512)
	private String pushNotificationUserSecret;

	@Column(name = "push_notification_topic", length = 256)
	private String pushNotificationTopic;

	private boolean email;
	private boolean gmail;
	private boolean facebook;
	private boolean twitter;
	private boolean tumblr;
	private boolean pocket;
	private boolean instapaper;
	private boolean buffer;

}
