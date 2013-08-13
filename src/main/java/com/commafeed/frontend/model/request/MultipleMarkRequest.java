package com.commafeed.frontend.model.request;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

import com.wordnik.swagger.annotations.ApiClass;
import com.wordnik.swagger.annotations.ApiProperty;

@SuppressWarnings("serial")
@ApiClass("Multiple Mark Request")
@Data
public class MultipleMarkRequest implements Serializable {

	@ApiProperty(value = "list of mark requests", required = true)
	private List<MarkRequest> requests;

}
