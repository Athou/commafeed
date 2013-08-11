package com.commafeed.frontend.model.request;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

import com.wordnik.swagger.annotations.ApiClass;
import com.wordnik.swagger.annotations.ApiProperty;

@SuppressWarnings("serial")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@ApiClass("Multiple Mark Request")
@Data
public class MultipleMarkRequest implements Serializable {

	@ApiProperty(value = "list of mark requests", required = true)
	private List<MarkRequest> requests;

}
