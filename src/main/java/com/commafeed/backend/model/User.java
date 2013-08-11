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

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.collect.Sets;

@Entity
@Table(name = "USERS")
@SuppressWarnings("serial")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Data
@EqualsAndHashCode(callSuper = true)
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

	@OneToMany(mappedBy = "user", cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
	private Set<UserRole> roles = Sets.newHashSet();

	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	private Set<FeedSubscription> subscriptions;

}
