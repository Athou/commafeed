package com.commafeed.frontend.rest.resources;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;

import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.UserSettings.ReadingOrder;
import com.commafeed.frontend.model.Entries;
import com.commafeed.frontend.model.Entry;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

@Path("entries")
public class EntriesREST extends AbstractREST {

	public static final String ALL = "all";

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
			@DefaultValue("-1") @QueryParam("limit") int limit,
			@QueryParam("order") @DefaultValue("desc") ReadingOrder order) {

		Preconditions.checkNotNull(type);
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(readType);

		Entries entries = new Entries();
		boolean unreadOnly = readType == ReadType.unread;

		if (type == Type.feed) {
			FeedSubscription subscription = feedSubscriptionDAO.findById(
					getUser(), Long.valueOf(id));
			if (subscription != null) {
				entries.setName(subscription.getTitle());
				entries.setMessage(subscription.getFeed().getMessage());
				entries.setErrorCount(subscription.getFeed().getErrorCount());

				List<FeedEntryStatus> unreadEntries = feedEntryStatusDAO
						.findByFeed(subscription.getFeed(), getUser(),
								unreadOnly, offset, limit, order, true);
				for (FeedEntryStatus status : unreadEntries) {
					entries.getEntries().add(buildEntry(status));
				}
			}

		} else {

			if (ALL.equals(id)) {
				entries.setName("All");
				List<FeedEntryStatus> unreadEntries = feedEntryStatusDAO
						.findAll(getUser(), unreadOnly, offset, limit,
								order, true);
				for (FeedEntryStatus status : unreadEntries) {
					entries.getEntries().add(buildEntry(status));
				}

			} else {
				FeedCategory feedCategory = feedCategoryDAO.findById(
						getUser(), Long.valueOf(id));
				if (feedCategory != null) {
					List<FeedCategory> childrenCategories = feedCategoryDAO
							.findAllChildrenCategories(getUser(), feedCategory);
					List<FeedEntryStatus> unreadEntries = feedEntryStatusDAO
							.findByCategories(childrenCategories, getUser(),
									unreadOnly, offset, limit, order, true);
					for (FeedEntryStatus status : unreadEntries) {
						entries.getEntries().add(buildEntry(status));
					}
					entries.setName(feedCategory.getName());
				}
			}

		}
		entries.setTimestamp(Calendar.getInstance().getTimeInMillis());
		return entries;
	}

	private Entry buildEntry(FeedEntryStatus status) {
		Entry entry = new Entry();

		FeedEntry feedEntry = status.getEntry();
		entry.setId(String.valueOf(status.getId()));
		entry.setTitle(feedEntry.getContent().getTitle());
		entry.setContent(feedEntry.getContent().getContent());
		entry.setEnclosureUrl(status.getEntry().getContent().getEnclosureUrl());
		entry.setEnclosureType(status.getEntry().getContent()
				.getEnclosureType());
		entry.setDate(feedEntry.getUpdated());
		entry.setUrl(feedEntry.getUrl());

		entry.setRead(status.isRead());

		entry.setFeedName(status.getSubscription().getTitle());
		entry.setFeedId(String.valueOf(status.getSubscription().getId()));
		entry.setFeedUrl(status.getSubscription().getFeed().getLink());

		return entry;
	}

	@Path("mark")
	@GET
	public Response mark(@QueryParam("type") Type type,
			@QueryParam("id") String id, @QueryParam("read") boolean read,
			@QueryParam("olderThan") Long olderThanTimestamp) {
		Preconditions.checkNotNull(type);
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(read);

		Date olderThan = olderThanTimestamp == null ? null : new Date(
				olderThanTimestamp);

		if (type == Type.entry) {
			FeedEntryStatus status = feedEntryStatusDAO.findById(getUser(),
					Long.valueOf(id));
			status.setRead(read);
			feedEntryStatusDAO.update(status);
		} else if (type == Type.feed) {
			if (read) {
				FeedSubscription subscription = feedSubscriptionDAO
						.findById(getUser(), Long.valueOf(id));
				feedEntryStatusDAO.markFeedEntries(getUser(),
						subscription.getFeed(), olderThan);
			} else {
				throw new WebApplicationException(Response.status(
						Status.INTERNAL_SERVER_ERROR).build());
			}
		} else if (type == Type.category) {
			if (read) {
				if (ALL.equals(id)) {
					feedEntryStatusDAO.markAllEntries(getUser(), olderThan);
				} else {
					List<FeedCategory> categories = feedCategoryDAO
							.findAllChildrenCategories(getUser(),
									feedCategoryDAO.findById(getUser(),
											Long.valueOf(id)));
					feedEntryStatusDAO.markCategoryEntries(getUser(),
							categories, olderThan);
				}
			} else {
				throw new WebApplicationException(Response.status(
						Status.INTERNAL_SERVER_ERROR).build());
			}
		}
		return Response.ok(Status.OK).build();
	}

	@Path("search")
	@GET
	public Entries searchEntries(@QueryParam("keywords") String keywords,
			@DefaultValue("0") @QueryParam("offset") int offset,
			@DefaultValue("-1") @QueryParam("limit") int limit) {
		Preconditions.checkArgument(StringUtils.length(keywords) >= 3);

		Entries entries = new Entries();

		List<Entry> list = Lists.newArrayList();
		List<FeedEntryStatus> entriesStatus = feedEntryStatusDAO
				.findByKeywords(getUser(), keywords, offset, limit, true);
		for (FeedEntryStatus status : entriesStatus) {
			list.add(buildEntry(status));
		}

		entries.setName("Search for : " + keywords);
		entries.getEntries().addAll(list);
		return entries;
	}

}
