package com.commafeed.frontend.model.request;

import java.io.Serializable;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;

import com.commafeed.frontend.auth.ValidPassword;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@SuppressWarnings("serial")
@Data
@ApiModel
public class RegistrationRequest implements Serializable {

	@ApiModelProperty(value = "username, between 3 and 32 characters", required = true)
	@Length(min = 3, max = 32)
	@NotEmpty
	private String name;

	@ApiModelProperty(value = "password, minimum 6 characters", required = true)
	@ValidPassword
	@NotEmpty
	private String password;

	@ApiModelProperty(value = "email address for password recovery", required = true)
	@Email
	@NotEmpty
	private String email;

}
