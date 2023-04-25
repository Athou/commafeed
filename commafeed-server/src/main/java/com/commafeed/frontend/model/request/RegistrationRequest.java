package com.commafeed.frontend.model.request;

import java.io.Serializable;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import com.commafeed.frontend.auth.ValidPassword;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@SuppressWarnings("serial")
@Data
@ApiModel
public class RegistrationRequest implements Serializable {

	@ApiModelProperty(value = "username, between 3 and 32 characters", required = true)
	@NotEmpty
	@Size(min = 3, max = 32)
	private String name;

	@ApiModelProperty(value = "password, minimum 6 characters", required = true)
	@NotEmpty
	@ValidPassword
	private String password;

	@ApiModelProperty(value = "email address for password recovery", required = true)
	@Email
	@NotEmpty
	@Size(max = 255)
	private String email;

}
