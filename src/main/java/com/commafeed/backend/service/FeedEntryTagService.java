package com.commafeed.backend.service;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.RequiredArgsConstructor;

import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.dao.FeedEntryTagDAO;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryTag;
import com.commafeed.backend.model.User;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
@Singleton
public class FeedEntryTagService {

	private final FeedEntryDAO feedEntryDAO;
	private final FeedEntryTagDAO feedEntryTagDAO;

	public void updateTags(User user, Long entryId, List<String> tagNames) {
		FeedEntry entry = feedEntryDAO.findById(entryId);
		if (entry == null) {
			return;
		}

		List<FeedEntryTag> tags = feedEntryTagDAO.findByEntry(user, entry);
		Map<String, FeedEntryTag> tagMap = Maps.uniqueIndex(tags, new Function<FeedEntryTag, String>() {
			@Override
			public String apply(FeedEntryTag input) {
				return input.getName();
			}
		});

		List<FeedEntryTag> addList = Lists.newArrayList();
		List<FeedEntryTag> removeList = Lists.newArrayList();

		for (String tagName : tagNames) {
			FeedEntryTag tag = tagMap.get(tagName);
			if (tag == null) {
				addList.add(new FeedEntryTag(user, entry, tagName));
			}
		}

		for (FeedEntryTag tag : tags) {
			if (!tagNames.contains(tag.getName())) {
				removeList.add(tag);
			}
		}

		feedEntryTagDAO.saveOrUpdate(addList);
		feedEntryTagDAO.delete(removeList);
	}

}
