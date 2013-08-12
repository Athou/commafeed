package com.commafeed.frontend.model;

import java.io.Serializable;

import lombok.Data;

import com.wordnik.swagger.annotations.ApiClass;

@SuppressWarnings("serial")
@ApiClass("Feed details")
@Data
public class FeedInfo implements Serializable {

	private String url;
	private String title;

}
