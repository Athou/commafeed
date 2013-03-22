package com.commafeed.frontend.rest.model;

import java.util.List;

import com.google.common.collect.Lists;

public class Category {
	private String id;
	private String name;
	private List<Category> children = Lists.newArrayList();
	private List<Subscription> feeds = Lists.newArrayList();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Category> getChildren() {
		return children;
	}

	public void setChildren(List<Category> children) {
		this.children = children;
	}

	public List<Subscription> getFeeds() {
		return feeds;
	}

	public void setFeeds(List<Subscription> feeds) {
		this.feeds = feeds;
	}

}