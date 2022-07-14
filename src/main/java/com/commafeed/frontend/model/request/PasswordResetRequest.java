package com.commafeed.frontend.model.request;

import java.io.Serializable;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@SuppressWarnings("serial")
@Data
@ApiModel
public class PasswordResetRequest implements Serializable {

	@ApiModelProperty(value = "email address for password recovery", required = true)
	@Email
	@NotEmpty
	private String email;
}
