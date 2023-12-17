package com.commafeed.frontend.model.request;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
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

	@Schema(
			description = "only entries older than this, pass the timestamp you got from the entry list to prevent marking an entry that was not retrieved",
			requiredMode = RequiredMode.NOT_REQUIRED)
	private Long olderThan;

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
