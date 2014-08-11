package com.commafeed.frontend.model.request;

import java.io.Serializable;

import lombok.Data;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@SuppressWarnings("serial")
@Data
@ApiModel
public class PasswordResetRequest implements Serializable {

	@ApiModelProperty(value = "email address for password recovery", required = true)
	@Email
	@NotEmpty
	private String email;
}
