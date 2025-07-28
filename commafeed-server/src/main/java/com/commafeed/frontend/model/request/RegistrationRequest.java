package com.commafeed.frontend.model.request;

import java.io.Serializable;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.commafeed.security.password.ValidPassword;

import lombok.Data;

@SuppressWarnings("serial")
@Data
@Schema
public class RegistrationRequest implements Serializable {

	@Schema(description = "username, between 3 and 32 characters", required = true)
	@NotEmpty
	@Size(min = 3, max = 32)
	private String name;

	@Schema(description = "password, minimum 6 characters", required = true)
	@NotEmpty
	@ValidPassword
	private String password;

	@Schema(description = "email address for password recovery", required = true)
	@Email
	@NotEmpty
	@Size(max = 255)
	private String email;

}
