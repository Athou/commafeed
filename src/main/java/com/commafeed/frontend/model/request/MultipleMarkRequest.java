package com.commafeed.frontend.model.request;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@SuppressWarnings("serial")
@ApiModel("Multiple Mark Request")
@Data
public class MultipleMarkRequest implements Serializable {

	@ApiModelProperty(value = "list of mark requests", required = true)
	private List<MarkRequest> requests;

}
