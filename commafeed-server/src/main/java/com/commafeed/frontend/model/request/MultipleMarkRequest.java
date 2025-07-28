package com.commafeed.frontend.model.request;

import java.io.Serializable;
import java.util.List;

import jakarta.validation.Valid;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import lombok.Data;

@SuppressWarnings("serial")
@Schema(description = "Multiple Mark Request")
@Data
public class MultipleMarkRequest implements Serializable {

	@Schema(description = "list of mark requests", required = true)
	private List<@Valid MarkRequest> requests;

}
