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
@ApiClass("Star Request")
@Data
public class StarRequest implements Serializable {

	@ApiProperty(value = "id", required = true)
	private String id;

	@ApiProperty(value = "feed id", required = true)
	private Long feedId;

	@ApiProperty(value = "starred or not")
	private boolean starred;

}
