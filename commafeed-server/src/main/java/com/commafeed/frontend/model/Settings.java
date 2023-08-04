package com.commafeed.frontend.model;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@SuppressWarnings("serial")
@ApiModel(description = "User settings")
@Data
public class Settings implements Serializable {

	@ApiModelProperty(value = "user's preferred language, english if none", required = true)
	private String language;

	@ApiModelProperty(value = "user reads all entries or unread entries only", allowableValues = "all,unread", required = true)
	private String readingMode;

	@ApiModelProperty(value = "user reads entries in ascending or descending order", allowableValues = "asc,desc", required = true)
	private String readingOrder;

	@ApiModelProperty(value = "user wants category and feeds with no unread entries shown", required = true)
	private boolean showRead;

	@ApiModelProperty(value = "In expanded view, scroll through entries mark them as read", required = true)
	private boolean scrollMarks;

	@ApiModelProperty(value = "user's custom css for the website")
	private String customCss;

	@ApiModelProperty(value = "user's custom js for the website")
	private String customJs;

	@ApiModelProperty(value = "user's preferred scroll speed when navigating between entries", required = true)
	private int scrollSpeed;

	@ApiModelProperty(value = "always scroll selected entry to the top of the page, even if it fits entirely on screen", required = true)
	private boolean alwaysScrollToEntry;

	@ApiModelProperty(value = "ask for confirmation when marking all entries as read", required = true)
	private boolean markAllAsReadConfirmation;

	@ApiModelProperty(value = "show commafeed's own context menu on right click", required = true)
	private boolean customContextMenu;

	@ApiModelProperty(value = "sharing settings", required = true)
	private SharingSettings sharingSettings = new SharingSettings();

	@ApiModel(description = "User sharing settings")
	@Data
	public static class SharingSettings implements Serializable {
		@ApiModelProperty(required = true)
		private boolean email;

		@ApiModelProperty(required = true)
		private boolean gmail;

		@ApiModelProperty(required = true)
		private boolean facebook;

		@ApiModelProperty(required = true)
		private boolean twitter;

		@ApiModelProperty(required = true)
		private boolean tumblr;

		@ApiModelProperty(required = true)
		private boolean pocket;

		@ApiModelProperty(required = true)
		private boolean instapaper;

		@ApiModelProperty(required = true)
		private boolean buffer;
	}
}
