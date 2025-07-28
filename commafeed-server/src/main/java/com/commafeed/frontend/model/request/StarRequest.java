package com.commafeed.frontend.model.request;

import java.io.Serializable;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import lombok.Data;

@SuppressWarnings("serial")
@Schema(description = "Star Request")
@Data
public class StarRequest implements Serializable {

	@Schema(description = "id", required = true)
	@NotEmpty
	@Size(max = 128)
	private String id;

	@Schema(description = "feed id", required = true)
	private Long feedId;

	@Schema(description = "starred or not", required = true)
	private boolean starred;

}
