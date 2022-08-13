package com.commafeed.frontend.model.request;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@SuppressWarnings("serial")
@Data
@ApiModel
public class LoginRequest implements Serializable {

	@ApiModelProperty(value = "username", required = true)
	@Size(min = 3, max = 32)
	private String name;

	@ApiModelProperty(value = "password", required = true)
	@NotEmpty
	@Size(max = 128)
	private String password;
}
