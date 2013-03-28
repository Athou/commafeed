package com.commafeed.backend.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "USERROLES")
@SuppressWarnings("serial")
public class UserRole extends AbstractModel {

	@OneToOne
	@JoinColumn(name = "user_id")
	private User user;

	@Column(name = "roleName")
	private String role;

	public UserRole() {

	}

	public UserRole(User user, String role) {
		this.user = user;
		this.role = role;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

}
