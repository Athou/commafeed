package com.commafeed.frontend.model.request;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@SuppressWarnings("serial")
@ApiModel(description = "Subscription request")
@Data
public class SubscribeRequest implements Serializable {

	@ApiModelProperty(value = "url of the feed", required = true)
	@NotEmpty
	@Size(max = 4096)
	private String url;

	@ApiModelProperty(value = "name of the feed for the user", required = true)
	@NotEmpty
	@Size(max = 128)
	private String title;

	@ApiModelProperty(value = "id of the user category to place the feed in")
	@Size(max = 128)
	private String categoryId;

}
