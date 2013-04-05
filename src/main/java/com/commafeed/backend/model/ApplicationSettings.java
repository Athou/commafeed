package com.commafeed.backend.model;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "APPLICATIONSETTINGS")
@SuppressWarnings("serial")
public class ApplicationSettings extends AbstractModel {

	private String publicUrl;
	private boolean allowRegistrations = false;
	private String googleClientId;
	private String googleClientSecret;

	public String getPublicUrl() {
		return publicUrl;
	}

	public void setPublicUrl(String publicUrl) {
		this.publicUrl = publicUrl;
	}

	public boolean isAllowRegistrations() {
		return allowRegistrations;
	}

	public void setAllowRegistrations(boolean allowRegistrations) {
		this.allowRegistrations = allowRegistrations;
	}

	public String getGoogleClientId() {
		return googleClientId;
	}

	public void setGoogleClientId(String googleClientId) {
		this.googleClientId = googleClientId;
	}

	public String getGoogleClientSecret() {
		return googleClientSecret;
	}

	public void setGoogleClientSecret(String googleClientSecret) {
		this.googleClientSecret = googleClientSecret;
	}

}
