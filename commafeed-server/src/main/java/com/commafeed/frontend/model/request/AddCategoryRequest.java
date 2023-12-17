package com.commafeed.frontend.model.request;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@SuppressWarnings("serial")
@Schema(description = "Add Category Request")
@Data
public class AddCategoryRequest implements Serializable {

	@Schema(description = "name", requiredMode = RequiredMode.REQUIRED)
	@NotEmpty
	@Size(max = 128)
	private String name;

	@Schema(description = "parent category id, if any")
	@Size(max = 128)
	private String parentId;

}
