package com.commafeed.frontend.rest.resources;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
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
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Path("/entries/")
@Api(value = "/entries", description = "Operations about feed entries")
public class EntriesREST extends AbstractResourceREST {

	public static final String ALL = "all";

	public enum Type {
		category, feed, entry;
	}

	public enum ReadType {
		all, unread;
	}

	@Path("/feed/get")
	@GET
	@ApiOperation(value = "Get feed entries", notes = "Get a list of feed entries", responseClass = "com.commafeed.frontend.model.Entries")
	public Entries getFeedEntries(
			@ApiParam(value = "id of the feed", required = true) @QueryParam("id") String id,
			@ApiParam(value = "all entries or only unread ones", allowableValues = "all,unread", required = true) @QueryParam("readType") ReadType readType,
			@ApiParam(value = "offset for paging") @DefaultValue("0") @QueryParam("offset") int offset,
			@ApiParam(value = "limit for paging") @DefaultValue("-1") @QueryParam("limit") int limit,
			@ApiParam(value = "date ordering", allowableValues = "asc,desc") @QueryParam("order") @DefaultValue("desc") ReadingOrder order) {

		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(readType);

		Entries entries = new Entries();
		boolean unreadOnly = readType == ReadType.unread;

		FeedSubscription subscription = feedSubscriptionDAO.findById(getUser(),
				Long.valueOf(id));
		if (subscription != null) {
			entries.setName(subscription.getTitle());
			entries.setMessage(subscription.getFeed().getMessage());
			entries.setErrorCount(subscription.getFeed().getErrorCount());

			List<FeedEntryStatus> unreadEntries = feedEntryStatusDAO
					.findByFeed(subscription.getFeed(), getUser(), unreadOnly,
							offset, limit, order, true);
			for (FeedEntryStatus status : unreadEntries) {
				entries.getEntries().add(buildEntry(status));
			}
		}

		entries.setTimestamp(Calendar.getInstance().getTimeInMillis());
		return entries;
	}

	@Path("/category/get")
	@GET
	@ApiOperation(value = "Get category entries", notes = "Get a list of category entries", responseClass = "com.commafeed.frontend.model.Entries")
	public Entries getCategoryEntries(
			@ApiParam(value = "id of the category, or 'all'", required = true) @QueryParam("id") String id,
			@ApiParam(value = "all entries or only unread ones", allowableValues = "all,unread", required = true) @QueryParam("readType") ReadType readType,
			@ApiParam(value = "offset for paging") @DefaultValue("0") @QueryParam("offset") int offset,
			@ApiParam(value = "limit for paging") @DefaultValue("-1") @QueryParam("limit") int limit,
			@ApiParam(value = "date ordering", allowableValues = "asc,desc") @QueryParam("order") @DefaultValue("desc") ReadingOrder order) {

		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(readType);

		Entries entries = new Entries();
		boolean unreadOnly = readType == ReadType.unread;

		if (ALL.equals(id)) {
			entries.setName("All");
			List<FeedEntryStatus> unreadEntries = feedEntryStatusDAO.findAll(
					getUser(), unreadOnly, offset, limit, order, true);
			for (FeedEntryStatus status : unreadEntries) {
				entries.getEntries().add(buildEntry(status));
			}

		} else {
			FeedCategory feedCategory = feedCategoryDAO.findById(getUser(),
					Long.valueOf(id));
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

	@Path("/entry/mark")
	@GET
	@ApiOperation(value = "Mark a feed entry", notes = "Mark a feed entry as read/unread")
	public Response markFeedEntry(
			@ApiParam(value = "entry id", required = true) @QueryParam("id") String id,
			@ApiParam(value = "read status", required = true) @QueryParam("read") boolean read) {
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(read);

		FeedEntryStatus status = feedEntryStatusDAO.findById(getUser(),
				Long.valueOf(id));
		status.setRead(read);
		feedEntryStatusDAO.update(status);

		return Response.ok(Status.OK).build();
	}

	@Path("/feed/mark")
	@GET
	@ApiOperation(value = "Mark feed entries", notes = "Mark feed entries as read/unread")
	public Response markFeedEntries(
			@ApiParam(value = "feed id", required = true) @QueryParam("id") String id,
			@ApiParam(value = "only entries older than this, prevent marking an entry that was not retrieved") @QueryParam("olderThan") Long olderThanTimestamp) {
		Preconditions.checkNotNull(id);

		Date olderThan = olderThanTimestamp == null ? null : new Date(
				olderThanTimestamp);

		FeedSubscription subscription = feedSubscriptionDAO.findById(getUser(),
				Long.valueOf(id));
		feedEntryStatusDAO.markFeedEntries(getUser(), subscription.getFeed(),
				olderThan);

		return Response.ok(Status.OK).build();
	}

	@Path("/category/mark")
	@GET
	@ApiOperation(value = "Mark category entries", notes = "Mark feed entries as read/unread")
	public Response markCategoryEntries(
			@ApiParam(value = "category id, or 'all'", required = true) @QueryParam("id") String id,
			@ApiParam(value = "only entries older than this, prevent marking an entry that was not retrieved") @QueryParam("olderThan") Long olderThanTimestamp) {
		Preconditions.checkNotNull(id);

		Date olderThan = olderThanTimestamp == null ? null : new Date(
				olderThanTimestamp);

		if (ALL.equals(id)) {
			feedEntryStatusDAO.markAllEntries(getUser(), olderThan);
		} else {
			List<FeedCategory> categories = feedCategoryDAO
					.findAllChildrenCategories(
							getUser(),
							feedCategoryDAO.findById(getUser(),
									Long.valueOf(id)));
			feedEntryStatusDAO.markCategoryEntries(getUser(), categories,
					olderThan);
		}

		return Response.ok(Status.OK).build();
	}

	@Path("/search")
	@GET
	@ApiOperation(value = "Search for entries", notes = "Look through title and content of entries by keywords", responseClass = "com.commafeed.frontend.model.Entries")
	public Entries searchEntries(
			@ApiParam(value = "keywords separated by spaces, 3 characters minimum", required = true) @QueryParam("keywords") String keywords,
			@ApiParam(value = "offset for paging") @DefaultValue("0") @QueryParam("offset") int offset,
			@ApiParam(value = "limit for paging") @DefaultValue("-1") @QueryParam("limit") int limit) {
		keywords = StringUtils.trimToEmpty(keywords);
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
