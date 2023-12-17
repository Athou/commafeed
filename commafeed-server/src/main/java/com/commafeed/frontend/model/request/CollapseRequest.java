package com.commafeed.frontend.model.request;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Data;

@SuppressWarnings("serial")
@Schema(description = "Mark Request")
@Data
public class CollapseRequest implements Serializable {

	@Schema(description = "category id", requiredMode = RequiredMode.REQUIRED)
	private Long id;

	@Schema(description = "collapse", requiredMode = RequiredMode.REQUIRED)
	private boolean collapse;

}
