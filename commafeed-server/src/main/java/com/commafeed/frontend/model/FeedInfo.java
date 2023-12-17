package com.commafeed.frontend.model;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Data;

@SuppressWarnings("serial")
@Schema(description = "Feed details")
@Data
public class FeedInfo implements Serializable {

	@Schema(description = "url", requiredMode = RequiredMode.REQUIRED)
	private String url;

	@Schema(description = "title", requiredMode = RequiredMode.REQUIRED)
	private String title;

}
