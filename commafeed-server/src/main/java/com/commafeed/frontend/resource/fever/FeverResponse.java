package com.commafeed.frontend.resource.fever;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@JsonInclude(Include.NON_NULL)
@Data
public class FeverResponse {

	@JsonProperty("api_version")
	private int apiVersion = 3;

	@JsonProperty("auth")
	@JsonFormat(shape = Shape.NUMBER)
	private boolean auth;

	@JsonProperty("last_refreshed_on_time")
	private long lastRefreshedOnTime;

	@JsonProperty("groups")
	private List<FeverGroup> groups;

	@JsonProperty("feeds")
	private List<FeverFeed> feeds;

	@JsonProperty("feeds_groups")
	private List<FeverFeedGroup> feedsGroups;

	@JsonProperty("unread_item_ids")
	@JsonSerialize(using = LongListToCommaSeparatedStringSerializer.class)
	private List<Long> unreadItemIds;

	@JsonProperty("saved_item_ids")
	@JsonSerialize(using = LongListToCommaSeparatedStringSerializer.class)
	private List<Long> savedItemIds;

	@JsonProperty("items")
	private List<FeverItem> items;

	@JsonProperty("favicons")
	private List<FeverFavicon> favicons;

	@JsonProperty("links")
	private List<FeverLink> links;

	@Data
	public static class FeverGroup {

		@JsonProperty("id")
		private long id;

		@JsonProperty("title")
		private String title;
	}

	@Data
	public static class FeverFeed {

		@JsonProperty("id")
		private long id;

		@JsonProperty("favicon_id")
		private long faviconId;

		@JsonProperty("title")
		private String title;

		@JsonProperty("url")
		private String url;

		@JsonProperty("site_url")
		private String siteUrl;

		@JsonProperty("is_spark")
		@JsonFormat(shape = Shape.NUMBER)
		private boolean spark;

		@JsonProperty("last_updated_on_time")
		private long lastUpdatedOnTime;
	}

	@Data
	public static class FeverFeedGroup {

		@JsonProperty("group_id")
		private long groupId;

		@JsonProperty("feed_ids")
		@JsonSerialize(using = LongListToCommaSeparatedStringSerializer.class)
		private List<Long> feedIds;
	}

	@Data
	public static class FeverItem {

		@JsonProperty("id")
		private long id;

		@JsonProperty("feed_id")
		private long feedId;

		@JsonProperty("title")
		private String title;

		@JsonProperty("author")
		private String author;

		@JsonProperty("html")
		private String html;

		@JsonProperty("url")
		private String url;

		@JsonProperty("is_saved")
		@JsonFormat(shape = Shape.NUMBER)
		private boolean saved;

		@JsonProperty("is_read")
		@JsonFormat(shape = Shape.NUMBER)
		private boolean read;

		@JsonProperty("created_on_time")
		private long createdOnTime;

	}

	@Data
	public static class FeverFavicon {

		@JsonProperty("id")
		private long id;

		@JsonProperty("data")
		private String data;
	}

	@Data
	public static class FeverLink {

	}

	public static class LongListToCommaSeparatedStringSerializer extends JsonSerializer<List<Long>> {

		@Override
		public void serialize(List<Long> input, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
			String output = input.stream().map(String::valueOf).collect(Collectors.joining(","));
			jsonGenerator.writeObject(output);
		}

	}
}
