package com.commafeed.frontend.model;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.commafeed.backend.model.Feed;
import com.google.common.collect.Lists;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class FeedCount implements Serializable {

	private static final long serialVersionUID = 1L;

	private String value;
	private List<Feed> feeds = Lists.newArrayList();;

	public FeedCount(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public List<Feed> getFeeds() {
		return feeds;
	}

	public void setFeeds(List<Feed> feeds) {
		this.feeds = feeds;
	}

}
