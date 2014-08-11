package com.commafeed.frontend.model.request;

import java.io.Serializable;

import lombok.Data;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@SuppressWarnings("serial")
@ApiModel("Add Category Request")
@Data
public class AddCategoryRequest implements Serializable {

	@ApiModelProperty(value = "name", required = true)
	private String name;

	@ApiModelProperty(value = "parent category id, if any")
	private String parentId;

}
