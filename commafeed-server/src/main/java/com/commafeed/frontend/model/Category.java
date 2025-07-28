package com.commafeed.frontend.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@SuppressWarnings("serial")
@Schema(description = "Entry details")
@Data
@RegisterForReflection
public class Category implements Serializable {

	@Schema(description = "category id", required = true)
	private String id;

	@Schema(description = "parent category id")
	private String parentId;

	@Schema(description = "parent category name")
	private String parentName;

	@Schema(description = "category id", required = true)
	private String name;

	@Schema(description = "category children categories", required = true)
	private List<Category> children = new ArrayList<>();

	@Schema(description = "category feeds", required = true)
	private List<Subscription> feeds = new ArrayList<>();

	@Schema(description = "whether the category is expanded or collapsed", required = true)
	private boolean expanded;

	@Schema(description = "position of the category in the list", required = true)
	private int position;
}