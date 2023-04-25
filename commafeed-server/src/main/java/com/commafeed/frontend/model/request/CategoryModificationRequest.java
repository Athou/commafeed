package com.commafeed.frontend.model.request;

import java.io.Serializable;

import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@SuppressWarnings("serial")
@ApiModel(description = "Category modification request")
@Data
public class CategoryModificationRequest implements Serializable {

	@ApiModelProperty(value = "id", required = true)
	private Long id;

	@ApiModelProperty(value = "new name, null if not changed")
	@Size(max = 128)
	private String name;

	@ApiModelProperty(value = "new parent category id")
	@Size(max = 128)
	private String parentId;

	@ApiModelProperty(value = "new display position, null if not changed")
	private Integer position;

}
