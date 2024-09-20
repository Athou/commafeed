package com.commafeed.frontend.model;

import java.io.Serializable;
import java.time.Instant;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Data;

@SuppressWarnings("serial")
@Schema(description = "User information")
@Data
@RegisterForReflection
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
	private Instant created;

	@Schema(description = "last login date", type = "number")
	private Instant lastLogin;

	@Schema(description = "user is admin", requiredMode = RequiredMode.REQUIRED)
	private boolean admin;

	@Schema(description = "user last force refresh", type = "number")
	private Instant lastForceRefresh;

}
