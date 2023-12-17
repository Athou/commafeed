package com.commafeed.frontend.model.request;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@SuppressWarnings("serial")
@Data
@Schema
public class PasswordResetRequest implements Serializable {

	@Schema(description = "email address for password recovery", requiredMode = RequiredMode.REQUIRED)
	@Email
	@NotEmpty
	@Size(max = 255)
	private String email;
}
