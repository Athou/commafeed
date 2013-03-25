package com.commafeed.frontend.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SubscriptionRequest implements Serializable {

	private String url;
	private String title;
	private String categoryId;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

}
