package com.commafeed.frontend.model;

import java.io.Serializable;

public class Subscription implements Serializable {

	private Long id;
	private String name;
	private int unread;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getUnread() {
		return unread;
	}

	public void setUnread(int unread) {
		this.unread = unread;
	}

}