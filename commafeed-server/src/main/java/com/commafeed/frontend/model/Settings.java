package com.commafeed.frontend.model;

import java.io.Serializable;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.commafeed.backend.model.UserSettings.IconDisplayMode;
import com.commafeed.backend.model.UserSettings.ReadingMode;
import com.commafeed.backend.model.UserSettings.ReadingOrder;
import com.commafeed.backend.model.UserSettings.ScrollMode;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@SuppressWarnings("serial")
@Schema(description = "User settings")
@Data
@RegisterForReflection
public class Settings implements Serializable {

	@Schema(description = "user's preferred language, english if none", required = true)
	private String language;

	@Schema(description = "user reads all entries or unread entries only", required = true)
	private ReadingMode readingMode;

	@Schema(description = "user reads entries in ascending or descending order", required = true)
	private ReadingOrder readingOrder;

	@Schema(description = "user wants category and feeds with no unread entries shown", required = true)
	private boolean showRead;

	@Schema(description = "In expanded view, scroll through entries mark them as read", required = true)
	private boolean scrollMarks;

	@Schema(description = "user's custom css for the website")
	private String customCss;

	@Schema(description = "user's custom js for the website")
	private String customJs;

	@Schema(description = "user's preferred scroll speed when navigating between entries", required = true)
	private int scrollSpeed;

	@Schema(description = "whether to scroll to the selected entry", required = true)
	private ScrollMode scrollMode;

	@Schema(description = "number of entries to keep above the selected entry when scrolling", required = true)
	private int entriesToKeepOnTopWhenScrolling;

	@Schema(description = "whether to show the star icon in the header of entries", required = true)
	private IconDisplayMode starIconDisplayMode;

	@Schema(description = "whether to show the external link icon in the header of entries", required = true)
	private IconDisplayMode externalLinkIconDisplayMode;

	@Schema(description = "ask for confirmation when marking all entries as read", required = true)
	private boolean markAllAsReadConfirmation;

	@Schema(description = "navigate to the next unread category or feed after marking all entries as read", required = true)
	private boolean markAllAsReadNavigateToNextUnread;

	@Schema(description = "show commafeed's own context menu on right click", required = true)
	private boolean customContextMenu;

	@Schema(description = "on mobile, show action buttons at the bottom of the screen", required = true)
	private boolean mobileFooter;

	@Schema(description = "show unread count in the title", required = true)
	private boolean unreadCountTitle;

	@Schema(description = "show unread count in the favicon", required = true)
	private boolean unreadCountFavicon;

	@Schema(description = "disable pull to refresh", required = true)
	private boolean disablePullToRefresh;

	@Schema(description = "primary theme color to use in the UI")
	private String primaryColor;

	@Schema(description = "sharing settings", required = true)
	private SharingSettings sharingSettings = new SharingSettings();

	@Schema(description = "User sharing settings")
	@Data
	public static class SharingSettings implements Serializable {
		@Schema(required = true)
		private boolean email;

		@Schema(required = true)
		private boolean gmail;

		@Schema(required = true)
		private boolean facebook;

		@Schema(required = true)
		private boolean twitter;

		@Schema(required = true)
		private boolean tumblr;

		@Schema(required = true)
		private boolean pocket;

		@Schema(required = true)
		private boolean instapaper;

		@Schema(required = true)
		private boolean buffer;
	}
}
