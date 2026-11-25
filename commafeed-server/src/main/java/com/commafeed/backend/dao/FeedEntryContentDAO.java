package com.commafeed.backend.dao;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryContent;
import com.commafeed.backend.model.FeedEntryContent_;
import com.commafeed.backend.model.FeedEntry_;

import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.JoinType;

import org.hibernate.query.criteria.CriteriaDefinition;

import java.util.List;

@Singleton
public class FeedEntryContentDAO extends GenericDAO<FeedEntryContent> {

    public FeedEntryContentDAO(EntityManager entityManager, CommaFeedConfiguration config) {
        super(entityManager, FeedEntryContent.class, config);
    }

    public List<FeedEntryContent> findExisting(String contentHash, String titleHash) {
        return query(
                        new CriteriaDefinition<>(getEntityManager(), FeedEntryContent.class) {
                            {
                                var content = from(FeedEntryContent.class);
                                where(
                                        equal(content.get(FeedEntryContent_.contentHash), contentHash),
                                        equal(content.get(FeedEntryContent_.titleHash), titleHash));
                            }
                        })
                .getResultList();
    }

    public long deleteWithoutEntries(int max) {
        List<Long> ids =
                query(
                                new CriteriaDefinition<>(getEntityManager(), Long.class) {
                                    {
                                        var content = from(FeedEntryContent.class);
                                        var entry = content.join(FeedEntry.class, JoinType.LEFT);
                                        entry.on(
                                                equal(
                                                        entry.get(FeedEntry_.content).get(FeedEntryContent_.id),
                                                        content.get(FeedEntryContent_.id)));
                                        select(content.get(FeedEntryContent_.id));
                                        where(isNull(entry.get(FeedEntry_.id)));
                                    }
                                })
                        .setMaxResults(max)
                        .getResultList();
        return deleteByIds(ids);
    }
}
