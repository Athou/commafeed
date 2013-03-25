package com.commafeed.frontend.rest.resources;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.apache.commons.lang.ObjectUtils;

import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.frontend.model.Entries;
import com.commafeed.frontend.model.Entry;
import com.commafeed.frontend.utils.ModelFactory.MF;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Path("entries")
public class EntriesREST extends AbstractREST {

	@Path("get/{type}/{id}/{readType}")
	@GET
	public Entries getEntries(@PathParam("type") String type,
			@PathParam("id") String id, @PathParam("readType") String readType,
			@DefaultValue("0") @QueryParam("offset") int offset,
			@DefaultValue("-1") @QueryParam("limit") int limit) {

		Entries entries = new Entries();
		boolean unreadOnly = "unread".equals(readType);

		if ("feed".equals(type)) {
			FeedSubscription subscription = feedSubscriptionService.findById(
					getUser(), Long.valueOf(id));
			entries.setName(subscription.getTitle());
			entries.getEntries().addAll(
					buildEntries(subscription, offset, limit, unreadOnly));

		} else {
			FeedCategory feedCategory = "all".equals(id) ? null
					: feedCategoryService.findById(getUser(), Long.valueOf(id));
			Collection<FeedSubscription> subscriptions = "all".equals(id) ? feedSubscriptionService
					.findAll(getUser()) : feedSubscriptionService
					.findWithCategory(getUser(), feedCategory);

			entries.setName("all".equals(id) ? "All" : feedCategory.getName());
			for (FeedSubscription subscription : subscriptions) {
				entries.getEntries().addAll(
						buildEntries(subscription, offset, limit, unreadOnly));
			}
		}

		Collections.sort(entries.getEntries(), new Comparator<Entry>() {

			@Override
			public int compare(Entry e1, Entry e2) {
				return ObjectUtils.compare(e2.getDate(), e1.getDate());
			}
		});

		int lastIndex = entries.getEntries().size()
				- (entries.getEntries().isEmpty() ? 0 : 1);
		int from = Math.min(lastIndex, offset);
		int to = limit == -1 ? lastIndex : Math.min(lastIndex, offset + limit);

		List<Entry> subList = entries.getEntries().subList(from, to);
		entries.setEntries(Lists.newArrayList(subList));
		return entries;
	}

	private List<Entry> buildEntries(FeedSubscription subscription, int offset,
			int limit, boolean unreadOnly) {
		List<FeedEntry> feedEntries = null;

		if (unreadOnly) {
			feedEntries = feedEntryService.getUnreadEntries(
					subscription.getFeed(), getUser(), offset, limit);
		} else {
			feedEntries = feedEntryService.getAllEntries(
					subscription.getFeed(), offset, limit);
		}

		List<Entry> entries = Lists.newArrayList();
		for (FeedEntry feedEntry : feedEntries) {

			List<FeedEntryStatus> feedEntryStatus = feedEntryStatusService
					.findByField(MF.i(MF.p(FeedEntryStatus.class).getEntry()),
							feedEntry);

			Entry entry = buildEntry(feedEntry);
			entry.setFeedName(subscription.getTitle());
			entry.setFeedId(String.valueOf(subscription.getId()));
			entry.setRead(feedEntryStatus.isEmpty() ? false : Iterables
					.getFirst(feedEntryStatus, null).isRead());
			entry.setStarred(feedEntryStatus.isEmpty() ? false : Iterables
					.getFirst(feedEntryStatus, null).isStarred());
			entries.add(entry);
		}
		return entries;
	}

	private Entry buildEntry(FeedEntry feedEntry) {
		Entry entry = new Entry();
		entry.setId(String.valueOf(feedEntry.getId()));
		entry.setTitle(feedEntry.getTitle());
		entry.setContent(feedEntry.getContent());
		entry.setDate(feedEntry.getUpdated());
		entry.setUrl(feedEntry.getUrl());

		return entry;
	}

	@Path("mark/{type}/{id}/{read}")
	@GET
	public void mark(@PathParam("type") String type,
			@PathParam("id") String id, @PathParam("read") boolean read) {
		if ("entry".equals(type)) {
			FeedEntry entry = feedEntryService.findById(Long.valueOf(id));
			FeedEntryStatus status = feedEntryStatusService.getStatus(
					getUser(), entry);
			if (status == null) {
				status = new FeedEntryStatus();
				status.setUser(getUser());
				status.setEntry(entry);
			}
			status.setRead(read);
			feedEntryStatusService.saveOrUpdate(status);
		}
	}

}
