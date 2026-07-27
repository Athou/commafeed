package com.commafeed.backend.dao;

import com.commafeed.backend.model.FeedEntryKeyword;
import com.commafeed.backend.model.User;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import java.util.List;

@Singleton
public class FeedEntryKeywordDAO extends GenericDAO<FeedEntryKeyword> {

    private final EntityManager em;

    @Inject
    public FeedEntryKeywordDAO(EntityManager entityManager) {
        super(entityManager, FeedEntryKeyword.class);
        this.em = entityManager;
    }

    public List<FeedEntryKeyword> findAllKeywords() {
        return em.createQuery("SELECT k FROM FeedEntryKeyword k", FeedEntryKeyword.class)
                .getResultList();
    }

    public List<FeedEntryKeyword> findByUser(User user) {
        return em.createQuery(
                        "SELECT k FROM FeedEntryKeyword k WHERE k.user = :user",
                        FeedEntryKeyword.class)
                .setParameter("user", user)
                .getResultList();
    }

    public void saveOrUpdate(FeedEntryKeyword keyword) {
        if (keyword.getId() == null) {
            em.persist(keyword);
        } else {
            em.merge(keyword);
        }
    }
}
