package com.commafeed.frontend.model.request;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import com.commafeed.frontend.auth.ValidPassword;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@SuppressWarnings("serial")
@ApiModel(description = "Profile modification request")
@Data
public class ProfileModificationRequest implements Serializable {
	@ApiModelProperty(value = "current user password, required to change profile data", required = true)
	@NotEmpty
	@Size(max = 128)
	private String currentPassword;

	@ApiModelProperty(value = "changes email of the user, if specified")
	@Size(max = 255)
	private String email;

	@ApiModelProperty(value = "changes password of the user, if specified")
	@ValidPassword
	private String newPassword;

	@ApiModelProperty(value = "generate a new api key")
	private boolean newApiKey;

}
