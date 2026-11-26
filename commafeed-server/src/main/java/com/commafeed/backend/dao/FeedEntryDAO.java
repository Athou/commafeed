package com.commafeed.backend.dao;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntry_;
import com.commafeed.backend.model.Feed_;
import com.google.common.collect.Lists;

import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.Expression;

import org.hibernate.query.criteria.CriteriaDefinition;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Singleton
public class FeedEntryDAO extends GenericDAO<FeedEntry> {

    private static final int IN_CLAUSE_BATCH_SIZE = 1000;

    public FeedEntryDAO(EntityManager entityManager, CommaFeedConfiguration config) {
        super(entityManager, FeedEntry.class, config);
    }

    public FeedEntry findExisting(String guidHash, Feed feed) {
        return query(
                        new CriteriaDefinition<>(getEntityManager(), FeedEntry.class) {
                            {
                                var entry = from(FeedEntry.class);
                                where(
                                        equal(entry.get(FeedEntry_.guidHash), guidHash),
                                        equal(entry.get(FeedEntry_.feed), feed));
                            }
                        })
                .setMaxResults(1)
                .getSingleResultOrNull();
    }

    public Set<String> findExistingGuidHashes(Set<String> guidHashes, Feed feed) {
        if (guidHashes.isEmpty()) {
            return Set.of();
        }

        Set<String> result = new HashSet<>();
        for (List<String> batch : Lists.partition(new ArrayList<>(guidHashes), IN_CLAUSE_BATCH_SIZE)) {
            result.addAll(
                    query(
                                    new CriteriaDefinition<>(getEntityManager(), String.class) {
                                        {
                                            var entry = from(FeedEntry.class);
                                            select(entry.get(FeedEntry_.guidHash));
                                            where(
                                                    equal(entry.get(FeedEntry_.feed), feed),
                                                    entry.get(FeedEntry_.guidHash).in(batch));
                                        }
                                    })
                            .getResultList());
        }
        return result;
    }

    public List<FeedCapacity> findFeedsExceedingCapacity(long maxCapacity, long max, boolean keepStarredEntries) {
        var criteria =
                new CriteriaDefinition<>(getEntityManager(), Tuple.class) {
                    final Expression<Long> feedId;
                    final Expression<Long> count;

                    {
                        var entry = from(FeedEntry.class);
                        feedId = entry.get(FeedEntry_.feed).get(Feed_.id);
                        count = count(entry.get(FeedEntry_.id));
                        select(tuple(feedId, count));

                        if (keepStarredEntries) {
                            where(Predicates.isNotStarred(this, this, entry));
                        }

                        groupBy(entry.get(FeedEntry_.feed));
                        having(greaterThan(count, maxCapacity));
                    }
                };

        return query(criteria).setMaxResults(Math.toIntExact(max)).getResultList().stream()
                .map(t -> new FeedCapacity(t.get(criteria.feedId), t.get(criteria.count)))
                .toList();
    }

    public int delete(Long feedId, long max) {
        List<FeedEntry> list =
                query(
                                new CriteriaDefinition<>(getEntityManager(), FeedEntry.class) {
                                    {
                                        var entry = from(FeedEntry.class);
                                        where(equal(entry.get(FeedEntry_.feed).get(Feed_.id), feedId));
                                    }
                                })
                        .setMaxResults(Math.toIntExact(max))
                        .getResultList();
        return delete(list);
    }

    /** Delete entries older than a certain date */
    public int deleteEntriesOlderThan(Instant olderThan, long max, boolean keepStarredEntries) {
        List<FeedEntry> list =
                query(
                                new CriteriaDefinition<>(getEntityManager(), FeedEntry.class) {
                                    {
                                        var entry = from(FeedEntry.class);
                                        where(lessThan(entry.get(FeedEntry_.published), olderThan));
                                        if (keepStarredEntries) {
                                            restrict(Predicates.isNotStarred(this, this, entry));
                                        }
                                        orderBy(asc(entry.get(FeedEntry_.published)));
                                    }
                                })
                        .setMaxResults(Math.toIntExact(max))
                        .getResultList();
        return delete(list);
    }

    /** Delete the oldest entries of a feed */
    public int deleteOldEntries(Long feedId, long max, boolean keepStarredEntries) {
        List<FeedEntry> list =
                query(
                                new CriteriaDefinition<>(getEntityManager(), FeedEntry.class) {
                                    {
                                        var entry = from(FeedEntry.class);
                                        where(equal(entry.get(FeedEntry_.feed).get(Feed_.id), feedId));
                                        if (keepStarredEntries) {
                                            restrict(Predicates.isNotStarred(this, this, entry));
                                        }
                                        orderBy(asc(entry.get(FeedEntry_.published)));
                                    }
                                })
                        .setMaxResults(Math.toIntExact(max))
                        .getResultList();
        return delete(list);
    }

    public record FeedCapacity(Long id, Long capacity) {}
}
