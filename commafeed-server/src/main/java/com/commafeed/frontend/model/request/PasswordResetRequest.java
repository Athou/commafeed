package com.commafeed.frontend.model.request;

import java.io.Serializable;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
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
