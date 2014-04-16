package com.commafeed.frontend.model;

import java.io.Serializable;

import lombok.Data;

import com.wordnik.swagger.annotations.ApiClass;
import com.wordnik.swagger.annotations.ApiProperty;

@SuppressWarnings("serial")
@ApiClass("User settings")
@Data
public class Settings implements Serializable {

	@ApiProperty(value = "user's preferred language, english if none")
	private String language;

	@ApiProperty(value = "user reads all entries or unread entries only", allowableValues = "all,unread", required = true)
	private String readingMode;

	@ApiProperty(value = "user reads entries in ascending or descending order", allowableValues = "asc,desc", required = true)
	private String readingOrder;

	@ApiProperty(value = "user viewing mode, either title-only or expande view", allowableValues = "title,expanded", required = true)
	private String viewMode;

	@ApiProperty(value = "user wants category and feeds with no unread entries shown", required = true)
	private boolean showRead;

	@ApiProperty(value = "In expanded view, scroll through entries mark them as read", required = true)
	private boolean scrollMarks;

	@ApiProperty(value = "user's selected theme")
	private String theme;

	@ApiProperty(value = "user's custom css for the website")
	private String customCss;
	
	@ApiProperty(value = "user's preferred scroll speed when navigating between entries")
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

}
