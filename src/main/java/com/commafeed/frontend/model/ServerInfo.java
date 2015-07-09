package com.commafeed.frontend.model;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@SuppressWarnings("serial")
@ApiModel("Server infos")
@Data
public class ServerInfo implements Serializable {

	private String announcement;
	private String version;
	private String gitCommit;
	private boolean allowRegistrations;
	private String googleAnalyticsCode;
	private boolean smtpEnabled;

}
