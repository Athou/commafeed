package com.commafeed.frontend.model;

import java.io.Serializable;
import java.util.List;

import com.google.common.collect.Lists;

@SuppressWarnings("serial")
public class Entries implements Serializable {
	private String name;
	private String message;
	private List<Entry> entries = Lists.newArrayList();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Entry> getEntries() {
		return entries;
	}

	public void setEntries(List<Entry> entries) {
		this.entries = entries;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
