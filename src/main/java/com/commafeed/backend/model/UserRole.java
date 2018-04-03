package com.commafeed.backend.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

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

	@Override
	public String toString() {
		return "UserRole{" +
				"user=" + user +
				", role=" + role +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof UserRole)) {
			return false;
		}

		UserRole userRole = (UserRole) o;

		if (getUser() != null ? !getUser().equals(userRole.getUser()) :
				userRole.getUser() != null) {
			return false;
		}
		return getRole() == userRole.getRole();

	}
}
