package com.commafeed.frontend.model.request;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Data;

@SuppressWarnings("serial")
@Schema(description = "Feed information request")
@Data
public class FeedInfoRequest implements Serializable {

	@Schema(description = "feed url", requiredMode = RequiredMode.REQUIRED)
	@NotEmpty
	@Size(max = 4096)
	private String url;

}
