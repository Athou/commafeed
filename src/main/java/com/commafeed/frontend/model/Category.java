package com.commafeed.frontend.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@SuppressWarnings("serial")
@ApiModel(description = "Entry details")
@Data
public class Category implements Serializable {

	@ApiModelProperty(value = "category id", required = true)
	private String id;

	@ApiModelProperty(value = "parent category id")
	private String parentId;

	@ApiModelProperty(value = "category id", required = true)
	private String name;

	@ApiModelProperty(value = "category children categories", required = true)
	private List<Category> children = new ArrayList<>();

	@ApiModelProperty(value = "category feeds", required = true)
	private List<Subscription> feeds = new ArrayList<>();

	@ApiModelProperty(value = "wether the category is expanded or collapsed", required = true)
	private boolean expanded;

	@ApiModelProperty(value = "position of the category in the list", required = true)
	private Integer position;
}