package com.commafeed.frontend.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@SuppressWarnings("serial")
@ApiModel("Entry details")
@Data
public class Category implements Serializable {

	@ApiModelProperty("category id")
	private String id;

	@ApiModelProperty("parent category id")
	private String parentId;

	@ApiModelProperty("category id")
	private String name;

	@ApiModelProperty("category children categories")
	private List<Category> children = new ArrayList<>();

	@ApiModelProperty("category feeds")
	private List<Subscription> feeds = new ArrayList<>();

	@ApiModelProperty("wether the category is expanded or collapsed")
	private boolean expanded;

	@ApiModelProperty("position of the category in the list")
	private Integer position;
}