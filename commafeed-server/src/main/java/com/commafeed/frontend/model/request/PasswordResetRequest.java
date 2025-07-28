package com.commafeed.frontend.model.request;

import java.io.Serializable;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import lombok.Data;

@SuppressWarnings("serial")
@Data
@Schema
public class PasswordResetRequest implements Serializable {

	@Schema(description = "email address for password recovery", required = true)
	@Email
	@NotEmpty
	@Size(max = 255)
	private String email;
}
