package com.commafeed.frontend.model;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@SuppressWarnings("serial")
@ApiModel(description = "Feed details")
@Data
public class FeedInfo implements Serializable {

	@ApiModelProperty(value = "url", required = true)
	private String url;

	@ApiModelProperty(value = "title", required = true)
	private String title;

}
