package com.commafeed.frontend.model.request;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@SuppressWarnings("serial")
@ApiModel("Mark Request")
@Data
public class CollapseRequest implements Serializable {

	@ApiModelProperty(value = "category id", required = true)
	private Long id;

	@ApiModelProperty(value = "collapse", required = true)
	private boolean collapse;

}
