package com.commafeed.frontend.model.request;

import java.io.Serializable;

import lombok.Data;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@SuppressWarnings("serial")
@ApiModel("Profile modification request")
@Data
public class ProfileModificationRequest implements Serializable {

	@ApiModelProperty(value = "changes email of the user, if specified")
	private String email;

	@ApiModelProperty(value = "changes password of the user, if specified")
	private String password;

	@ApiModelProperty(value = "generate a new api key")
	private boolean newApiKey;

}
