package com.commafeed.frontend.model.request;

import java.io.Serializable;

import com.commafeed.security.password.ValidPassword;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@SuppressWarnings("serial")
@Data
@Schema
public class RegistrationRequest implements Serializable {

	@Schema(description = "username, between 3 and 32 characters", requiredMode = RequiredMode.REQUIRED)
	@NotEmpty
	@Size(min = 3, max = 32)
	private String name;

	@Schema(description = "password, minimum 6 characters", requiredMode = RequiredMode.REQUIRED)
	@NotEmpty
	@ValidPassword
	private String password;

	@Schema(description = "email address for password recovery", requiredMode = RequiredMode.REQUIRED)
	@Email
	@NotEmpty
	@Size(max = 255)
	private String email;

}
