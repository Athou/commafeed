package com.commafeed.frontend.model;

import java.io.Serializable;
import java.util.Set;

import com.google.common.collect.Sets;

@SuppressWarnings("serial")
public class UserModel implements Serializable {

	private String name;
	private Set<String> roles = Sets.newHashSet();
	private boolean enabled;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<String> getRoles() {
		return roles;
	}

	public void setRoles(Set<String> roles) {
		this.roles = roles;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
