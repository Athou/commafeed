package com.commafeed.frontend.model.request;

import java.io.Serializable;

import lombok.Data;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@SuppressWarnings("serial")
@Data
@ApiModel
public class RegistrationRequest implements Serializable {

	@ApiModelProperty(value = "username, between 3 and 32 characters", required = true)
	@Length(min = 3, max = 32)
	@NotEmpty
	private String name;

	@ApiModelProperty(value = "password, minimum 6 characters", required = true)
	@Length(min = 6)
	@NotEmpty
	private String password;

	@ApiModelProperty(value = "email address for password recovery", required = true)
	@Email
	@NotEmpty
	private String email;

}
