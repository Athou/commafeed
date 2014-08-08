package com.commafeed.frontend.model.request;

import java.io.Serializable;

import lombok.Data;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@SuppressWarnings("serial")
@Data
@ApiModel
public class RegistrationRequest implements Serializable {

	@ApiModelProperty(value = "username, between 3 and 32 characters", required = true)
	private String name;

	@ApiModelProperty(value = "password, minimum 6 characters", required = true)
	private String password;

	@ApiModelProperty(value = "email address for password recovery", required = true)
	private String email;

}
