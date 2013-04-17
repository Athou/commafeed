package com.commafeed.frontend.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.wordnik.swagger.annotations.ApiClass;
import com.wordnik.swagger.annotations.ApiProperty;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@ApiClass("Profile modification request")
public class ProfileModificationRequest {

	@ApiProperty(value = "changes email of the user, if specified")
	private String email;

	@ApiProperty(value = "changes password of the user, if specified")
	private String password;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
