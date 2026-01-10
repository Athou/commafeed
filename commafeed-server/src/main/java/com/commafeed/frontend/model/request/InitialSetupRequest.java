package com.commafeed.frontend.model.request;

import java.io.Serializable;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.commafeed.security.password.ValidPassword;

import lombok.Data;

@SuppressWarnings("serial")
@Schema(description = "Initial admin account setup request")
@Data
public class InitialSetupRequest implements Serializable {

	@Schema(description = "admin username", required = true)
	private String name;

	@Schema(description = "admin password", required = true)
	@ValidPassword
	private String password;

	@Schema(description = "admin email")
	private String email;
}
