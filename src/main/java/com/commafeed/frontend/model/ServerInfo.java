package com.commafeed.frontend.model;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@SuppressWarnings("serial")
@ApiModel(description = "Server infos")
@Data
public class ServerInfo implements Serializable {

	@ApiModelProperty
	private String announcement;

	@ApiModelProperty(required = true)
	private String version;

	@ApiModelProperty(required = true)
	private String gitCommit;

	@ApiModelProperty(required = true)
	private boolean allowRegistrations;

	@ApiModelProperty
	private String googleAnalyticsCode;

	@ApiModelProperty(required = true)
	private boolean smtpEnabled;

}
