package com.commafeed.frontend.model.request;

import java.io.Serializable;

import lombok.Data;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@SuppressWarnings("serial")
@ApiModel("Feed information request")
@Data
public class FeedInfoRequest implements Serializable {

	@ApiModelProperty(value = "feed url", required = true)
	private String url;

}
