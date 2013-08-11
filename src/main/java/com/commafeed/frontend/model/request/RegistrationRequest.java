package com.commafeed.frontend.model.request;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

import com.wordnik.swagger.annotations.ApiProperty;

@SuppressWarnings("serial")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class RegistrationRequest implements Serializable {

	@ApiProperty(value = "username, between 3 and 32 characters", required = true)
	private String name;

	@ApiProperty(value = "password, minimum 6 characters", required = true)
	private String password;

	@ApiProperty(value = "email address for password recovery", required = true)
	private String email;

}
