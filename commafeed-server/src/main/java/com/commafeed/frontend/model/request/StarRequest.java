package com.commafeed.frontend.model.request;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@SuppressWarnings("serial")
@Schema(description = "Star Request")
@Data
public class StarRequest implements Serializable {

	@Schema(description = "id", requiredMode = RequiredMode.REQUIRED)
	@NotEmpty
	@Size(max = 128)
	private String id;

	@Schema(description = "feed id", requiredMode = RequiredMode.REQUIRED)
	private Long feedId;

	@Schema(description = "starred or not", requiredMode = RequiredMode.REQUIRED)
	private boolean starred;

}
