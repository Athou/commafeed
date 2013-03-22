package com.commafeed.frontend.rest;

import java.text.DateFormat;
import java.util.List;

import javax.inject.Inject;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.commafeed.backend.dao.FeedEntryService;
import com.commafeed.backend.dao.FeedEntryStatusService;
import com.commafeed.backend.dao.FeedSubscriptionService;
import com.commafeed.frontend.rest.model.Entries;
import com.commafeed.frontend.rest.model.Entry;
import com.commafeed.frontend.utils.ModelFactory.MF;
import com.commafeed.model.FeedEntry;
import com.commafeed.model.FeedEntryStatus;
import com.commafeed.model.FeedSubscription;
import com.google.common.collect.Iterables;

public class FeedEntriesREST extends JSONPage {

	public static final String PARAM_TYPE = "type";
	public static final String PARAM_ID = "id";
	public static final String PARAM_READTYPE = "readtype";

	@Inject
	FeedEntryService feedEntryService;

	@Inject
	FeedEntryStatusService feedEntryStatusService;

	@Inject
	FeedSubscriptionService feedSubscriptionService;

	public FeedEntriesREST(PageParameters pageParameters) {
		super(pageParameters);
	}

	@Override
	protected Object getObject(PageParameters parameters) {

		String type = parameters.get(PARAM_TYPE).toString();
		String id = parameters.get(PARAM_ID).toString();

		@SuppressWarnings("unused")
		String readType = parameters.get(PARAM_READTYPE).toString();

		Entries entries = new Entries();
		if ("feed".equals(type)) {
			FeedSubscription subscription = feedSubscriptionService
					.findById(Long.valueOf(id));
			List<FeedEntry> feedEntries = feedEntryService.getUnreadEntries(
					subscription.getFeed(), getUser());

			entries.setName(subscription.getTitle());
			for (FeedEntry feedEntry : feedEntries) {
				
				List<FeedEntryStatus> feedEntryStatus = feedEntryStatusService.findByField(MF.i(MF.p(FeedEntryStatus.class).getEntry()), feedEntry);
				
				Entry entry = buildEntry(feedEntry);
				entry.setFeedName(subscription.getTitle());
				entry.setFeedId(String.valueOf(subscription.getId()));
				entry.setRead(feedEntryStatus.isEmpty() ? false : Iterables.getFirst(feedEntryStatus, null).isRead());
				entry.setStarred(feedEntryStatus.isEmpty() ? false : Iterables.getFirst(feedEntryStatus, null).isStarred());
				entries.getEntries().add(entry);
			}
		}

		return entries;
	}

	private Entry buildEntry(FeedEntry feedEntry) {
		Entry entry = new Entry();
		entry.setId(feedEntry.getGuid());
		entry.setTitle(feedEntry.getTitle());
		entry.setContent(feedEntry.getContent());
		entry.setDate(DateFormat.getDateInstance(DateFormat.SHORT, getLocale())
				.format(feedEntry.getUpdated()));
		entry.setUrl(feedEntry.getUrl());

		return entry;
	}

}
