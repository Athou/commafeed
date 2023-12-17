package com.commafeed.frontend.model.request;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Size;
import lombok.Data;

@SuppressWarnings("serial")
@Schema(description = "Feed modification request")
@Data
public class FeedModificationRequest implements Serializable {

	@Schema(description = "id", requiredMode = RequiredMode.REQUIRED)
	private Long id;

	@Schema(description = "new name, null if not changed")
	@Size(max = 128)
	private String name;

	@Schema(description = "new parent category id")
	@Size(max = 128)
	private String categoryId;

	@Schema(description = "new display position, null if not changed")
	private Integer position;

	@Schema(description = "JEXL string evaluated on new entries to mark them as read if they do not match")
	@Size(max = 4096)
	private String filter;

}
