package com.commafeed.frontend.model;

import java.io.Serializable;

import lombok.Data;

import com.wordnik.swagger.annotations.ApiModel;

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
