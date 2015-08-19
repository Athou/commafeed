package com.commafeed.backend.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.RequiredArgsConstructor;

import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.dao.FeedEntryTagDAO;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryTag;
import com.commafeed.backend.model.User;

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

		List<FeedEntryTag> existingTags = feedEntryTagDAO.findByEntry(user, entry);
		Set<String> existingTagNames = existingTags.stream().map(t -> t.getName()).collect(Collectors.toSet());

		List<FeedEntryTag> addList = tagNames.stream().filter(name -> !existingTagNames.contains(name))
				.map(name -> new FeedEntryTag(user, entry, name)).collect(Collectors.toList());
		List<FeedEntryTag> removeList = existingTags.stream().filter(tag -> !tagNames.contains(tag.getName())).collect(Collectors.toList());

		feedEntryTagDAO.saveOrUpdate(addList);
		feedEntryTagDAO.delete(removeList);
	}

}
