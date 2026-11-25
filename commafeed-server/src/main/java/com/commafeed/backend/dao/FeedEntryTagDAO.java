package com.commafeed.backend.dao;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryTag;
import com.commafeed.backend.model.FeedEntryTag_;
import com.commafeed.backend.model.User;

import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;

import org.hibernate.query.criteria.CriteriaDefinition;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class FeedEntryTagDAO extends GenericDAO<FeedEntryTag> {

    public FeedEntryTagDAO(EntityManager entityManager, CommaFeedConfiguration config) {
        super(entityManager, FeedEntryTag.class, config);
    }

    public List<String> findByUser(User user) {
        return query(
                        new CriteriaDefinition<>(getEntityManager(), String.class) {
                            {
                                var tag = from(FeedEntryTag.class);
                                select(tag.get(FeedEntryTag_.name));
                                where(equal(tag.get(FeedEntryTag_.user), user));
                                distinct(true);
                            }
                        })
                .getResultList();
    }

    public List<FeedEntryTag> findByEntry(User user, FeedEntry entry) {
        return query(
                        new CriteriaDefinition<>(getEntityManager(), FeedEntryTag.class) {
                            {
                                var tag = from(FeedEntryTag.class);
                                where(
                                        equal(tag.get(FeedEntryTag_.user), user),
                                        equal(tag.get(FeedEntryTag_.entry), entry));
                            }
                        })
                .getResultList();
    }

    public Map<Long, List<FeedEntryTag>> findByEntries(User user, List<FeedEntry> entries) {
        if (entries.isEmpty()) {
            return Map.of();
        }

        return query(
                        new CriteriaDefinition<>(getEntityManager(), FeedEntryTag.class) {
                            {
                                var tag = from(FeedEntryTag.class);
                                where(
                                        equal(tag.get(FeedEntryTag_.user), user),
                                        tag.get(FeedEntryTag_.entry).in(entries));
                            }
                        })
                .getResultList()
                .stream()
                .collect(Collectors.groupingBy(t -> t.getEntry().getId()));
    }
}
