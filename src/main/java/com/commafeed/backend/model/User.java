package com.commafeed.backend.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang3.time.DateUtils;

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

}
