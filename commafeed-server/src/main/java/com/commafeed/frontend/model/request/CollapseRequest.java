package com.commafeed.frontend.model.request;

import java.io.Serializable;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import lombok.Data;

@SuppressWarnings("serial")
@Schema(description = "Mark Request")
@Data
public class CollapseRequest implements Serializable {

	@Schema(description = "category id", required = true)
	private Long id;

	@Schema(description = "collapse", required = true)
	private boolean collapse;

}
