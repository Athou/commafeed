package com.commafeed.backend.dao;

import com.commafeed.backend.model.FeedEntryNote;
import com.commafeed.backend.model.User;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import java.util.List;

@Singleton
public class FeedEntryNoteDAO extends GenericDAO<FeedEntryNote> {

    private final EntityManager em;

    @Inject
    public FeedEntryNoteDAO(EntityManager entityManager) {
        super(entityManager, FeedEntryNote.class);
        this.em = entityManager;
    }

    public FeedEntryNote findByUserAndEntryId(User user, Long entryId) {
        List<FeedEntryNote> notes =
                em.createQuery(
                                "SELECT n FROM FeedEntryNote n WHERE n.user = :user AND n.entry.id = :entryId",
                                FeedEntryNote.class)
                        .setParameter("user", user)
                        .setParameter("entryId", entryId)
                        .getResultList();
        return notes.isEmpty() ? null : notes.get(0);
    }

    public List<FeedEntryNote> findByUser(User user) {
        return em.createQuery(
                        "SELECT n FROM FeedEntryNote n WHERE n.user = :user", FeedEntryNote.class)
                .setParameter("user", user)
                .getResultList();
    }

    public void saveOrUpdate(FeedEntryNote note) {
        if (note.getId() == null) {
            em.persist(note);
        } else {
            em.merge(note);
        }
    }
}
