package com.commafeed.frontend.resource.googlereader;

import org.apache.commons.lang3.StringUtils;

public record GoogleReaderStreamId(Type type, String value) {

    public enum Type {
        ALL,
        FEED,
        LABEL,
        STARRED
    }

    public static final String READING_LIST = "user/-/state/com.google/reading-list";
    public static final String STARRED = "user/-/state/com.google/starred";
    public static final String READ = "user/-/state/com.google/read";
    public static final String LABEL_PREFIX = "user/-/label/";
    public static final String FEED_PREFIX = "feed/";

    public static final GoogleReaderStreamId ALL_STREAM = new GoogleReaderStreamId(Type.ALL, null);
    public static final GoogleReaderStreamId STARRED_STREAM =
            new GoogleReaderStreamId(Type.STARRED, null);

    public static GoogleReaderStreamId parse(String raw) {
        if (StringUtils.isBlank(raw) || "reading-list".equals(raw) || READING_LIST.equals(raw)) {
            return ALL_STREAM;
        }
        if (STARRED.equals(raw)) {
            return STARRED_STREAM;
        }
        if (raw.startsWith(LABEL_PREFIX)) {
            return new GoogleReaderStreamId(Type.LABEL, raw.substring(LABEL_PREFIX.length()));
        }
        if (raw.startsWith(FEED_PREFIX)) {
            return new GoogleReaderStreamId(Type.FEED, raw.substring(FEED_PREFIX.length()));
        }
        // unrecognized stream id, default to the whole reading list
        return ALL_STREAM;
    }

    public static String feed(long subscriptionId) {
        return FEED_PREFIX + subscriptionId;
    }

    public static String label(String categoryName) {
        return LABEL_PREFIX + categoryName;
    }

    /** extracts the subscription id out of a "feed/&lt;id&gt;" stream id, or null if invalid */
    public Long feedSubscriptionId() {
        if (type != Type.FEED) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
