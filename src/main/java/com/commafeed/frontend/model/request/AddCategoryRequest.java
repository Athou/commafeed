package com.commafeed.frontend.model.request;

import java.io.Serializable;

import lombok.Data;

import com.wordnik.swagger.annotations.ApiClass;
import com.wordnik.swagger.annotations.ApiProperty;

@SuppressWarnings("serial")
@ApiClass("Add Category Request")
@Data
public class AddCategoryRequest implements Serializable {

	@ApiProperty(value = "name", required = true)
	private String name;

	@ApiProperty(value = "parent category id, if any")
	private String parentId;

}
