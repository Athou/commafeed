package com.commafeed.frontend.model.request;

import java.io.Serializable;

import lombok.Data;

import com.wordnik.swagger.annotations.ApiClass;
import com.wordnik.swagger.annotations.ApiProperty;

@SuppressWarnings("serial")
@ApiClass("Mark Request")
@Data
public class CollapseRequest implements Serializable {

	@ApiProperty(value = "category id", required = true)
	private Long id;

	@ApiProperty(value = "collapse", required = true)
	private boolean collapse;

}
