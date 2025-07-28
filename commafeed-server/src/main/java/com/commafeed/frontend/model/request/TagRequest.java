package com.commafeed.frontend.model.request;

import java.io.Serializable;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import lombok.Data;

@SuppressWarnings("serial")
@Schema(description = "Tag Request")
@Data
public class TagRequest implements Serializable {

	@Schema(description = "entry id", required = true)
	private Long entryId;

	@Schema(description = "tags", required = true)
	private List<String> tags;

}
