package com.commafeed.frontend.model.request;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

import com.wordnik.swagger.annotations.ApiClass;
import com.wordnik.swagger.annotations.ApiProperty;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@ApiClass("Profile modification request")
@Data
public class ProfileModificationRequest {

	@ApiProperty(value = "changes email of the user, if specified")
	private String email;

	@ApiProperty(value = "changes password of the user, if specified")
	private String password;

	@ApiProperty(value = "generate a new api key")
	private boolean newApiKey;

}
