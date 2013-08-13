package com.commafeed.frontend.model.request;

import java.io.Serializable;

import lombok.Data;

import com.wordnik.swagger.annotations.ApiClass;
import com.wordnik.swagger.annotations.ApiProperty;

@SuppressWarnings("serial")
@ApiClass("Feed information request")
@Data
public class FeedInfoRequest implements Serializable {

	@ApiProperty(value = "feed url", required = true)
	private String url;

}
