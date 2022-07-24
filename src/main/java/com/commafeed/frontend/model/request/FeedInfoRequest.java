package com.commafeed.frontend.model.request;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@SuppressWarnings("serial")
@ApiModel(description = "Feed information request")
@Data
public class FeedInfoRequest implements Serializable {

	@ApiModelProperty(value = "feed url", required = true)
	@NotEmpty
	@Size(max = 4096)
	private String url;

}
