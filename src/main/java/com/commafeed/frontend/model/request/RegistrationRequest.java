package com.commafeed.frontend.model.request;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.wordnik.swagger.annotations.ApiProperty;

@SuppressWarnings("serial")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class RegistrationRequest implements Serializable {

	@ApiProperty(value = "username, between 3 and 32 characters", required = true)
	private String name;

	@ApiProperty(value = "password, minimum 6 characters", required = true)
	private String password;

	@ApiProperty(value = "email address for password recovery", required = true)
	private String email;

	@ApiProperty(value = "not used through the api", required = false)
	private boolean googleImport = true;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isGoogleImport() {
		return googleImport;
	}

	public void setGoogleImport(boolean googleImport) {
		this.googleImport = googleImport;
	}

}
