package com.commafeed.frontend.model;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;
import com.wordnik.swagger.annotations.ApiClass;
import com.wordnik.swagger.annotations.ApiProperty;

@SuppressWarnings("serial")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@ApiClass("Entry details")
public class Category implements Serializable {

	@ApiProperty("category id")
	private String id;

	@ApiProperty("parent category id")
	private String parentId;

	@ApiProperty("category id")
	private String name;

	@ApiProperty("category children categories")
	private List<Category> children = Lists.newArrayList();

	@ApiProperty("category feeds")
	private List<Subscription> feeds = Lists.newArrayList();

	@ApiProperty("wether the category is expanded or collapsed")
	private boolean expanded;

	@ApiProperty("position of the category in the list")
	private Integer position;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
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

	public boolean isExpanded() {
		return expanded;
	}

	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

}