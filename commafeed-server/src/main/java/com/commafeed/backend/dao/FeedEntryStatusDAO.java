package com.commafeed.backend.dao;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.feed.FeedEntryKeyword;
import com.commafeed.backend.feed.FeedEntryKeyword.Mode;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryContent;
import com.commafeed.backend.model.FeedEntryContent_;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.FeedEntryStatus_;
import com.commafeed.backend.model.FeedEntryTag;
import com.commafeed.backend.model.FeedEntryTag_;
import com.commafeed.backend.model.FeedEntry_;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.FeedSubscription_;
import com.commafeed.backend.model.Feed_;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserSettings.ReadingOrder;
import com.commafeed.backend.model.User_;
import com.commafeed.frontend.model.UnreadCount;

import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.query.common.TemporalUnit;
import org.hibernate.query.criteria.CriteriaDefinition;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class FeedEntryStatusDAO extends GenericDAO<FeedEntryStatus> {

    private final FeedEntryTagDAO feedEntryTagDAO;
    private final CommaFeedConfiguration config;

    public FeedEntryStatusDAO(
            EntityManager entityManager, FeedEntryTagDAO feedEntryTagDAO, CommaFeedConfiguration config) {
        super(entityManager, FeedEntryStatus.class, config);
        this.feedEntryTagDAO = feedEntryTagDAO;
        this.config = config;
    }

    public FeedEntryStatus getStatus(User user, FeedSubscription sub, FeedEntry entry) {
        List<FeedEntryStatus> statuses =
                query(
                                new CriteriaDefinition<>(getEntityManager(), FeedEntryStatus.class) {
                                    {
                                        var status = from(FeedEntryStatus.class);
                                        where(
                                                equal(status.get(FeedEntryStatus_.entry), entry),
                                                equal(status.get(FeedEntryStatus_.subscription), sub));
                                    }
                                })
                        .getResultList();
        FeedEntryStatus status = statuses.stream().findFirst().orElse(null);
        return handleStatus(user, status, sub, entry);
    }

    /** creates an artificial "unread" status if status is null */
    private FeedEntryStatus handleStatus(User user, FeedEntryStatus status, FeedSubscription sub, FeedEntry entry) {
        if (status == null) {
            Instant statusesInstantThreshold = config.database().cleanup().statusesInstantThreshold();
            boolean read = statusesInstantThreshold != null && entry.getPublished().isBefore(statusesInstantThreshold);
            status = new FeedEntryStatus(user, sub, entry);
            status.setRead(read);
            status.setMarkable(!read);
        } else {
            status.setMarkable(true);
        }
        return status;
    }

    private void fetchTags(User user, List<FeedEntryStatus> statuses) {
        Map<Long, List<FeedEntryTag>> tagsByEntryIds =
                feedEntryTagDAO.findByEntries(user, statuses.stream().map(FeedEntryStatus::getEntry).toList());
        for (FeedEntryStatus status : statuses) {
            List<FeedEntryTag> tags = tagsByEntryIds.get(status.getEntry().getId());
            status.setTags(tags == null ? List.of() : tags);
        }
    }

    public List<FeedEntryStatus> findStarred(
            User user,
            List<FeedEntryKeyword> keywords,
            Instant newerThan,
            int offset,
            int limit,
            ReadingOrder order,
            boolean includeContent) {
        var criteria =
                new CriteriaDefinition<>(getEntityManager(), FeedEntryStatus.class) {
                    {
                        var status = from(FeedEntryStatus.class);
                        where(
                                equal(status.get(FeedEntryStatus_.user), user),
                                isTrue(status.get(FeedEntryStatus_.starred)));

                        if (includeContent || CollectionUtils.isNotEmpty(keywords)) {
                            var entry = fetchJoin(status, FeedEntryStatus_.entry, JoinType.INNER);
                            var content = fetchJoin(entry, FeedEntry_.content, JoinType.INNER);
                            if (CollectionUtils.isNotEmpty(keywords)) {
                                restrict(keywordsPredicate(this, content, keywords));
                            }
                        }

                        if (newerThan != null) {
                            restrict(greaterThan(status.get(FeedEntryStatus_.entryInserted), newerThan));
                        }

                        if (order == ReadingOrder.ASC) {
                            orderBy(
                                    asc(status.get(FeedEntryStatus_.entryPublished)),
                                    asc(status.get(FeedEntryStatus_.id)));
                        } else {
                            orderBy(
                                    desc(status.get(FeedEntryStatus_.entryPublished)),
                                    desc(status.get(FeedEntryStatus_.id)));
                        }
                    }
                };

        var query = query(criteria);
        if (offset > -1) {
            query.setFirstResult(offset);
        }

        if (limit > -1) {
            query.setMaxResults(limit);
        }

        List<FeedEntryStatus> statuses = query.getResultList();
        statuses.forEach(s -> s.setMarkable(true));
        if (includeContent) {
            fetchTags(user, statuses);
        }

        return statuses;
    }

    public List<FeedEntryStatus> findBySubscriptions(
            User user,
            List<FeedSubscription> subs,
            boolean unreadOnly,
            List<FeedEntryKeyword> keywords,
            Instant newerThan,
            int offset,
            int limit,
            ReadingOrder order,
            boolean includeContent,
            String tag,
            Long minEntryId,
            Long maxEntryId) {
        Map<Long, List<FeedSubscription>> subsByFeedId =
                subs.stream().collect(Collectors.groupingBy(s -> s.getFeed().getId()));

        var criteria =
                new CriteriaDefinition<>(getEntityManager(), Tuple.class) {
                    final Root<FeedEntry> entry;
                    final Join<FeedEntry, FeedEntryStatus> status;

                    {
                        entry = from(FeedEntry.class);
                        status = entry.join(FeedEntry_.statuses, JoinType.LEFT);
                        status.on(status.get(FeedEntryStatus_.subscription).in(subs));
                        select(tuple(entry, status));
                        where(entry.get(FeedEntry_.feed).get(Feed_.id).in(subsByFeedId.keySet()));

                        if (includeContent || CollectionUtils.isNotEmpty(keywords)) {
                            var content = fetchJoin(entry, FeedEntry_.content, JoinType.INNER);
                            if (CollectionUtils.isNotEmpty(keywords)) {
                                restrict(keywordsPredicate(this, content, keywords));
                            }
                        }

                        if (unreadOnly && tag == null) {
                            restrict(unreadPredicate(this, entry, status));
                        }

                        if (tag != null) {
                            var tagJoin = entry.join(FeedEntry_.tags, JoinType.INNER);
                            tagJoin.on(
                                    equal(tagJoin.get(FeedEntryTag_.user).get(User_.id), user.getId()),
                                    equal(tagJoin.get(FeedEntryTag_.name), tag));
                        }

                        if (newerThan != null) {
                            restrict(greaterThanOrEqualTo(entry.get(FeedEntry_.inserted), newerThan));
                        }

                        if (minEntryId != null) {
                            restrict(greaterThan(entry.get(FeedEntry_.id), minEntryId));
                        }

                        if (maxEntryId != null) {
                            restrict(lessThan(entry.get(FeedEntry_.id), maxEntryId));
                        }

                        if (order != null) {
                            if (order == ReadingOrder.ASC) {
                                orderBy(asc(entry.get(FeedEntry_.published)), asc(entry.get(FeedEntry_.id)));
                            } else {
                                orderBy(desc(entry.get(FeedEntry_.published)), desc(entry.get(FeedEntry_.id)));
                            }
                        }
                    }
                };

        var query = query(criteria);
        if (offset > -1) {
            query.setFirstResult(offset);
        }

        if (limit > -1) {
            query.setMaxResults(limit);
        }

        List<FeedEntryStatus> statuses = new ArrayList<>();
        List<Tuple> tuples = query.getResultList();
        for (Tuple tuple : tuples) {
            FeedEntry e = tuple.get(criteria.entry);
            FeedEntryStatus s = tuple.get(criteria.status);
            for (FeedSubscription sub : subsByFeedId.get(e.getFeed().getId())) {
                statuses.add(handleStatus(user, s, sub, e));
            }
        }

        if (includeContent) {
            fetchTags(user, statuses);
        }

        return statuses;
    }

    private static Predicate keywordsPredicate(
            HibernateCriteriaBuilder cb, Path<FeedEntryContent> content, List<FeedEntryKeyword> keywords) {
        List<Predicate> predicates = new ArrayList<>();
        for (FeedEntryKeyword keyword : keywords) {
            Predicate or =
                    cb.or(
                            containsIgnoreCase(cb, content.get(FeedEntryContent_.content), keyword.keyword()),
                            containsIgnoreCase(cb, content.get(FeedEntryContent_.title), keyword.keyword()));
            predicates.add(keyword.mode() == Mode.EXCLUDE ? cb.not(or) : or);
        }
        return cb.and(predicates);
    }

    public Map<Long, UnreadCount> getUnreadCounts(List<FeedSubscription> subs) {
        if (subs.isEmpty()) {
            return Map.of();
        }

        Map<Long, List<FeedSubscription>> subsByFeedId =
                subs.stream().collect(Collectors.groupingBy(s -> s.getFeed().getId()));

        var criteria =
                new CriteriaDefinition<>(getEntityManager(), Tuple.class) {
                    final Path<Long> feedId;
                    final Expression<Long> count;
                    final Expression<Instant> maxPublished;

                    {
                        var entry = from(FeedEntry.class);
                        var status = entry.join(FeedEntry_.statuses, JoinType.LEFT);
                        status.on(status.get(FeedEntryStatus_.subscription).in(subs));

                        feedId = entry.get(FeedEntry_.feed).get(Feed_.id);
                        count = count(entry);
                        maxPublished = greatest(entry.get(FeedEntry_.published));
                        select(tuple(feedId, count, maxPublished));

                        where(feedId.in(subsByFeedId.keySet()), unreadPredicate(this, entry, status));
                        groupBy(feedId);
                    }
                };

        Map<Long, UnreadCount> result = new HashMap<>();
        for (Tuple tuple : query(criteria).getResultList()) {
            Long feedId = tuple.get(criteria.feedId);
            Long count = tuple.get(criteria.count);
            Instant published = tuple.get(criteria.maxPublished);
            for (FeedSubscription sub : subsByFeedId.get(feedId)) {
                result.put(sub.getId(), new UnreadCount(sub.getId(), count == null ? 0 : count, published));
            }
        }

        // feeds with no unread entries are absent from the query result, default them to zero
        for (FeedSubscription sub : subs) {
            result.putIfAbsent(sub.getId(), new UnreadCount(sub.getId(), 0, null));
        }

        return result;
    }

    private Predicate unreadPredicate(
            HibernateCriteriaBuilder cb, Path<FeedEntry> entry, Path<FeedEntryStatus> status) {
        Path<Boolean> read = status.get(FeedEntryStatus_.read);
        Predicate unread = cb.or(cb.isNull(read), cb.isFalse(read));

        Instant statusesInstantThreshold = config.database().cleanup().statusesInstantThreshold();
        if (statusesInstantThreshold != null) {
            return cb.and(unread, cb.greaterThanOrEqualTo(entry.get(FeedEntry_.published), statusesInstantThreshold));
        } else {
            return unread;
        }
    }

    public long deleteOldStatuses(Instant olderThan, int limit) {
        List<Long> ids =
                query(
                                new CriteriaDefinition<>(getEntityManager(), Long.class) {
                                    {
                                        var status = from(FeedEntryStatus.class);
                                        select(status.get(FeedEntryStatus_.id));
                                        where(
                                                lessThan(status.get(FeedEntryStatus_.entryInserted), olderThan),
                                                isFalse(status.get(FeedEntryStatus_.starred)));
                                    }
                                })
                        .setMaxResults(limit)
                        .getResultList();
        return deleteByIds(ids);
    }

    public long autoMarkAsRead(int limit) {
        Instant now = Instant.now();

        var criteria =
                new CriteriaDefinition<>(getEntityManager(), Tuple.class) {
                    final Root<FeedEntry> entry;
                    final Join<FeedEntry, FeedEntryStatus> status;
                    final Join<FeedEntry, FeedSubscription> subscription;

                    {
                        entry = from(FeedEntry.class);
                        var feed = entry.join(FeedEntry_.feed);
                        subscription = entry.join(FeedSubscription.class);
                        subscription.on(equal(subscription.get(FeedSubscription_.feed), feed));
                        status = entry.join(FeedEntry_.statuses, JoinType.LEFT);
                        status.on(equal(status.get(FeedEntryStatus_.subscription), subscription));
                        select(tuple(entry, status, subscription));

                        var autoMarkAsReadAfterDays = subscription.get(FeedSubscription_.autoMarkAsReadAfterDays);
                        var daysSincePublished =
                                durationByUnit(
                                        TemporalUnit.DAY, durationBetween(value(now), entry.get(FeedEntry_.published)));
                        where(
                                isNotNull(autoMarkAsReadAfterDays),
                                greaterThan(autoMarkAsReadAfterDays, 0),
                                ge(daysSincePublished, autoMarkAsReadAfterDays),
                                unreadPredicate(this, entry, status));
                    }
                };

        List<Tuple> tuples = query(criteria).setMaxResults(limit).getResultList();

        long updated = 0;

        // Update existing statuses
        List<Long> statusIdsToUpdate =
                tuples.stream()
                        .map(t -> t.get(criteria.status))
                        .filter(s -> s != null && s.getId() != null)
                        .map(FeedEntryStatus::getId)
                        .distinct()
                        .toList();

        if (!statusIdsToUpdate.isEmpty()) {
            updated += updateByIds(statusIdsToUpdate, update -> update.set(FeedEntryStatus_.read, true));
        }

        // Insert new statuses for entries without existing status
        for (Tuple tuple : tuples) {
            FeedEntryStatus status = tuple.get(criteria.status);
            if (status == null || status.getId() == null) {
                FeedEntry entry = tuple.get(criteria.entry);
                FeedSubscription sub = tuple.get(criteria.subscription);
                FeedEntryStatus newStatus = new FeedEntryStatus(sub.getUser(), sub, entry);
                newStatus.setRead(true);
                persist(newStatus);
                updated++;
            }
        }

        return updated;
    }
}
