package com.commafeed.frontend.model.request;

import java.io.Serializable;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@SuppressWarnings("serial")
@Schema(description = "Mark Request")
@Data
public class MarkRequest implements Serializable {

	@Schema(description = "entry id, category id, 'all' or 'starred'", requiredMode = RequiredMode.REQUIRED)
	@NotEmpty
	@Size(max = 128)
	private String id;

	@Schema(description = "mark as read or unread", requiredMode = RequiredMode.REQUIRED)
	private boolean read;

	@Schema(description = "mark only entries older than this", requiredMode = RequiredMode.NOT_REQUIRED)
	private Long olderThan;

	@Schema(
			description = "pass the timestamp you got from the entry list to avoid marking entries that may have been fetched in the mean time and never displayed",
			requiredMode = RequiredMode.NOT_REQUIRED)
	private Long insertedBefore;

	@Schema(
			description = "only mark read if a feed has these keywords in the title or rss content",
			requiredMode = RequiredMode.NOT_REQUIRED)
	@Size(max = 128)
	private String keywords;

	@Schema(
			description = "if marking a category or 'all', exclude those subscriptions from the marking",
			requiredMode = RequiredMode.NOT_REQUIRED)
	private List<Long> excludedSubscriptions;

}
