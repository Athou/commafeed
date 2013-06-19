package com.commafeed.backend.model;

import java.util.Date;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.collect.Sets;

@Entity
@Table(name = "USERS")
@SuppressWarnings("serial")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
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

	@OneToMany(mappedBy = "user", cascade = { CascadeType.PERSIST,
			CascadeType.REMOVE })
	private Set<UserRole> roles = Sets.newHashSet();

	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	private Set<FeedSubscription> subscriptions;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public byte[] getPassword() {
		return password;
	}

	public void setPassword(byte[] password) {
		this.password = password;
	}

	public byte[] getSalt() {
		return salt;
	}

	public void setSalt(byte[] salt) {
		this.salt = salt;
	}

	public Set<UserRole> getRoles() {
		return roles;
	}

	public void setRoles(Set<UserRole> roles) {
		this.roles = roles;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Date getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getRecoverPasswordToken() {
		return recoverPasswordToken;
	}

	public void setRecoverPasswordToken(String recoverPasswordToken) {
		this.recoverPasswordToken = recoverPasswordToken;
	}

	public Date getRecoverPasswordTokenDate() {
		return recoverPasswordTokenDate;
	}

	public void setRecoverPasswordTokenDate(Date recoverPasswordTokenDate) {
		this.recoverPasswordTokenDate = recoverPasswordTokenDate;
	}

	public Set<FeedSubscription> getSubscriptions() {
		return subscriptions;
	}

	public void setSubscriptions(Set<FeedSubscription> subscriptions) {
		this.subscriptions = subscriptions;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

}
