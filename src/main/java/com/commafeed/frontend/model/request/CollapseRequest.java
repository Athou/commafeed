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
@ApiClass("Mark Request")
@Data
public class CollapseRequest implements Serializable {

	@ApiProperty(value = "category id", required = true)
	private Long id;

	@ApiProperty(value = "collapse", required = true)
	private boolean collapse;

}
