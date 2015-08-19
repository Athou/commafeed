package com.commafeed.frontend.model.request;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@SuppressWarnings("serial")
@ApiModel("Feed information request")
@Data
public class FeedInfoRequest implements Serializable {

	@ApiModelProperty(value = "feed url", required = true)
	private String url;

}
