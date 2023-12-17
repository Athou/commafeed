package com.commafeed.frontend.model.request;

import java.io.Serializable;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import lombok.Data;

@SuppressWarnings("serial")
@Schema(description = "Multiple Mark Request")
@Data
public class MultipleMarkRequest implements Serializable {

	@Schema(description = "list of mark requests", requiredMode = RequiredMode.REQUIRED)
	private List<@Valid MarkRequest> requests;

}
