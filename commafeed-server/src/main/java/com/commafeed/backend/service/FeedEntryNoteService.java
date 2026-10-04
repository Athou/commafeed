package com.commafeed.backend.service;

import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.dao.FeedEntryNoteDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryNote;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.User;

import jakarta.inject.Singleton;

import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Singleton
public class FeedEntryNoteService {

    private final FeedEntryDAO feedEntryDAO;
    private final FeedEntryNoteDAO feedEntryNoteDAO;
    private final FeedSubscriptionDAO feedSubscriptionDAO;

    public FeedEntryNote saveOrUpdate(User user, Long entryId, String comment, Integer rating) {
        FeedEntry feedEntry = feedEntryDAO.findById(entryId);
        if (feedEntry == null) {
            return null;
        }

        FeedSubscription subscription = feedSubscriptionDAO.findByFeed(user, feedEntry.getFeed());
        if (subscription == null) {
            return null;
        }

        FeedEntryNote note = feedEntryNoteDAO.findByUserAndFeedEntry(user, feedEntry);
        if (note == null) {
            note = new FeedEntryNote();
            note.setUser(user);
            note.setFeedEntry(feedEntry);
            note.setCreated(new Date());
            feedEntryNoteDAO.persist(note);
        }

        note.setComment(comment);
        note.setRating(rating);
        return note;
    }

    public List<FeedEntryNote> findByUser(User user) {
        return feedEntryNoteDAO.findByUser(user);
    }
}
