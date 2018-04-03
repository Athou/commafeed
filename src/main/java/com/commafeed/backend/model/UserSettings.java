package com.commafeed.backend.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.*;

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
		asc, desc, abc, zyx
	}

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

	@Column(length = 32)
	private String theme;

	@Lob
	@Column(length = Integer.MAX_VALUE)
	@Type(type = "org.hibernate.type.StringClobType")
	private String customCss;

	@Column(name = "scroll_speed")
	private int scrollSpeed;

	private boolean email;
	private boolean gmail;
	private boolean facebook;
	private boolean twitter;
	private boolean googleplus;
	private boolean tumblr;
	private boolean pocket;
	private boolean instapaper;
	private boolean buffer;
	private boolean readability;

	@Override
	public String toString() {
		return "UserSettings{" +
				"user=" + user +
				", readingMode=" + readingMode +
				", readingOrder=" + readingOrder +
				", viewMode=" + viewMode +
				", language='" + language + '\'' +
				", showRead=" + showRead +
				", scrollMarks=" + scrollMarks +
				", theme='" + theme + '\'' +
				", customCss='" + customCss + '\'' +
				", scrollSpeed=" + scrollSpeed +
				", email=" + email +
				", gmail=" + gmail +
				", facebook=" + facebook +
				", twitter=" + twitter +
				", googleplus=" + googleplus +
				", tumblr=" + tumblr +
				", pocket=" + pocket +
				", instapaper=" + instapaper +
				", buffer=" + buffer +
				", readability=" + readability +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof UserSettings)) {
			return false;
		}

		UserSettings that = (UserSettings) o;

		if (isShowRead() != that.isShowRead()) {
			return false;
		}
		if (isScrollMarks() != that.isScrollMarks()) {
			return false;
		}
		if (getScrollSpeed() != that.getScrollSpeed()) {
			return false;
		}
		if (isEmail() != that.isEmail()) {
			return false;
		}
		if (isGmail() != that.isGmail()) {
			return false;
		}
		if (isFacebook() != that.isFacebook()) {
			return false;
		}
		if (isTwitter() != that.isTwitter()) {
			return false;
		}
		if (isGoogleplus() != that.isGoogleplus()) {
			return false;
		}
		if (isTumblr() != that.isTumblr()) {
			return false;
		}
		if (isPocket() != that.isPocket()) {
			return false;
		}
		if (isInstapaper() != that.isInstapaper()) {
			return false;
		}
		if (isBuffer() != that.isBuffer()) {
			return false;
		}
		if (isReadability() != that.isReadability()) {
			return false;
		}
		if (getUser() != null ? !getUser().equals(that.getUser()) :
				that.getUser() != null) {
			return false;
		}
		if (getReadingMode() != that.getReadingMode()) {
			return false;
		}
		if (getReadingOrder() != that.getReadingOrder()) {
			return false;
		}
		if (getViewMode() != that.getViewMode()) {
			return false;
		}
		if (getLanguage() != null ? !getLanguage().equals(that.getLanguage()) :
				that.getLanguage() != null) {
			return false;
		}
		if (getTheme() != null ? !getTheme().equals(that.getTheme()) :
				that.getTheme() != null) {
			return false;
		}
		return getCustomCss() != null ?
				getCustomCss().equals(that.getCustomCss()) :
				that.getCustomCss() == null;

	}
}
