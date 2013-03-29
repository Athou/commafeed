package com.commafeed.frontend.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class UserModel implements Serializable {

	private String name;
	private boolean enabled;
	private boolean admin;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
