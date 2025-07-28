package com.commafeed.frontend.model.request;

import java.io.Serializable;

import jakarta.validation.constraints.Size;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import lombok.Data;

@SuppressWarnings("serial")
@Schema(description = "Category modification request")
@Data
public class CategoryModificationRequest implements Serializable {

	@Schema(description = "id", required = true)
	private Long id;

	@Schema(description = "new name, null if not changed")
	@Size(max = 128)
	private String name;

	@Schema(description = "new parent category id")
	@Size(max = 128)
	private String parentId;

	@Schema(description = "new display position, null if not changed")
	private Integer position;

}
