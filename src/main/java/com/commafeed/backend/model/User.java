package com.commafeed.backend.model;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.time.DateUtils;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Date;

@Entity
@Table(name = "USERS")
@SuppressWarnings("serial")
@Getter
@Setter
public class User extends AbstractModel {

	@Column(length = 32, nullable = false, unique = true)
	private String name;

	@Column(length = 255, unique = true)
	private String email;

	@Column(length = 256, nullable = false)
	private byte[] password;

	@Column(length = 40, unique = true)
	private String apiKey;

	@Column(length = 8, nullable = false)
	private byte[] salt;

	@Column(nullable = false)
	private boolean disabled;

	@Temporal(TemporalType.TIMESTAMP)
	private Date lastLogin;

	@Temporal(TemporalType.TIMESTAMP)
	private Date created;

	@Column(length = 40)
	private String recoverPasswordToken;

	@Temporal(TemporalType.TIMESTAMP)
	private Date recoverPasswordTokenDate;

	@Column(name = "last_full_refresh")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastFullRefresh;

	public boolean shouldRefreshFeedsAt(Date when) {
		return (lastFullRefresh == null || lastFullRefreshMoreThan30MinutesBefore(when));
	}

	private boolean lastFullRefreshMoreThan30MinutesBefore(Date when) {
		return lastFullRefresh.before(DateUtils.addMinutes(when, -30));
	}

	@Override
	public String toString() {
		return "User{" +
				"name='" + name + '\'' +
				", email='" + email + '\'' +
				", password=" + Arrays.toString(password) +
				", apiKey='" + apiKey + '\'' +
				", salt=" + Arrays.toString(salt) +
				", disabled=" + disabled +
				", lastLogin=" + lastLogin +
				", created=" + created +
				", recoverPasswordToken='" + recoverPasswordToken + '\'' +
				", recoverPasswordTokenDate=" + recoverPasswordTokenDate +
				", lastFullRefresh=" + lastFullRefresh +
				'}';
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof User)) {
			return false;
		}

		User user = (User) o;

		if (isDisabled() != user.isDisabled()) {
			return false;
		}
		if (getName() != null ? !getName().equals(user.getName()) :
				user.getName() != null) {
			return false;
		}
		if (getEmail() != null ? !getEmail().equals(user.getEmail()) :
				user.getEmail() != null) {
			return false;
		}
		if (!Arrays.equals(getPassword(), user.getPassword())) {
			return false;
		}
		if (getApiKey() != null ? !getApiKey().equals(user.getApiKey()) :
				user.getApiKey() != null) {
			return false;
		}
		if (!Arrays.equals(getSalt(), user.getSalt())) {
			return false;
		}
		if (getLastLogin() != null ?
				!getLastLogin().equals(user.getLastLogin()) :
				user.getLastLogin() != null) {
			return false;
		}
		if (getCreated() != null ? !getCreated().equals(user.getCreated()) :
				user.getCreated() != null) {
			return false;
		}
		if (getRecoverPasswordToken() != null ?
				!getRecoverPasswordToken()
						.equals(user.getRecoverPasswordToken()) :
				user.getRecoverPasswordToken() != null) {
			return false;
		}
		if (getRecoverPasswordTokenDate() != null ?
				!getRecoverPasswordTokenDate()
						.equals(user.getRecoverPasswordTokenDate()) :
				user.getRecoverPasswordTokenDate() != null) {
			return false;
		}
		return getLastFullRefresh() != null ?
				getLastFullRefresh().equals(user.getLastFullRefresh()) :
				user.getLastFullRefresh() == null;
	}
}
