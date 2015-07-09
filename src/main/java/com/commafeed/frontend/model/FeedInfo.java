package com.commafeed.frontend.model;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@SuppressWarnings("serial")
@ApiModel("Feed details")
@Data
public class FeedInfo implements Serializable {

	private String url;
	private String title;

}
