package com.commafeed.frontend.model.request;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@SuppressWarnings("serial")
@ApiModel("Subscription request")
@Data
public class SubscribeRequest implements Serializable {

	@ApiModelProperty(value = "url of the feed", required = true)
	private String url;

	@ApiModelProperty(value = "name of the feed for the user", required = true)
	private String title;

	@ApiModelProperty(value = "id of the user category to place the feed in")
	private String categoryId;

}
