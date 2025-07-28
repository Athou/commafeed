package com.commafeed.frontend.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@SuppressWarnings("serial")
@Schema(description = "List of entries with some metadata")
@Data
@RegisterForReflection
public class Entries implements Serializable {

	@Schema(description = "name of the feed or the category requested", required = true)
	private String name;

	@Schema(description = "error or warning message")
	private String message;

	@Schema(description = "times the server tried to refresh the feed and failed", required = true)
	private int errorCount;

	@Schema(description = "URL of the website, extracted from the feed, only filled if querying for feed entries, not category entries")
	private String feedLink;

	@Schema(description = "list generation timestamp", required = true)
	private long timestamp;

	@Schema(description = "if the query has more elements", required = true)
	private boolean hasMore;

	@Schema(description = "the requested offset")
	private int offset;

	@Schema(description = "the requested limit")
	private int limit;

	@Schema(description = "list of entries", required = true)
	private List<Entry> entries = new ArrayList<>();

	@Schema(
			description = "if true, the unread flag was ignored in the request, all entries are returned regardless of their read status",
			required = true)
	private boolean ignoredReadStatus;

}
