package com.commafeed.frontend.model;

import java.io.Serializable;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.api.client.util.Maps;
import com.wordnik.swagger.annotations.ApiClass;

@SuppressWarnings("serial")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@ApiClass("Server infos")
public class ServerInfo implements Serializable {

	private String announcement;
	private Map<String, String> supportedLanguages = Maps.newHashMap();

	public String getAnnouncement() {
		return announcement;
	}

	public void setAnnouncement(String announcement) {
		this.announcement = announcement;
	}

	public Map<String, String> getSupportedLanguages() {
		return supportedLanguages;
	}

	public void setSupportedLanguages(Map<String, String> supportedLanguages) {
		this.supportedLanguages = supportedLanguages;
	}

}
