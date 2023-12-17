package com.commafeed.frontend.model.request;

import java.io.Serializable;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Data;

@SuppressWarnings("serial")
@Schema(description = "Tag Request")
@Data
public class TagRequest implements Serializable {

	@Schema(description = "entry id", requiredMode = RequiredMode.REQUIRED)
	private Long entryId;

	@Schema(description = "tags", requiredMode = RequiredMode.REQUIRED)
	private List<String> tags;

}
