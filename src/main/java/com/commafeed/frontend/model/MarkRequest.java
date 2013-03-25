package com.commafeed.frontend.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class MarkRequest implements Serializable {
	private String type;
	private String id;
	private boolean read;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

}
