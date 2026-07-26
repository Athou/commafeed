package com.commafeed.backend.service;

import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.dao.FeedEntryNoteDAO;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryNote;
import com.commafeed.backend.model.User;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor
public class FeedEntryNoteService {

    private final FeedEntryNoteDAO noteDAO;
    private final FeedEntryDAO entryDAO;

    @Transactional
    public FeedEntryNote saveOrUpdateNote(
            User user, Long entryId, String noteText, Integer rating) {
        FeedEntry entry = entryDAO.findById(entryId);
        if (entry == null) {
            throw new IllegalArgumentException("Entry not found with id: " + entryId);
        }

        FeedEntryNote note = noteDAO.findByUserAndEntryId(user, entryId);
        if (note == null) {
            note = new FeedEntryNote();
            note.setUser(user);
            note.setEntry(entry);
            note.setCreatedAt(new Date());
        }

        note.setNote(noteText);
        note.setRating(rating);
        note.setUpdatedAt(new Date());

        noteDAO.saveOrUpdate(note);
        return note;
    }

    public List<FeedEntryNote> getUserNotes(User user) {
        return noteDAO.findByUser(user);
    }
}
