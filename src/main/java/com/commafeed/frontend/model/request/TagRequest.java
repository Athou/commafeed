package com.commafeed.frontend.model.request;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

import com.wordnik.swagger.annotations.ApiClass;
import com.wordnik.swagger.annotations.ApiProperty;

@SuppressWarnings("serial")
@ApiClass("Tag Request")
@Data
public class TagRequest implements Serializable {

	@ApiProperty(value = "entry id", required = true)
	private Long entryId;

	@ApiProperty(value = "tags")
	private List<String> tags;

}
