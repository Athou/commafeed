package com.commafeed.frontend.model;

import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@SuppressWarnings("serial")
@ApiModel("User information")
@Data
public class UserModel implements Serializable {

	@ApiModelProperty(value = "user id", required = true)
	private Long id;

	@ApiModelProperty(value = "user name", required = true)
	private String name;

	@ApiModelProperty("user email, if any")
	private String email;

	@ApiModelProperty("api key")
	private String apiKey;

	@ApiModelProperty(value = "user password, never returned by the api")
	private String password;

	@ApiModelProperty(value = "account status")
	private boolean enabled;

	@ApiModelProperty(value = "account creation date")
	private Date created;

	@ApiModelProperty(value = "last login date")
	private Date lastLogin;

	@ApiModelProperty(value = "user is admin")
	private boolean admin;

}
