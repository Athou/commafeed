package com.commafeed.frontend.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Data;

@SuppressWarnings("serial")
@Schema(description = "Entry details")
@Data
@RegisterForReflection
public class Category implements Serializable {

	@Schema(description = "category id", requiredMode = RequiredMode.REQUIRED)
	private String id;

	@Schema(description = "parent category id")
	private String parentId;

	@Schema(description = "parent category name")
	private String parentName;

	@Schema(description = "category id", requiredMode = RequiredMode.REQUIRED)
	private String name;

	@Schema(description = "category children categories", requiredMode = RequiredMode.REQUIRED)
	private List<Category> children = new ArrayList<>();

	@Schema(description = "category feeds", requiredMode = RequiredMode.REQUIRED)
	private List<Subscription> feeds = new ArrayList<>();

	@Schema(description = "whether the category is expanded or collapsed", requiredMode = RequiredMode.REQUIRED)
	private boolean expanded;

	@Schema(description = "position of the category in the list", requiredMode = RequiredMode.REQUIRED)
	private int position;
}