package com.commafeed.frontend.model.request;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@SuppressWarnings("serial")
@ApiModel(description = "Star Request")
@Data
public class StarRequest implements Serializable {

	@ApiModelProperty(value = "id", required = true)
	@NotEmpty
	@Size(max = 128)
	private String id;

	@ApiModelProperty(value = "feed id", required = true)
	private Long feedId;

	@ApiModelProperty(value = "starred or not", required = true)
	private boolean starred;

}
