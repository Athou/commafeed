package com.commafeed.frontend.model.request;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@SuppressWarnings("serial")
@ApiModel("Add Category Request")
@Data
public class AddCategoryRequest implements Serializable {

	@ApiModelProperty(value = "name", required = true)
	private String name;

	@ApiModelProperty(value = "parent category id, if any")
	private String parentId;

}
