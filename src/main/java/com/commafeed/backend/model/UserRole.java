package com.commafeed.backend.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "USERROLES")
@SuppressWarnings("serial")
@Getter
@Setter
public class UserRole extends AbstractModel {

	public static enum Role {
		USER, ADMIN
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "roleName", nullable = false)
	@Enumerated(EnumType.STRING)
	private Role role;

	public UserRole() {

	}

	public UserRole(User user, Role role) {
		this.user = user;
		this.role = role;
	}

}
