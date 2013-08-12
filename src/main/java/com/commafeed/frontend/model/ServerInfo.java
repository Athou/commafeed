package com.commafeed.frontend.model;

import java.io.Serializable;
import java.util.Map;

import lombok.Data;

import com.google.common.collect.Maps;
import com.wordnik.swagger.annotations.ApiClass;

@SuppressWarnings("serial")
@ApiClass("Server infos")
@Data
public class ServerInfo implements Serializable {

	private String announcement;
	private String version;
	private String gitCommit;
	private Map<String, String> supportedLanguages = Maps.newHashMap();

}
