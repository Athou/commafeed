package com.commafeed.frontend.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@SuppressWarnings("serial")
@ApiModel(description = "List of entries with some metadata")
@Data
public class Entries implements Serializable {

	@ApiModelProperty(value = "name of the feed or the category requested", required = true)
	private String name;

	@ApiModelProperty(value = "error or warning message")
	private String message;

	@ApiModelProperty(value = "times the server tried to refresh the feed and failed", required = true)
	private int errorCount;

	@ApiModelProperty(value = "URL of the website, extracted from the feed", required = true)
	private String feedLink;

	@ApiModelProperty(value = "list generation timestamp", required = true)
	private long timestamp;

	@ApiModelProperty(value = "if the query has more elements", required = true)
	private boolean hasMore;

	@ApiModelProperty(value = "the requested offset")
	private int offset;

	@ApiModelProperty(value = "the requested limit")
	private int limit;

	@ApiModelProperty(value = "list of entries", required = true)
	private List<Entry> entries = new ArrayList<>();

	@ApiModelProperty(
			value = "if true, the unread flag was ignored in the request, all entries are returned regardless of their read status",
			required = true)
	private boolean ignoredReadStatus;

}
