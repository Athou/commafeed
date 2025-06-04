package com.commafeed.frontend.model;

import java.io.Serializable;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Data;

@SuppressWarnings("serial")
@Schema(description = "User settings")
@Data
@RegisterForReflection
public class Settings implements Serializable {

	@Schema(description = "user's preferred language, english if none", requiredMode = RequiredMode.REQUIRED)
	private String language;

	@Schema(
			description = "user reads all entries or unread entries only",
			allowableValues = "all,unread",
			requiredMode = RequiredMode.REQUIRED)
	private String readingMode;

	@Schema(
			description = "user reads entries in ascending or descending order",
			allowableValues = "asc,desc",
			requiredMode = RequiredMode.REQUIRED)
	private String readingOrder;

	@Schema(description = "user wants category and feeds with no unread entries shown", requiredMode = RequiredMode.REQUIRED)
	private boolean showRead;

	@Schema(description = "In expanded view, scroll through entries mark them as read", requiredMode = RequiredMode.REQUIRED)
	private boolean scrollMarks;

	@Schema(description = "user's custom css for the website")
	private String customCss;

	@Schema(description = "user's custom js for the website")
	private String customJs;

	@Schema(description = "user's preferred scroll speed when navigating between entries", requiredMode = RequiredMode.REQUIRED)
	private int scrollSpeed;

	@Schema(
			description = "whether to scroll to the selected entry",
			allowableValues = "always,never,if_needed",
			requiredMode = RequiredMode.REQUIRED)
	private String scrollMode;

	@Schema(description = "number of entries to keep above the selected entry when scrolling", requiredMode = RequiredMode.REQUIRED)
	private int entriesToKeepOnTopWhenScrolling;

	@Schema(
			description = "whether to show the star icon in the header of entries",
			allowableValues = "always,never,on_desktop,on_mobile",
			requiredMode = RequiredMode.REQUIRED)
	private String starIconDisplayMode;

	@Schema(
			description = "whether to show the external link icon in the header of entries",
			allowableValues = "always,never,on_desktop,on_mobile",
			requiredMode = RequiredMode.REQUIRED)
	private String externalLinkIconDisplayMode;

	@Schema(description = "ask for confirmation when marking all entries as read", requiredMode = RequiredMode.REQUIRED)
	private boolean markAllAsReadConfirmation;

	@Schema(
			description = "navigate to the next unread category or feed after marking all entries as read",
			requiredMode = RequiredMode.REQUIRED)
	private boolean markAllAsReadNavigateToNextUnread;

	@Schema(description = "show commafeed's own context menu on right click", requiredMode = RequiredMode.REQUIRED)
	private boolean customContextMenu;

	@Schema(description = "on mobile, show action buttons at the bottom of the screen", requiredMode = RequiredMode.REQUIRED)
	private boolean mobileFooter;

	@Schema(description = "show unread count in the title", requiredMode = RequiredMode.REQUIRED)
	private boolean unreadCountTitle;

	@Schema(description = "show unread count in the favicon", requiredMode = RequiredMode.REQUIRED)
	private boolean unreadCountFavicon;

	@Schema(description = "primary theme color to use in the UI")
	private String primaryColor;

	@Schema(description = "sharing settings", requiredMode = RequiredMode.REQUIRED)
	private SharingSettings sharingSettings = new SharingSettings();

	@Schema(description = "User sharing settings")
	@Data
	public static class SharingSettings implements Serializable {
		@Schema(requiredMode = RequiredMode.REQUIRED)
		private boolean email;

		@Schema(requiredMode = RequiredMode.REQUIRED)
		private boolean gmail;

		@Schema(requiredMode = RequiredMode.REQUIRED)
		private boolean facebook;

		@Schema(requiredMode = RequiredMode.REQUIRED)
		private boolean twitter;

		@Schema(requiredMode = RequiredMode.REQUIRED)
		private boolean tumblr;

		@Schema(requiredMode = RequiredMode.REQUIRED)
		private boolean pocket;

		@Schema(requiredMode = RequiredMode.REQUIRED)
		private boolean instapaper;

		@Schema(requiredMode = RequiredMode.REQUIRED)
		private boolean buffer;
	}
}
