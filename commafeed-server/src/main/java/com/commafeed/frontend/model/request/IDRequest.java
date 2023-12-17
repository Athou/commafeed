package com.commafeed.frontend.model.request;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Data;

@SuppressWarnings("serial")
@Schema
@Data
public class IDRequest implements Serializable {

	@Schema(requiredMode = RequiredMode.REQUIRED)
	private Long id;

}
