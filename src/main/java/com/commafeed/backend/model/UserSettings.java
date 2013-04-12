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

	public enum ReadingOrder {
		asc, desc
	}

	@OneToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ReadingMode readingMode;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ReadingOrder readingOrder;

	private boolean showRead;

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

	public ReadingOrder getReadingOrder() {
		return readingOrder;
	}

	public void setReadingOrder(ReadingOrder readingOrder) {
		this.readingOrder = readingOrder;
	}

	public boolean isShowRead() {
		return showRead;
	}

	public void setShowRead(boolean showRead) {
		this.showRead = showRead;
	}

}
