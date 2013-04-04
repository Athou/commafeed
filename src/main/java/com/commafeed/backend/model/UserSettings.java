package com.commafeed.backend.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "USERSETTINGS")
@SuppressWarnings("serial")
public class UserSettings extends AbstractModel {

	public enum ReadingMode {
		all, unread
	}

	@OneToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ReadingMode readingMode;

	@Lob
	@Column(length = Integer.MAX_VALUE)
	private String customCss;

	public ReadingMode getReadingMode() {
		return readingMode;
	}

	public void setReadingMode(ReadingMode readingMode) {
		this.readingMode = readingMode;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getCustomCss() {
		return customCss;
	}

	public void setCustomCss(String customCss) {
		this.customCss = customCss;
	}

}
