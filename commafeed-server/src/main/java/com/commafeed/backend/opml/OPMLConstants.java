package com.commafeed.backend.opml;

import lombok.experimental.UtilityClass;

import org.jdom2.Namespace;

import java.util.List;

/** CommaFeed-specific subscription settings, exported as namespaced outline attributes ignored by other readers */
@UtilityClass
public class OPMLConstants {

    public static final Namespace NAMESPACE = Namespace.getNamespace("commafeed", "https://www.commafeed.com/opml");

    public static final String FILTER = "filter";
    public static final String AUTO_MARK_AS_READ_AFTER_DAYS = "autoMarkAsReadAfterDays";
    public static final String PUSH_NOTIFICATIONS_ENABLED = "pushNotificationsEnabled";

    public static final List<String> COMMAFEED_ATTRIBUTES =
            List.of(FILTER, AUTO_MARK_AS_READ_AFTER_DAYS, PUSH_NOTIFICATIONS_ENABLED);
}
