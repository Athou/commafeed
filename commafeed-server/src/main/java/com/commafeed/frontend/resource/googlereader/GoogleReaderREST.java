package com.commafeed.frontend.resource.googlereader;

import com.commafeed.backend.Digests;
import com.commafeed.backend.dao.FeedCategoryDAO;
import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.dao.FeedEntryStatusDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.feed.FeedUtils;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryContent;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserSettings.ReadingOrder;
import com.commafeed.backend.service.FeedEntryService;
import com.commafeed.backend.service.FeedSubscriptionService;
import com.commafeed.backend.service.UserService;
import com.commafeed.frontend.model.UnreadCount;
import com.commafeed.security.AuthenticationContext;
import com.commafeed.security.Roles;
import com.commafeed.security.mechanism.GoogleReaderAuthenticationMechanism;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;

import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Google Reader compatible API
 *
 * <ul>
 *   <li>url: /rest/googlereader
 *   <li>login: username
 *   <li>password: api key
 * </ul>
 */
@Path(GoogleReaderAuthenticationMechanism.PATH_PREFIX)
@RolesAllowed(Roles.USER)
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Singleton
public class GoogleReaderREST {

    private static final int DEFAULT_ITEM_COUNT = 20;
    private static final int MAX_ITEM_COUNT = 1000;
    // cap at which clients should display "count+" instead of the exact unread count
    private static final int MAX_UNREAD_COUNT = 10000;

    private final AuthenticationContext authenticationContext;
    private final UserService userService;
    private final FeedSubscriptionService feedSubscriptionService;
    private final FeedEntryService feedEntryService;
    private final FeedSubscriptionDAO feedSubscriptionDAO;
    private final FeedCategoryDAO feedCategoryDAO;
    private final FeedEntryDAO feedEntryDAO;
    private final FeedEntryStatusDAO feedEntryStatusDAO;

    @GET
    @PermitAll
    @Path("/")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(hidden = true)
    public Response welcome() {
        return Response.ok("This is the Google Reader API endpoint.").build();
    }

    @POST
    @PermitAll
    @Path("/accounts/ClientLogin")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    @Operation(hidden = true)
    public Response clientLogin(@Context UriInfo uri, MultivaluedMap<String, String> form) {
        Map<String, String> params = new HashMap<>();
        uri.getQueryParameters().forEach((k, v) -> params.put(k, v.getFirst()));
        form.forEach((k, v) -> params.put(k, v.getFirst()));

        String password = params.get("Passwd");
        User user = StringUtils.isBlank(password) ? null : userService.login(password).orElse(null);
        if (user == null) {
            return Response.status(Status.FORBIDDEN).entity("Error=BadAuthentication\n").build();
        }

        String token = user.getName() + "/" + user.getApiKey();
        String body = "SID=" + token + "\nLSID=" + token + "\nAuth=" + token + "\n";
        return Response.ok(body).build();
    }

    @GET
    @Path("/reader/api/0/token")
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    @Operation(hidden = true)
    public Response token() {
        return Response.ok(computeEditToken(currentUser())).build();
    }

    @GET
    @Path("/reader/api/0/user-info")
    @Transactional
    @Operation(hidden = true)
    public GoogleReaderModel.UserInfo userInfo() {
        User user = currentUser();
        GoogleReaderModel.UserInfo info = new GoogleReaderModel.UserInfo();
        info.setUserId(String.valueOf(user.getId()));
        info.setUserName(user.getName());
        info.setUserProfileId(String.valueOf(user.getId()));
        info.setUserEmail(user.getEmail());
        return info;
    }

    @GET
    @Path("/reader/api/0/subscription/list")
    @Transactional
    @Operation(hidden = true)
    public GoogleReaderModel.SubscriptionList subscriptionList() {
        User user = currentUser();
        GoogleReaderModel.SubscriptionList resp = new GoogleReaderModel.SubscriptionList();
        resp.setSubscriptions(
                feedSubscriptionDAO.findAll(user).stream().map(this::mapSubscription).toList());
        return resp;
    }

    @POST
    @Path("/reader/api/0/subscription/edit")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    @Operation(hidden = true)
    public Response subscriptionEdit(
            @FormParam("ac") String action,
            @FormParam("s") String streamId,
            @FormParam("t") String title,
            @FormParam("a") String addLabel,
            @FormParam("r") String removeLabel,
            @FormParam("T") String token) {
        User user = currentUser();
        if (!validToken(user, token)) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        if (StringUtils.isBlank(action) || StringUtils.isBlank(streamId)) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        if ("subscribe".equals(action)) {
            String url =
                    streamId.startsWith(GoogleReaderStreamId.FEED_PREFIX)
                            ? streamId.substring(GoogleReaderStreamId.FEED_PREFIX.length())
                            : streamId;
            FeedCategory category = resolveLabelCategory(user, addLabel, true);
            String subTitle = StringUtils.defaultIfBlank(title, url);
            feedSubscriptionService.subscribe(user, url, subTitle, category, 0);
        } else {
            GoogleReaderStreamId parsed = GoogleReaderStreamId.parse(streamId);
            Long subId = parsed.feedSubscriptionId();
            if (subId == null) {
                return Response.status(Status.BAD_REQUEST).build();
            }

            if ("unsubscribe".equals(action)) {
                feedSubscriptionService.unsubscribe(user, subId);
            } else if ("edit".equals(action)) {
                FeedSubscription sub = feedSubscriptionDAO.findById(user, subId);
                if (sub != null) {
                    if (StringUtils.isNotBlank(title)) {
                        sub.setTitle(title);
                    }
                    if (StringUtils.isNotBlank(addLabel)) {
                        sub.setCategory(resolveLabelCategory(user, addLabel, true));
                    }
                    if (StringUtils.isNotBlank(removeLabel)) {
                        sub.setCategory(null);
                    }
                }
            }
        }

        return Response.ok("OK").build();
    }

    @GET
    @Path("/reader/api/0/tag/list")
    @Transactional
    @Operation(hidden = true)
    public GoogleReaderModel.TagList tagList() {
        User user = currentUser();
        List<GoogleReaderModel.Tag> tags = new ArrayList<>();
        feedCategoryDAO
                .findAll(user)
                .forEach(
                        c ->
                                tags.add(
                                        new GoogleReaderModel.Tag(
                                                GoogleReaderStreamId.label(c.getName()))));
        tags.add(new GoogleReaderModel.Tag(GoogleReaderStreamId.STARRED));

        GoogleReaderModel.TagList resp = new GoogleReaderModel.TagList();
        resp.setTags(tags);
        return resp;
    }

    @GET
    @Path("/reader/api/0/unread-count")
    @Transactional
    @Operation(hidden = true)
    public GoogleReaderModel.UnreadCounts unreadCounts() {
        User user = currentUser();
        List<FeedSubscription> subs = feedSubscriptionDAO.findAll(user);
        Map<Long, UnreadCount> unreadCounts = feedSubscriptionService.getUnreadCount(user);

        List<GoogleReaderModel.UnreadCountEntry> entries = new ArrayList<>();
        Map<Long, Long> unreadByCategory = new HashMap<>();
        long totalUnread = 0;

        for (FeedSubscription sub : subs) {
            UnreadCount uc = unreadCounts.get(sub.getId());

            GoogleReaderModel.UnreadCountEntry entry = new GoogleReaderModel.UnreadCountEntry();
            entry.setId(GoogleReaderStreamId.feed(sub.getId()));
            entry.setCount(uc.getUnreadCount());
            entry.setNewestItemTimestampUsec(toUsec(uc.getNewestItemTime()));
            entries.add(entry);

            totalUnread += uc.getUnreadCount();

            long categoryId = sub.getCategory() == null ? 0 : sub.getCategory().getId();
            unreadByCategory.merge(categoryId, uc.getUnreadCount(), Long::sum);
        }

        Map<Long, FeedCategory> categoriesById =
                feedCategoryDAO.findAll(user).stream()
                        .collect(Collectors.toMap(FeedCategory::getId, c -> c));
        unreadByCategory.forEach(
                (categoryId, count) -> {
                    if (categoryId == 0) {
                        return;
                    }
                    FeedCategory category = categoriesById.get(categoryId);
                    if (category == null) {
                        return;
                    }
                    GoogleReaderModel.UnreadCountEntry entry =
                            new GoogleReaderModel.UnreadCountEntry();
                    entry.setId(GoogleReaderStreamId.label(category.getName()));
                    entry.setCount(count);
                    entries.add(entry);
                });

        GoogleReaderModel.UnreadCountEntry readingList = new GoogleReaderModel.UnreadCountEntry();
        readingList.setId(GoogleReaderStreamId.READING_LIST);
        readingList.setCount(totalUnread);
        entries.add(readingList);

        GoogleReaderModel.UnreadCounts resp = new GoogleReaderModel.UnreadCounts();
        resp.setMax(MAX_UNREAD_COUNT);
        resp.setUnreadCounts(entries);
        return resp;
    }

    @GET
    @Path("/reader/api/0/stream/contents/{streamId : .+}")
    @Transactional
    @Operation(hidden = true)
    public GoogleReaderModel.StreamContents streamContentsPath(
            @PathParam("streamId") String streamId,
            @QueryParam("n") Integer n,
            @QueryParam("c") String continuation,
            @QueryParam("r") String order,
            @QueryParam("xt") List<String> excludeTargets) {
        return buildStreamContents(streamId, n, continuation, order, excludeTargets);
    }

    @GET
    @Path("/reader/api/0/stream/contents")
    @Transactional
    @Operation(hidden = true)
    public GoogleReaderModel.StreamContents streamContentsQuery(
            @QueryParam("s") String streamId,
            @QueryParam("n") Integer n,
            @QueryParam("c") String continuation,
            @QueryParam("r") String order,
            @QueryParam("xt") List<String> excludeTargets) {
        return buildStreamContents(streamId, n, continuation, order, excludeTargets);
    }

    @GET
    @Path("/reader/api/0/stream/items/ids")
    @Transactional
    @Operation(hidden = true)
    public GoogleReaderModel.ItemRefs streamItemIds(
            @QueryParam("s") String streamId,
            @QueryParam("n") Integer n,
            @QueryParam("c") String continuation,
            @QueryParam("r") String order,
            @QueryParam("xt") List<String> excludeTargets) {
        User user = currentUser();
        GoogleReaderStreamId parsed = GoogleReaderStreamId.parse(streamId);
        int limit = clampLimit(n);
        ReadingOrder readingOrder = "o".equals(order) ? ReadingOrder.ASC : ReadingOrder.DESC;
        boolean unreadOnly = isUnreadOnly(excludeTargets);

        PagedStatuses paged =
                fetchStatuses(user, parsed, limit, continuation, readingOrder, unreadOnly);

        GoogleReaderModel.ItemRefs resp = new GoogleReaderModel.ItemRefs();
        resp.setItemRefs(
                paged.statuses().stream()
                        .map(
                                s ->
                                        new GoogleReaderModel.ItemRef(
                                                String.valueOf(s.getEntry().getId())))
                        .toList());
        resp.setContinuation(paged.continuation());
        return resp;
    }

    @POST
    @Path("/reader/api/0/stream/items/contents")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    @Operation(hidden = true)
    public GoogleReaderModel.StreamContents streamItemsContentsPost(
            @FormParam("i") List<String> ids) {
        return buildStreamItemsContents(ids);
    }

    @GET
    @Path("/reader/api/0/stream/items/contents")
    @Transactional
    @Operation(hidden = true)
    public GoogleReaderModel.StreamContents streamItemsContentsGet(
            @QueryParam("i") List<String> ids) {
        return buildStreamItemsContents(ids);
    }

    @POST
    @Path("/reader/api/0/edit-tag")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    @Operation(hidden = true)
    public Response editTag(
            @FormParam("i") List<String> ids,
            @FormParam("a") List<String> add,
            @FormParam("r") List<String> remove,
            @FormParam("T") String token) {
        User user = currentUser();
        if (!validToken(user, token)) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        if (ids == null) {
            return Response.ok("OK").build();
        }

        boolean markRead = containsTag(add, GoogleReaderStreamId.READ);
        boolean markUnread = containsTag(remove, GoogleReaderStreamId.READ);
        boolean star = containsTag(add, GoogleReaderStreamId.STARRED);
        boolean unstar = containsTag(remove, GoogleReaderStreamId.STARRED);

        for (String rawId : ids) {
            Long entryId = parseItemId(rawId);
            if (entryId == null) {
                continue;
            }
            FeedEntry entry = feedEntryDAO.findById(entryId);
            if (entry == null) {
                continue;
            }

            if (markRead) {
                feedEntryService.markEntry(user, entryId, true);
            }
            if (markUnread) {
                feedEntryService.markEntry(user, entryId, false);
            }
            if (star || unstar) {
                FeedSubscription sub = feedSubscriptionDAO.findByFeed(user, entry.getFeed());
                if (sub != null) {
                    if (star) {
                        feedEntryService.starEntry(user, entryId, sub.getId(), true);
                    }
                    if (unstar) {
                        feedEntryService.starEntry(user, entryId, sub.getId(), false);
                    }
                }
            }
        }

        return Response.ok("OK").build();
    }

    @POST
    @Path("/reader/api/0/mark-all-as-read")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    @Operation(hidden = true)
    public Response markAllAsRead(
            @FormParam("s") String streamId,
            @FormParam("ts") Long ts,
            @FormParam("T") String token) {
        User user = currentUser();
        if (!validToken(user, token)) {
            return Response.status(Status.UNAUTHORIZED).build();
        }

        GoogleReaderStreamId parsed = GoogleReaderStreamId.parse(streamId);
        Instant olderThan = (ts != null && ts > 0) ? Instant.ofEpochSecond(ts / 1_000_000) : null;

        switch (parsed.type()) {
            case STARRED -> feedEntryService.markStarredEntries(user, olderThan, null);
            case FEED -> {
                Long subId = parsed.feedSubscriptionId();
                FeedSubscription sub =
                        subId == null ? null : feedSubscriptionDAO.findById(user, subId);
                if (sub != null) {
                    feedEntryService.markSubscriptionEntries(
                            user, List.of(sub), olderThan, null, null);
                }
            }
            case LABEL -> {
                FeedCategory category = findCategoryByName(user, parsed.value());
                if (category != null) {
                    List<FeedCategory> categories =
                            feedCategoryDAO.findAllChildrenCategories(user, category);
                    List<FeedSubscription> subs =
                            feedSubscriptionDAO.findByCategories(user, categories);
                    feedEntryService.markSubscriptionEntries(user, subs, olderThan, null, null);
                }
            }
            default ->
                    feedEntryService.markSubscriptionEntries(
                            user, feedSubscriptionDAO.findAll(user), olderThan, null, null);
        }

        return Response.ok("OK").build();
    }

    private GoogleReaderModel.StreamContents buildStreamContents(
            String rawStreamId,
            Integer n,
            String continuation,
            String order,
            List<String> excludeTargets) {
        User user = currentUser();
        GoogleReaderStreamId parsed = GoogleReaderStreamId.parse(rawStreamId);
        int limit = clampLimit(n);
        ReadingOrder readingOrder = "o".equals(order) ? ReadingOrder.ASC : ReadingOrder.DESC;
        boolean unreadOnly = isUnreadOnly(excludeTargets);

        PagedStatuses paged =
                fetchStatuses(user, parsed, limit, continuation, readingOrder, unreadOnly);

        GoogleReaderModel.StreamContents resp = new GoogleReaderModel.StreamContents();
        resp.setId(streamIdString(parsed));
        resp.setTitle(streamTitle(user, parsed));
        resp.setUpdated(Instant.now().getEpochSecond());
        resp.setItems(paged.statuses().stream().map(this::mapItem).toList());
        resp.setContinuation(paged.continuation());
        return resp;
    }

    private GoogleReaderModel.StreamContents buildStreamItemsContents(List<String> ids) {
        User user = currentUser();
        List<FeedSubscription> subs = feedSubscriptionDAO.findAll(user);
        Map<Long, FeedSubscription> subsByFeedId =
                subs.stream()
                        .collect(Collectors.toMap(s -> s.getFeed().getId(), s -> s, (a, b) -> a));

        List<GoogleReaderModel.StreamItem> items = new ArrayList<>();
        if (ids != null) {
            for (String rawId : ids) {
                Long entryId = parseItemId(rawId);
                if (entryId == null) {
                    continue;
                }
                FeedEntry entry = feedEntryDAO.findById(entryId);
                if (entry == null) {
                    continue;
                }
                FeedSubscription sub = subsByFeedId.get(entry.getFeed().getId());
                if (sub == null) {
                    continue;
                }
                FeedEntryStatus status = feedEntryStatusDAO.getStatus(user, sub, entry);
                items.add(mapItem(status));
            }
        }

        GoogleReaderModel.StreamContents resp = new GoogleReaderModel.StreamContents();
        resp.setId(GoogleReaderStreamId.READING_LIST);
        resp.setUpdated(Instant.now().getEpochSecond());
        resp.setItems(items);
        return resp;
    }

    private PagedStatuses fetchStatuses(
            User user,
            GoogleReaderStreamId streamId,
            int limit,
            String continuation,
            ReadingOrder order,
            boolean unreadOnly) {
        Long minEntryId = null;
        Long maxEntryId = null;
        int offset = 0;

        if (streamId.type() == GoogleReaderStreamId.Type.STARRED) {
            offset = parseOffset(continuation);
        } else if (StringUtils.isNotBlank(continuation)) {
            try {
                long cursor = Long.parseLong(continuation);
                if (order == ReadingOrder.ASC) {
                    minEntryId = cursor;
                } else {
                    maxEntryId = cursor;
                }
            } catch (NumberFormatException ignored) {
                // invalid continuation token, ignore and start from the beginning
            }
        }

        List<FeedEntryStatus> statuses;
        int fetchedCount;
        switch (streamId.type()) {
            case STARRED -> {
                List<FeedEntryStatus> starred =
                        feedEntryStatusDAO.findStarred(
                                user, null, null, offset, limit, order, true);
                fetchedCount = starred.size();
                statuses =
                        unreadOnly ? starred.stream().filter(s -> !s.isRead()).toList() : starred;
            }
            case FEED -> {
                Long subId = streamId.feedSubscriptionId();
                FeedSubscription sub =
                        subId == null ? null : feedSubscriptionDAO.findById(user, subId);
                List<FeedSubscription> subs = sub == null ? List.of() : List.of(sub);
                statuses =
                        feedEntryStatusDAO.findBySubscriptions(
                                user,
                                subs,
                                unreadOnly,
                                null,
                                null,
                                0,
                                limit,
                                order,
                                true,
                                null,
                                minEntryId,
                                maxEntryId);
                fetchedCount = statuses.size();
            }
            case LABEL -> {
                FeedCategory category = findCategoryByName(user, streamId.value());
                List<FeedSubscription> subs =
                        category == null
                                ? List.of()
                                : feedSubscriptionDAO.findByCategories(
                                        user,
                                        feedCategoryDAO.findAllChildrenCategories(user, category));
                statuses =
                        feedEntryStatusDAO.findBySubscriptions(
                                user,
                                subs,
                                unreadOnly,
                                null,
                                null,
                                0,
                                limit,
                                order,
                                true,
                                null,
                                minEntryId,
                                maxEntryId);
                fetchedCount = statuses.size();
            }
            default -> {
                List<FeedSubscription> subs = feedSubscriptionDAO.findAll(user);
                statuses =
                        feedEntryStatusDAO.findBySubscriptions(
                                user,
                                subs,
                                unreadOnly,
                                null,
                                null,
                                0,
                                limit,
                                order,
                                true,
                                null,
                                minEntryId,
                                maxEntryId);
                fetchedCount = statuses.size();
            }
        }

        String nextContinuation = null;
        if (fetchedCount >= limit && fetchedCount > 0) {
            if (streamId.type() == GoogleReaderStreamId.Type.STARRED) {
                nextContinuation = String.valueOf(offset + limit);
            } else if (!statuses.isEmpty()) {
                nextContinuation = String.valueOf(statuses.getLast().getEntry().getId());
            }
        }

        return new PagedStatuses(statuses, nextContinuation);
    }

    private GoogleReaderModel.StreamItem mapItem(FeedEntryStatus status) {
        FeedEntry entry = status.getEntry();
        FeedSubscription sub = status.getSubscription();

        GoogleReaderModel.StreamItem item = new GoogleReaderModel.StreamItem();
        item.setId(formatItemId(entry.getId()));

        Instant published = entry.getPublished() != null ? entry.getPublished() : Instant.EPOCH;
        item.setPublished(published.getEpochSecond());
        item.setUpdated(published.getEpochSecond());
        item.setCrawlTimeMsec(String.valueOf(entry.getInserted().toEpochMilli()));
        item.setTimestampUsec(String.valueOf(published.toEpochMilli() * 1000));
        item.setTitle(entry.getContent().getTitle());
        item.setAuthor(entry.getContent().getAuthor());

        if (StringUtils.isNotBlank(entry.getUrl())) {
            item.setCanonical(List.of(new GoogleReaderModel.HrefEntry(entry.getUrl(), null)));
            item.setAlternate(
                    List.of(new GoogleReaderModel.HrefEntry(entry.getUrl(), "text/html")));
        }
        item.setSummary(
                new GoogleReaderModel.Content(
                        Optional.ofNullable(entry.getContent().getContent()).orElse(""),
                        entry.getContent().getDirection() == FeedEntryContent.Direction.RTL
                                ? "rtl"
                                : "ltr"));

        List<String> categories = new ArrayList<>();
        categories.add(GoogleReaderStreamId.READING_LIST);
        if (status.isRead()) {
            categories.add(GoogleReaderStreamId.READ);
        }
        if (status.isStarred()) {
            categories.add(GoogleReaderStreamId.STARRED);
        }
        if (sub != null && sub.getCategory() != null) {
            categories.add(GoogleReaderStreamId.label(sub.getCategory().getName()));
        }
        item.setCategories(categories);

        if (sub != null) {
            GoogleReaderModel.Origin origin = new GoogleReaderModel.Origin();
            origin.setStreamId(GoogleReaderStreamId.feed(sub.getId()));
            origin.setTitle(sub.getTitle());
            origin.setHtmlUrl(sub.getFeed().getLink());
            item.setOrigin(origin);
        }

        return item;
    }

    private GoogleReaderModel.Subscription mapSubscription(FeedSubscription sub) {
        GoogleReaderModel.Subscription s = new GoogleReaderModel.Subscription();
        s.setId(GoogleReaderStreamId.feed(sub.getId()));
        s.setTitle(sub.getTitle());
        s.setUrl(sub.getFeed().getUrl());
        s.setHtmlUrl(sub.getFeed().getLink());
        s.setIconUrl(sub.getFeed().getIconUrl());
        if (sub.getCategory() != null) {
            s.getCategories()
                    .add(
                            new GoogleReaderModel.Category(
                                    GoogleReaderStreamId.label(sub.getCategory().getName()),
                                    sub.getCategory().getName()));
        }
        return s;
    }

    private FeedCategory resolveLabelCategory(
            User user, String labelParam, boolean createIfMissing) {
        if (StringUtils.isBlank(labelParam)) {
            return null;
        }
        GoogleReaderStreamId parsed = GoogleReaderStreamId.parse(labelParam);
        if (parsed.type() != GoogleReaderStreamId.Type.LABEL) {
            return null;
        }

        String name = parsed.value();
        FeedCategory category = findCategoryByName(user, name);
        if (category == null && createIfMissing) {
            category = new FeedCategory();
            category.setUser(user);
            category.setName(FeedUtils.truncate(name, 128));
            feedCategoryDAO.persist(category);
        }
        return category;
    }

    private FeedCategory findCategoryByName(User user, String name) {
        return feedCategoryDAO.findAll(user).stream()
                .filter(c -> c.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    private String streamIdString(GoogleReaderStreamId parsed) {
        return switch (parsed.type()) {
            case FEED -> GoogleReaderStreamId.FEED_PREFIX + parsed.value();
            case LABEL -> GoogleReaderStreamId.label(parsed.value());
            case STARRED -> GoogleReaderStreamId.STARRED;
            default -> GoogleReaderStreamId.READING_LIST;
        };
    }

    private String streamTitle(User user, GoogleReaderStreamId parsed) {
        return switch (parsed.type()) {
            case FEED -> {
                Long subId = parsed.feedSubscriptionId();
                FeedSubscription sub =
                        subId == null ? null : feedSubscriptionDAO.findById(user, subId);
                yield sub == null ? null : sub.getTitle();
            }
            case LABEL -> parsed.value();
            case STARRED -> "Starred items";
            default -> "reading list";
        };
    }

    private boolean isUnreadOnly(List<String> excludeTargets) {
        return excludeTargets != null
                && excludeTargets.stream().anyMatch(t -> t.endsWith("/state/com.google/read"));
    }

    private boolean containsTag(List<String> tags, String suffix) {
        return tags != null && tags.stream().anyMatch(t -> t.endsWith(suffix));
    }

    private String formatItemId(long id) {
        // Google Reader's canonical long-form item id, e.g.
        // "tag:google.com,2005:reader/item/00000000148b9369", where the suffix is the item's
        // numeric id, represented as a zero-padded, 16-digit hex string
        return "tag:google.com,2005:reader/item/" + String.format("%016x", id);
    }

    private Long parseItemId(String raw) {
        if (StringUtils.isBlank(raw)) {
            return null;
        }
        int idx = raw.lastIndexOf('/');
        if (raw.startsWith("tag:google.com") && idx != -1) {
            try {
                return Long.parseUnsignedLong(raw.substring(idx + 1), 16);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        try {
            return Long.valueOf(raw);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int parseOffset(String continuation) {
        if (StringUtils.isBlank(continuation)) {
            return 0;
        }
        try {
            return Integer.parseInt(continuation);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int clampLimit(Integer n) {
        if (n == null || n <= 0) {
            return DEFAULT_ITEM_COUNT;
        }
        return Math.min(n, MAX_ITEM_COUNT);
    }

    private String toUsec(Instant instant) {
        return instant == null ? "0" : String.valueOf(instant.toEpochMilli() * 1000);
    }

    private boolean validToken(User user, String token) {
        // the edit token is a CSRF protection inherited from Google Reader's cookie-based
        // authentication model. Since this API is authenticated via an api key header instead of
        // cookies, that risk does not apply here. Some real-world clients (e.g. RSSGuard, unless
        // configured as "Reedah"/"Miniflux") never call the "token" endpoint and always submit a
        // blank token, so, blank tokens are accepted.
        return StringUtils.isBlank(token) || computeEditToken(user).equals(token);
    }

    private String computeEditToken(User user) {
        return Digests.md5Hex(user.getApiKey() + ":" + user.getId());
    }

    private User currentUser() {
        return authenticationContext.getCurrentUser();
    }

    private record PagedStatuses(List<FeedEntryStatus> statuses, String continuation) {}
}
