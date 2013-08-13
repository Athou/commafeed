package com.commafeed.frontend.model.request;

import java.io.Serializable;

import lombok.Data;

import com.wordnik.swagger.annotations.ApiClass;
import com.wordnik.swagger.annotations.ApiProperty;

@SuppressWarnings("serial")
@ApiClass("Category modification request")
@Data
public class CategoryModificationRequest implements Serializable {

	@ApiProperty(value = "id", required = true)
	private Long id;

	@ApiProperty(value = "new name, null if not changed")
	private String name;

	@ApiProperty(value = "new parent category id")
	private String parentId;

	@ApiProperty(value = "new display position, null if not changed")
	private Integer position;

}
