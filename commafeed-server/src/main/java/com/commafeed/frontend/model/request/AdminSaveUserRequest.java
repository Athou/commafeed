package com.commafeed.frontend.model.request;

import java.io.Serializable;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import lombok.Data;

@SuppressWarnings("serial")
@Schema(description = "Save User information")
@Data
public class AdminSaveUserRequest implements Serializable {

	@Schema(description = "user id")
	private Long id;

	@Schema(description = "user name", required = true)
	private String name;

	@Schema(description = "user email, if any")
	private String email;

	@Schema(description = "user password")
	private String password;

	@Schema(description = "account status", required = true)
	private boolean enabled;

	@Schema(description = "user is admin", required = true)
	private boolean admin;
}
