package com.commafeed.frontend.resource.fever;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import com.codahale.metrics.annotation.Timed;
import com.commafeed.backend.dao.FeedCategoryDAO;
import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.dao.FeedEntryStatusDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.favicon.AbstractFaviconFetcher.Favicon;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserSettings.ReadingOrder;
import com.commafeed.backend.service.FeedEntryService;
import com.commafeed.backend.service.FeedService;
import com.commafeed.backend.service.UserService;
import com.commafeed.frontend.resource.fever.FeverResponse.FeverFavicon;
import com.commafeed.frontend.resource.fever.FeverResponse.FeverFeed;
import com.commafeed.frontend.resource.fever.FeverResponse.FeverFeedGroup;
import com.commafeed.frontend.resource.fever.FeverResponse.FeverGroup;
import com.commafeed.frontend.resource.fever.FeverResponse.FeverItem;

import io.dropwizard.hibernate.UnitOfWork;
import lombok.RequiredArgsConstructor;

/**
 * Fever-compatible API
 * 
 * <ul>
 * <li>url: /rest/fever/user/${userId}</li>
 * <li>login: username</li>
 * <li>password: api key</li>
 * </ul>
 * 
 * See https://feedafever.com/api
 */
@Path("/fever")
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
@Singleton
public class FeverREST {

	private static final String PATH = "/user/{userId}{optionalTrailingFever : (/fever)?}{optionalTrailingSlash : (/)?}";
	private static final int UNREAD_ITEM_IDS_BATCH_SIZE = 1000;
	private static final int SAVED_ITEM_IDS_BATCH_SIZE = 1000;
	private static final int ITEMS_BATCH_SIZE = 200;

	private final UserService userService;
	private final FeedEntryService feedEntryService;
	private final FeedService feedService;
	private final FeedEntryDAO feedEntryDAO;
	private final FeedSubscriptionDAO feedSubscriptionDAO;
	private final FeedCategoryDAO feedCategoryDAO;
	private final FeedEntryStatusDAO feedEntryStatusDAO;

	// expected Fever API
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path(PATH)
	@POST
	@UnitOfWork
	@Timed
	public FeverResponse formUrlencoded(@Context UriInfo uri, @PathParam("userId") Long userId, MultivaluedMap<String, String> form) {
		Map<String, String> params = new HashMap<>();
		uri.getQueryParameters().forEach((k, v) -> params.put(k, v.get(0)));
		form.forEach((k, v) -> params.put(k, v.get(0)));
		return handle(userId, params);
	}

	// workaround for some readers that post data without any media type, and all params in the url
	// e.g. FeedMe
	@Path(PATH)
	@POST
	@UnitOfWork
	@Timed
	public FeverResponse noForm(@Context UriInfo uri, @PathParam("userId") Long userId) {
		Map<String, String> params = new HashMap<>();
		uri.getQueryParameters().forEach((k, v) -> params.put(k, v.get(0)));
		return handle(userId, params);
	}

	// workaround for some readers that post data using MultiPart FormData instead of the classic POST
	// e.g. Raven Reader
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Path(PATH)
	@POST
	@UnitOfWork
	@Timed
	public FeverResponse formData(@Context UriInfo uri, @PathParam("userId") Long userId, FormDataMultiPart form) {
		Map<String, String> params = new HashMap<>();
		uri.getQueryParameters().forEach((k, v) -> params.put(k, v.get(0)));
		form.getFields().forEach((k, v) -> params.put(k, v.get(0).getValue()));
		return handle(userId, params);
	}

	public FeverResponse handle(long userId, Map<String, String> params) {
		User user = auth(userId, params.get("api_key")).orElse(null);
		if (user == null) {
			FeverResponse resp = new FeverResponse();
			resp.setAuth(false);
			return resp;
		}

		FeverResponse resp = new FeverResponse();
		resp.setAuth(true);

		List<FeedSubscription> subscriptions = feedSubscriptionDAO.findAll(user);
		resp.setLastRefreshedOnTime(buildLastRefreshedOnTime(subscriptions));

		if (params.containsKey("groups") || params.containsKey("feeds")) {
			resp.setFeedsGroups(buildFeedsGroups(subscriptions));

			if (params.containsKey("groups")) {
				List<FeedCategory> categories = feedCategoryDAO.findAll(user);
				resp.setGroups(buildGroups(categories));
			}

			if (params.containsKey("feeds")) {
				resp.setFeeds(buildFeeds(subscriptions));
			}
		}

		if (params.containsKey("unread_item_ids")) {
			resp.setUnreadItemIds(buildUnreadItemIds(user, subscriptions));
		}

		if (params.containsKey("saved_item_ids")) {
			resp.setSavedItemIds(buildSavedItemIds(user));
		}

		if (params.containsKey("items")) {
			if (params.containsKey("with_ids")) {
				String withIds = params.get("with_ids");
				List<String> entryIds = Stream.of(withIds.split(",")).map(String::trim).collect(Collectors.toList());
				resp.setItems(buildItems(user, subscriptions, entryIds));
			} else {
				Long sinceId = params.containsKey("since_id") ? Long.valueOf(params.get("since_id")) : null;
				Long maxId = params.containsKey("max_id") ? Long.valueOf(params.get("max_id")) : null;
				resp.setItems(buildItems(user, subscriptions, sinceId, maxId));
			}
		}

		if (params.containsKey("favicons")) {
			resp.setFavicons(buildFavicons(subscriptions));
		}

		if (params.containsKey("links")) {
			resp.setLinks(Collections.emptyList());
		}

		if (params.containsKey("mark") && params.containsKey("id") && params.containsKey("as")) {
			long id = Long.parseLong(params.get("id"));
			String before = params.get("before");
			Date olderThan = before == null ? null : Date.from(Instant.ofEpochSecond(Long.parseLong(before)));
			mark(user, params.get("mark"), id, params.get("as"), olderThan);
		}

		return resp;
	}

	private Optional<User> auth(Long userId, String feverApiKey) {
		return userService.login(userId, feverApiKey);
	}

	private long buildLastRefreshedOnTime(List<FeedSubscription> subscriptions) {
		return subscriptions.stream()
				.map(FeedSubscription::getFeed)
				.map(Feed::getLastUpdated)
				.filter(Objects::nonNull)
				.max(Comparator.naturalOrder())
				.map(d -> d.toInstant().getEpochSecond())
				.orElse(0L);
	}

	private List<FeverFeedGroup> buildFeedsGroups(List<FeedSubscription> subscriptions) {
		return subscriptions.stream()
				.collect(Collectors.groupingBy(s -> s.getCategory() == null ? 0 : s.getCategory().getId()))
				.entrySet()
				.stream()
				.map(e -> {
					FeverFeedGroup fg = new FeverFeedGroup();
					fg.setGroupId(e.getKey());
					fg.setFeedIds(e.getValue().stream().map(FeedSubscription::getId).collect(Collectors.toList()));
					return fg;
				})
				.collect(Collectors.toList());
	}

	private List<FeverGroup> buildGroups(List<FeedCategory> categories) {
		return categories.stream().map(c -> {
			FeverGroup g = new FeverGroup();
			g.setId(c.getId());
			g.setTitle(c.getName());
			return g;
		}).collect(Collectors.toList());
	}

	private List<FeverFeed> buildFeeds(List<FeedSubscription> subscriptions) {
		return subscriptions.stream().map(s -> {
			FeverFeed f = new FeverFeed();
			f.setId(s.getId());
			f.setFaviconId(s.getId());
			f.setTitle(s.getTitle());
			f.setUrl(s.getFeed().getUrl());
			f.setSiteUrl(s.getFeed().getLink());
			f.setSpark(false);
			f.setLastUpdatedOnTime(s.getFeed().getLastUpdated() == null ? 0 : s.getFeed().getLastUpdated().toInstant().getEpochSecond());
			return f;
		}).collect(Collectors.toList());
	}

	private List<Long> buildUnreadItemIds(User user, List<FeedSubscription> subscriptions) {
		List<FeedEntryStatus> statuses = feedEntryStatusDAO.findBySubscriptions(user, subscriptions, true, null, null, 0,
				UNREAD_ITEM_IDS_BATCH_SIZE, ReadingOrder.desc, false, true, null, null, null);
		return statuses.stream().map(s -> s.getEntry().getId()).collect(Collectors.toList());
	}

	private List<Long> buildSavedItemIds(User user) {
		List<FeedEntryStatus> statuses = feedEntryStatusDAO.findStarred(user, null, 0, SAVED_ITEM_IDS_BATCH_SIZE, ReadingOrder.desc, false);
		return statuses.stream().map(s -> s.getEntry().getId()).collect(Collectors.toList());
	}

	private List<FeverItem> buildItems(User user, List<FeedSubscription> subscriptions, List<String> entryIds) {
		List<FeverItem> items = new ArrayList<>();

		Map<Long, FeedSubscription> subscriptionsByFeedId = subscriptions.stream()
				.collect(Collectors.toMap(s -> s.getFeed().getId(), s -> s));
		for (String entryId : entryIds) {
			FeedEntry entry = feedEntryDAO.findById(Long.parseLong(entryId));
			FeedSubscription sub = subscriptionsByFeedId.get(entry.getFeed().getId());
			if (sub != null) {
				FeedEntryStatus status = feedEntryStatusDAO.getStatus(user, sub, entry);
				items.add(mapStatus(status));
			}
		}

		return items;
	}

	private List<FeverItem> buildItems(User user, List<FeedSubscription> subscriptions, Long sinceId, Long maxId) {
		List<FeedEntryStatus> statuses = feedEntryStatusDAO.findBySubscriptions(user, subscriptions, false, null, null, 0, ITEMS_BATCH_SIZE,
				ReadingOrder.desc, false, false, null, sinceId, maxId);
		return statuses.stream().map(this::mapStatus).collect(Collectors.toList());
	}

	private FeverItem mapStatus(FeedEntryStatus s) {
		FeverItem i = new FeverItem();
		i.setId(s.getEntry().getId());
		i.setFeedId(s.getSubscription().getId());
		i.setTitle(s.getEntry().getContent().getTitle());
		i.setAuthor(s.getEntry().getContent().getAuthor());
		i.setHtml(Optional.ofNullable(s.getEntry().getContent().getContent()).orElse(""));
		i.setUrl(s.getEntry().getUrl());
		i.setSaved(s.isStarred());
		i.setRead(s.isRead());
		i.setCreatedOnTime(s.getEntryUpdated().toInstant().getEpochSecond());
		return i;
	}

	private List<FeverFavicon> buildFavicons(List<FeedSubscription> subscriptions) {
		return subscriptions.stream().map(s -> {
			Favicon favicon = feedService.fetchFavicon(s.getFeed());

			FeverFavicon f = new FeverFavicon();
			f.setId(s.getFeed().getId());
			f.setData(String.format("data:%s;base64,%s", favicon.getMediaType(), Base64.getEncoder().encodeToString(favicon.getIcon())));
			return f;
		}).collect(Collectors.toList());
	}

	private void mark(User user, String source, long id, String action, Date olderThan) {
		if ("item".equals(source)) {
			if ("read".equals(action) || "unread".equals(action)) {
				feedEntryService.markEntry(user, id, "read".equals(action));
			} else if ("saved".equals(action) || "unsaved".equals(action)) {
				FeedEntry entry = feedEntryDAO.findById(id);
				FeedSubscription sub = feedSubscriptionDAO.findByFeed(user, entry.getFeed());
				feedEntryService.starEntry(user, id, sub.getId(), "saved".equals(action));
			}
		} else if ("feed".equals(source)) {
			FeedSubscription subscription = feedSubscriptionDAO.findById(user, id);
			feedEntryService.markSubscriptionEntries(user, Collections.singletonList(subscription), olderThan, null);
		} else if ("group".equals(source)) {
			FeedCategory parent = feedCategoryDAO.findById(user, id);
			List<FeedCategory> categories = feedCategoryDAO.findAllChildrenCategories(user, parent);
			List<FeedSubscription> subscriptions = feedSubscriptionDAO.findByCategories(user, categories);
			feedEntryService.markSubscriptionEntries(user, subscriptions, olderThan, null);
		}
	}

}
