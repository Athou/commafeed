package com.commafeed.frontend.model.request;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Data;

@SuppressWarnings("serial")
@Schema(description = "Save User information")
@Data
public class AdminSaveUserRequest implements Serializable {

	@Schema(description = "user id")
	private Long id;

	@Schema(description = "user name", requiredMode = RequiredMode.REQUIRED)
	private String name;

	@Schema(description = "user email, if any")
	private String email;

	@Schema(description = "user password")
	private String password;

	@Schema(description = "account status", requiredMode = RequiredMode.REQUIRED)
	private boolean enabled;

	@Schema(description = "user is admin", requiredMode = RequiredMode.REQUIRED)
	private boolean admin;
}
