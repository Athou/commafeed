package com.commafeed.frontend.model;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

import com.google.common.collect.Lists;
import com.wordnik.swagger.annotations.ApiClass;
import com.wordnik.swagger.annotations.ApiProperty;

@SuppressWarnings("serial")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@ApiClass("Entry details")
@Data
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
}