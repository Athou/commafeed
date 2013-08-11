package com.commafeed.frontend.model.request;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

import com.wordnik.swagger.annotations.ApiClass;
import com.wordnik.swagger.annotations.ApiProperty;

@SuppressWarnings("serial")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@ApiClass("Add Category Request")
@Data
public class AddCategoryRequest implements Serializable {

	@ApiProperty(value = "name", required = true)
	private String name;

	@ApiProperty(value = "parent category id, if any")
	private String parentId;

}
