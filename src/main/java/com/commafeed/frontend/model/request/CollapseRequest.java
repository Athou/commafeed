package com.commafeed.frontend.model.request;

import java.io.Serializable;

import lombok.Data;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@SuppressWarnings("serial")
@ApiModel("Mark Request")
@Data
public class CollapseRequest implements Serializable {

	@ApiModelProperty(value = "category id", required = true)
	private Long id;

	@ApiModelProperty(value = "collapse", required = true)
	private boolean collapse;

}
