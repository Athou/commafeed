package com.commafeed.frontend.model.request;

import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@SuppressWarnings("serial")
@ApiModel("Multiple Mark Request")
@Data
public class MultipleMarkRequest implements Serializable {

	@ApiModelProperty(value = "list of mark requests", required = true)
	private List<MarkRequest> requests;

}
