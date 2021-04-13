package com.commafeed.backend.service;

import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.dao.FeedEntryTagDAO;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryTag;
import com.commafeed.backend.model.User;
import com.google.common.annotations.VisibleForTesting;
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
		Set<String> existingTagNames = getTagNames(existingTags);

		List<FeedEntryTag> addList = getAddList(user, entry, tagNames, existingTagNames);
		List<FeedEntryTag> removeList = getRemoveList(existingTags, tagNames);

		feedEntryTagDAO.saveOrUpdate(addList);
		feedEntryTagDAO.delete(removeList);
	}

	@VisibleForTesting
	List<FeedEntryTag> getAddList(User user, FeedEntry entry, List<String> tagNames, Set<String> existingTagNames) {
		return tagNames.stream()
				.filter(name -> !existingTagNames.contains(name))
				.map(name -> new FeedEntryTag(user, entry, name))
				.collect(Collectors.toList());
	}

	@VisibleForTesting
	List<FeedEntryTag> getRemoveList(List<FeedEntryTag> existingTags, List<String> tagNames) {
		return existingTags.stream()
				.filter(tag -> !tagNames.contains(tag.getName()))
				.collect(Collectors.toList());
	}

	@VisibleForTesting
	Set<String> getTagNames(List<FeedEntryTag> tags) {
		return tags.stream()
				.map(FeedEntryTag::getName)
				.collect(Collectors.toSet());
	}

}
