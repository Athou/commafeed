package com.commafeed.frontend.model.request;

import java.io.Serializable;

import jakarta.validation.constraints.Size;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import lombok.Data;

@SuppressWarnings("serial")
@Schema(description = "Feed modification request")
@Data
public class FeedModificationRequest implements Serializable {

	@Schema(description = "id", required = true)
	private Long id;

	@Schema(description = "new name, null if not changed")
	@Size(max = 128)
	private String name;

	@Schema(description = "new parent category id")
	@Size(max = 128)
	private String categoryId;

	@Schema(description = "new display position, null if not changed")
	private Integer position;

	@Schema(description = "CEL string evaluated on new entries to mark them as read if they do not match")
	@Size(max = 4096)
	private String filter;

	/**
	 * Part of the auto-mark-read feature.
	 */
	@Schema(description = "auto-mark-as-read entries older than this number of days, null if not set")
	private Integer autoMarkAsReadAfterDays;

}
