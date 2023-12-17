package com.commafeed.frontend.model;

import java.io.Serializable;
import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Data;

@SuppressWarnings("serial")
@Schema(description = "User information")
@Data
public class UserModel implements Serializable {

	@Schema(description = "user id", requiredMode = RequiredMode.REQUIRED)
	private Long id;

	@Schema(description = "user name", requiredMode = RequiredMode.REQUIRED)
	private String name;

	@Schema(description = "user email, if any")
	private String email;

	@Schema(description = "api key")
	private String apiKey;

	@Schema(description = "user password, never returned by the api")
	private String password;

	@Schema(description = "account status", requiredMode = RequiredMode.REQUIRED)
	private boolean enabled;

	@Schema(description = "account creation date", type = "number")
	private Date created;

	@Schema(description = "last login date", type = "number")
	private Date lastLogin;

	@Schema(description = "user is admin", requiredMode = RequiredMode.REQUIRED)
	private boolean admin;

}
