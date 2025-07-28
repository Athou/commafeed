package com.commafeed.frontend.model;

import java.io.Serializable;
import java.time.Instant;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@SuppressWarnings("serial")
@Schema(description = "User information")
@Data
@RegisterForReflection
public class UserModel implements Serializable {

	@Schema(description = "user id", required = true)
	private Long id;

	@Schema(description = "user name", required = true)
	private String name;

	@Schema(description = "user email, if any")
	private String email;

	@Schema(description = "api key")
	private String apiKey;

	@Schema(description = "user password, never returned by the api")
	private String password;

	@Schema(description = "account status", required = true)
	private boolean enabled;

	@Schema(description = "account creation date", type = SchemaType.INTEGER)
	private Instant created;

	@Schema(description = "last login date", type = SchemaType.INTEGER)
	private Instant lastLogin;

	@Schema(description = "user is admin", required = true)
	private boolean admin;

	@Schema(description = "user last force refresh", type = SchemaType.INTEGER)
	private Instant lastForceRefresh;

}
