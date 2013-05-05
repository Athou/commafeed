package com.commafeed.frontend.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.wordnik.swagger.annotations.ApiClass;
import com.wordnik.swagger.annotations.ApiProperty;

@SuppressWarnings("serial")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@ApiClass("User settings")
public class Settings implements Serializable {

	@ApiProperty(value = "user reads all entries or unread entries only", allowableValues = "all,unread", required = true)
	private String readingMode;

	@ApiProperty(value = "user reads entries in ascending or descending order", allowableValues = "asc,desc", required = true)
	private String readingOrder;

	@ApiProperty(value = "user viewing mode, either title-only or expande view", allowableValues = "title,expanded", required = true)
	private String viewMode;

	@ApiProperty(value = "user wants category and feeds with no unread entries shown", required = true)
	private boolean showRead;

	@ApiProperty(value = "user wants social buttons (facebook, twitter, ...) shown", required = true)
	private boolean socialButtons;

	@ApiProperty(value = "In expanded view, scroll through entries mark them as read", required = true)
	private boolean scrollMarks;

	@ApiProperty(value = "user's custom css for the website")
	private String customCss;

	public String getReadingMode() {
		return readingMode;
	}

	public void setReadingMode(String readingMode) {
		this.readingMode = readingMode;
	}

	public String getCustomCss() {
		return customCss;
	}

	public void setCustomCss(String customCss) {
		this.customCss = customCss;
	}

	public String getReadingOrder() {
		return readingOrder;
	}

	public void setReadingOrder(String readingOrder) {
		this.readingOrder = readingOrder;
	}

	public boolean isShowRead() {
		return showRead;
	}

	public void setShowRead(boolean showRead) {
		this.showRead = showRead;
	}

	public boolean isSocialButtons() {
		return socialButtons;
	}

	public void setSocialButtons(boolean socialButtons) {
		this.socialButtons = socialButtons;
	}

	public String getViewMode() {
		return viewMode;
	}

	public void setViewMode(String viewMode) {
		this.viewMode = viewMode;
	}

	public boolean isScrollMarks() {
		return scrollMarks;
	}

	public void setScrollMarks(boolean scrollMarks) {
		this.scrollMarks = scrollMarks;
	}

}
