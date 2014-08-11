package com.commafeed.frontend.model.request;

import java.io.Serializable;

import lombok.Data;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@SuppressWarnings("serial")
@Data
@ApiModel
public class LoginRequest implements Serializable {

	@ApiModelProperty(value = "username", required = true)
	private String name;

	@ApiModelProperty(value = "password", required = true)
	private String password;
}
