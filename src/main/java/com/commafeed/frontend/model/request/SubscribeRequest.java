package com.commafeed.frontend.model.request;

import java.io.Serializable;

import lombok.Data;

import com.wordnik.swagger.annotations.ApiClass;
import com.wordnik.swagger.annotations.ApiProperty;

@SuppressWarnings("serial")
@ApiClass("Subscription request")
@Data
public class SubscribeRequest implements Serializable {

	@ApiProperty(value = "url of the feed", required = true)
	private String url;

	@ApiProperty(value = "name of the feed for the user", required = true)
	private String title;

	@ApiProperty(value = "id of the user category to place the feed in")
	private String categoryId;

}
