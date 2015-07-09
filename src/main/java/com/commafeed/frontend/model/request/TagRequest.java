package com.commafeed.frontend.model.request;

import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@SuppressWarnings("serial")
@ApiModel("Tag Request")
@Data
public class TagRequest implements Serializable {

	@ApiModelProperty(value = "entry id", required = true)
	private Long entryId;

	@ApiModelProperty(value = "tags")
	private List<String> tags;

}
