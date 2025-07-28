package com.commafeed.frontend.model.request;

import java.io.Serializable;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.commafeed.security.password.ValidPassword;

import lombok.Data;

@SuppressWarnings("serial")
@Schema(description = "Profile modification request")
@Data
public class ProfileModificationRequest implements Serializable {
	@Schema(description = "current user password, required to change profile data", required = true)
	@NotEmpty
	@Size(max = 128)
	private String currentPassword;

	@Schema(description = "changes email of the user, if specified")
	@Size(max = 255)
	private String email;

	@Schema(description = "changes password of the user, if specified")
	@ValidPassword
	private String newPassword;

	@Schema(description = "generate a new api key")
	private boolean newApiKey;

}
