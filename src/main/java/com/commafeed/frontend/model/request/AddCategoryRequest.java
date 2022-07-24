package com.commafeed.frontend.model.request;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@SuppressWarnings("serial")
@ApiModel(description = "Add Category Request")
@Data
public class AddCategoryRequest implements Serializable {

	@ApiModelProperty(value = "name", required = true)
	@NotEmpty
	@Size(max = 128)
	private String name;

	@ApiModelProperty(value = "parent category id, if any")
	@Size(max = 128)
	private String parentId;

}
