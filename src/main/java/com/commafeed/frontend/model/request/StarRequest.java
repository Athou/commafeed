package com.commafeed.frontend.model.request;

import java.io.Serializable;

import lombok.Data;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@SuppressWarnings("serial")
@ApiModel("Star Request")
@Data
public class StarRequest implements Serializable {

	@ApiModelProperty(value = "id", required = true)
	private String id;

	@ApiModelProperty(value = "feed id", required = true)
	private Long feedId;

	@ApiModelProperty(value = "starred or not")
	private boolean starred;

}
