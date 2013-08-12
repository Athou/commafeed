package com.commafeed.frontend.model;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

import com.commafeed.backend.model.Feed;
import com.google.common.collect.Lists;
import com.wordnik.swagger.annotations.ApiClass;

@ApiClass("Feed count")
@Data
public class FeedCount implements Serializable {

	private static final long serialVersionUID = 1L;

	private String value;
	private List<Feed> feeds = Lists.newArrayList();;

	public FeedCount(String value) {
		this.value = value;
	}

}
