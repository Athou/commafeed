package com.commafeed.frontend.rest.resources;

import java.util.List;
import java.util.Map;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.extended.FeedEntryWithStatus;
import com.commafeed.frontend.model.Entries;
import com.commafeed.frontend.model.Entry;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Path("entries")
public class EntriesREST extends AbstractREST {

	private static final String ALL = "all";

	public enum Type {
		category, feed, entry;
	}

	public enum ReadType {
		all, unread;
	}

	@Path("get")
	@GET
	public Entries getEntries(@QueryParam("type") Type type,
			@QueryParam("id") String id,
			@QueryParam("readType") ReadType readType,
			@DefaultValue("0") @QueryParam("offset") int offset,
			@DefaultValue("-1") @QueryParam("limit") int limit) {

		Preconditions.checkNotNull(type);
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(readType);

		Entries entries = new Entries();
		boolean unreadOnly = readType == ReadType.unread;

		if (type == Type.feed) {
			FeedSubscription subscription = feedSubscriptionService.findById(
					getUser(), Long.valueOf(id));
			entries.setName(subscription.getTitle());
			entries.getEntries().addAll(
					buildEntries(subscription, offset, limit, unreadOnly));

		} else {
			FeedCategory feedCategory = null;
			if (!ALL.equals(id)) {
				feedCategory = new FeedCategory();
				feedCategory.setId(Long.valueOf(id));
			}
			List<FeedCategory> childrenCategories = feedCategoryService
					.findAllChildrenCategories(getUser(), feedCategory);

			Map<Long, FeedSubscription> subMapping = Maps.uniqueIndex(
					feedSubscriptionService.findAll(getUser()),
					new Function<FeedSubscription, Long>() {
						public Long apply(FeedSubscription sub) {
							return sub.getFeed().getId();
						}
					});

			entries.setName(ALL.equals(id) ? ALL : feedCategory.getName());
			entries.getEntries().addAll(
					buildEntries(childrenCategories, subMapping, offset, limit,
							unreadOnly));
		}

		return entries;
	}

	private List<Entry> buildEntries(FeedSubscription subscription, int offset,
			int limit, boolean unreadOnly) {
		List<Entry> entries = Lists.newArrayList();

		List<FeedEntryWithStatus> unreadEntries = feedEntryService.getEntries(
				subscription.getFeed(), getUser(), unreadOnly, offset, limit);
		for (FeedEntryWithStatus feedEntry : unreadEntries) {
			entries.add(populateEntry(buildEntry(feedEntry), subscription));
		}

		return entries;
	}

	private List<Entry> buildEntries(List<FeedCategory> categories,
			Map<Long, FeedSubscription> subMapping, int offset, int limit,
			boolean unreadOnly) {
		List<Entry> entries = Lists.newArrayList();

		List<FeedEntryWithStatus> unreadEntries = feedEntryService.getEntries(
				categories, getUser(), unreadOnly, offset, limit);
		for (FeedEntryWithStatus feedEntry : unreadEntries) {
			entries.add(populateEntry(buildEntry(feedEntry),
					subMapping.get(feedEntry.getEntry().getFeed().getId())));
		}

		return entries;
	}

	private Entry buildEntry(FeedEntryWithStatus feedEntryWithStatus) {
		Entry entry = new Entry();

		FeedEntry feedEntry = feedEntryWithStatus.getEntry();
		entry.setId(String.valueOf(feedEntry.getId()));
		entry.setTitle(feedEntry.getTitle());
		entry.setContent(feedEntry.getContent());
		entry.setDate(feedEntry.getUpdated());
		entry.setUrl(feedEntry.getUrl());

		FeedEntryStatus status = feedEntryWithStatus.getStatus();
		entry.setRead(status == null ? false : status.isRead());

		return entry;
	}

	private Entry populateEntry(Entry entry, FeedSubscription sub) {
		entry.setFeedName(sub.getTitle());
		entry.setFeedId(String.valueOf(sub.getId()));
		return entry;
	}

	@Path("mark")
	@GET
	public Response mark(@QueryParam("type") Type type,
			@QueryParam("id") String id, @QueryParam("read") boolean read) {
		Preconditions.checkNotNull(type);
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(read);

		if (type == Type.entry) {
			FeedEntry entry = feedEntryService.findById(Long.valueOf(id));
			markEntry(entry, read);
		} else if (type == Type.feed) {
			Feed feed = feedSubscriptionService.findById(Long.valueOf(id))
					.getFeed();
			if (read) {
				List<FeedEntryWithStatus> entries = feedEntryService
						.getEntries(feed, getUser(), false);
				for (FeedEntryWithStatus entry : entries) {
					markEntry(entry.getEntry(), true);
				}
			}
		}
		return Response.ok(Status.OK).build();
	}

	private void markEntry(FeedEntry entry, boolean read) {
		FeedEntryStatus status = feedEntryStatusService.getStatus(getUser(),
				entry);
		if (status == null) {
			status = new FeedEntryStatus();
			status.setUser(getUser());
			status.setEntry(entry);
		}
		status.setRead(read);
		feedEntryStatusService.saveOrUpdate(status);
	}

}
