package com.commafeed.backend.model;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

import com.google.common.collect.Sets;

@Entity
@Table(name = "USERS")
@SuppressWarnings("serial")
public class User extends AbstractModel {

	@Column(length = 32, nullable = false, unique = true)
	@Index(name = "username_index")
	private String name;

	@Column(length = 256, nullable = false)
	private byte[] password;

	@Column(length = 8, nullable = false)
	private byte[] salt;

	@OneToMany(mappedBy = "user", cascade = CascadeType.PERSIST)
	private Set<UserRole> roles = Sets.newHashSet();

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

}
