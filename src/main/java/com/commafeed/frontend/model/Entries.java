package com.commafeed.frontend.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@SuppressWarnings("serial")
@ApiModel("List of entries with some metadata")
@Data
public class Entries implements Serializable {

	@ApiModelProperty("name of the feed or the category requested")
	private String name;

	@ApiModelProperty("error or warning message")
	private String message;

	@ApiModelProperty("times the server tried to refresh the feed and failed")
	private int errorCount;

	@ApiModelProperty("URL of the website, extracted from the feed")
	private String feedLink;

	@ApiModelProperty("list generation timestamp")
	private long timestamp;

	@ApiModelProperty("if the query has more elements")
	private boolean hasMore;

	@ApiModelProperty("the requested offset")
	private int offset;

	@ApiModelProperty("the requested limit")
	private int limit;

	@ApiModelProperty("list of entries")
	private List<Entry> entries = new ArrayList<>();

	@ApiModelProperty("if true, the unread flag was ignored in the request, all entries are returned regardless of their read status")
	private boolean ignoredReadStatus;

}
