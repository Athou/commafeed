package com.commafeed.frontend.model.request;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@SuppressWarnings("serial")
@ApiModel("Category modification request")
@Data
public class CategoryModificationRequest implements Serializable {

	@ApiModelProperty(value = "id", required = true)
	private Long id;

	@ApiModelProperty(value = "new name, null if not changed")
	private String name;

	@ApiModelProperty(value = "new parent category id")
	private String parentId;

	@ApiModelProperty(value = "new display position, null if not changed")
	private Integer position;

}
