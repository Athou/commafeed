package com.commafeed.frontend.model;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@SuppressWarnings("serial")
@ApiModel("User settings")
@Data
public class Settings implements Serializable {

	@ApiModelProperty(value = "user's preferred language, english if none")
	private String language;

	@ApiModelProperty(value = "user reads all entries or unread entries only", allowableValues = "all,unread", required = true)
	private String readingMode;

	@ApiModelProperty(value = "user reads entries in ascending or descending order", allowableValues = "asc,desc", required = true)
	private String readingOrder;

	@ApiModelProperty(value = "user viewing mode, either title-only or expande view", allowableValues = "title,expanded", required = true)
	private String viewMode;

	@ApiModelProperty(value = "user wants category and feeds with no unread entries shown", required = true)
	private boolean showRead;

	@ApiModelProperty(value = "In expanded view, scroll through entries mark them as read", required = true)
	private boolean scrollMarks;

	@ApiModelProperty(value = "user's selected theme")
	private String theme;

	@ApiModelProperty(value = "user's custom css for the website")
	private String customCss;

	@ApiModelProperty(value = "user's preferred scroll speed when navigating between entries")
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
