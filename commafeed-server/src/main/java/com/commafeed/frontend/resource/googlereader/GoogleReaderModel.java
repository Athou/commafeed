package com.commafeed.frontend.resource.googlereader;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(Include.NON_NULL)
public class GoogleReaderModel {

    private GoogleReaderModel() {}

    @Data
    public static class UserInfo {

        @JsonProperty("userId")
        private String userId;

        @JsonProperty("userName")
        private String userName;

        @JsonProperty("userProfileId")
        private String userProfileId;

        @JsonProperty("userEmail")
        private String userEmail;
    }

    @Data
    public static class SubscriptionList {

        @JsonProperty("subscriptions")
        private List<Subscription> subscriptions = new ArrayList<>();
    }

    @Data
    public static class Subscription {

        @JsonProperty("id")
        private String id;

        @JsonProperty("title")
        private String title;

        @JsonProperty("categories")
        private List<Category> categories = new ArrayList<>();

        @JsonProperty("url")
        private String url;

        @JsonProperty("htmlUrl")
        private String htmlUrl;

        @JsonProperty("iconUrl")
        private String iconUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Category {

        @JsonProperty("id")
        private String id;

        @JsonProperty("label")
        private String label;
    }

    @Data
    public static class TagList {

        @JsonProperty("tags")
        private List<Tag> tags = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Tag {

        @JsonProperty("id")
        private String id;
    }

    @Data
    public static class StreamContents {

        @JsonProperty("id")
        private String id;

        @JsonProperty("title")
        private String title;

        @JsonProperty("updated")
        private long updated;

        @JsonProperty("items")
        private List<StreamItem> items = new ArrayList<>();

        @JsonProperty("continuation")
        private String continuation;
    }

    @Data
    public static class StreamItem {

        @JsonProperty("id")
        private String id;

        @JsonProperty("crawlTimeMsec")
        private String crawlTimeMsec;

        @JsonProperty("timestampUsec")
        private String timestampUsec;

        @JsonProperty("published")
        private long published;

        @JsonProperty("updated")
        private long updated;

        @JsonProperty("title")
        private String title;

        @JsonProperty("author")
        private String author;

        @JsonProperty("categories")
        private List<String> categories = new ArrayList<>();

        @JsonProperty("canonical")
        private List<HrefEntry> canonical = new ArrayList<>();

        @JsonProperty("alternate")
        private List<HrefEntry> alternate = new ArrayList<>();

        @JsonProperty("summary")
        private Content summary;

        @JsonProperty("origin")
        private Origin origin;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HrefEntry {

        @JsonProperty("href")
        private String href;

        @JsonProperty("type")
        private String type;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Content {

        @JsonProperty("content")
        private String content;

        @JsonProperty("direction")
        private String direction;
    }

    @Data
    public static class Origin {

        @JsonProperty("streamId")
        private String streamId;

        @JsonProperty("title")
        private String title;

        @JsonProperty("htmlUrl")
        private String htmlUrl;
    }

    @Data
    public static class ItemRefs {

        @JsonProperty("itemRefs")
        private List<ItemRef> itemRefs = new ArrayList<>();

        @JsonProperty("continuation")
        private String continuation;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemRef {

        @JsonProperty("id")
        private String id;
    }

    @Data
    public static class UnreadCounts {

        @JsonProperty("max")
        private long max;

        @JsonProperty("unreadcounts")
        private List<UnreadCountEntry> unreadCounts = new ArrayList<>();
    }

    @Data
    public static class UnreadCountEntry {

        @JsonProperty("id")
        private String id;

        @JsonProperty("count")
        private long count;

        @JsonProperty("newestItemTimestampUsec")
        private String newestItemTimestampUsec;
    }
}
