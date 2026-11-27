package com.commafeed.backend.dao;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryContent;
import com.commafeed.backend.model.FeedEntryContent_;
import com.commafeed.backend.model.FeedEntry_;

import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;

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

    public long findMaxId() {
        Long maxId =
                query(
                                new CriteriaDefinition<>(getEntityManager(), Long.class) {
                                    {
                                        var content = from(FeedEntryContent.class);
                                        select(max(content.get(FeedEntryContent_.id)));
                                    }
                                })
                        .getSingleResult();
        return maxId == null ? 0 : maxId;
    }

    public Long findIdAfter(long fromIdExclusive, int count) {
        return query(
                        new CriteriaDefinition<>(getEntityManager(), Long.class) {
                            {
                                var content = from(FeedEntryContent.class);
                                select(content.get(FeedEntryContent_.id));
                                where(greaterThan(content.get(FeedEntryContent_.id), fromIdExclusive));
                                orderBy(asc(content.get(FeedEntryContent_.id)));
                            }
                        })
                .setFirstResult(count - 1)
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public long deleteWithoutEntries(long fromIdExclusive, long toIdInclusive, int max) {
        List<Long> ids =
                query(
                                new CriteriaDefinition<>(getEntityManager(), Long.class) {
                                    {
                                        var content = from(FeedEntryContent.class);
                                        var subquery = subquery(Integer.class);
                                        var entry = subquery.from(FeedEntry.class);
                                        subquery.select(literal(1))
                                                .where(equal(entry.get(FeedEntry_.content), content));

                                        select(content.get(FeedEntryContent_.id));
                                        where(
                                                greaterThan(content.get(FeedEntryContent_.id), fromIdExclusive),
                                                lessThanOrEqualTo(content.get(FeedEntryContent_.id), toIdInclusive),
                                                not(exists(subquery)));
                                    }
                                })
                        .setMaxResults(max)
                        .getResultList();
        return deleteByIds(ids);
    }
}
