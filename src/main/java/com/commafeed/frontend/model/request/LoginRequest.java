package com.commafeed.frontend.model.request;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@SuppressWarnings("serial")
@Data
@ApiModel
public class LoginRequest implements Serializable {

	@ApiModelProperty(value = "username", required = true)
	private String name;

	@ApiModelProperty(value = "password", required = true)
	private String password;
}
