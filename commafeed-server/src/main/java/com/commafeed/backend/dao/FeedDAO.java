package com.commafeed.backend.dao;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.FeedSubscription_;
import com.commafeed.backend.model.Feed_;
import com.commafeed.backend.model.User_;

import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;

import org.apache.commons.lang3.Strings;
import org.hibernate.query.criteria.CriteriaDefinition;

import java.time.Instant;
import java.util.List;

@Singleton
public class FeedDAO extends GenericDAO<Feed> {

    public FeedDAO(EntityManager entityManager, CommaFeedConfiguration config) {
        super(entityManager, Feed.class, config);
    }

    public List<Feed> findByIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }

        return query(
                        new CriteriaDefinition<>(getEntityManager(), Feed.class) {
                            {
                                var feed = from(Feed.class);
                                where(feed.get(Feed_.id).in(ids));
                            }
                        })
                .getResultList();
    }

    public List<Feed> findNextUpdatable(int count, Instant lastLoginThreshold) {
        return query(
                        new CriteriaDefinition<>(getEntityManager(), Feed.class) {
                            {
                                var feed = from(Feed.class);
                                distinct(true);

                                // join on subscriptions to only refresh feeds that have subscribers
                                var subscription = feed.join(FeedSubscription.class);
                                subscription.on(equal(subscription.get(FeedSubscription_.feed), feed));

                                var disabledUntil = feed.get(Feed_.disabledUntil);
                                where(or(isNull(disabledUntil), lessThan(disabledUntil, Instant.now())));

                                if (lastLoginThreshold != null) {
                                    var user = subscription.join(FeedSubscription_.user);
                                    restrict(greaterThan(user.get(User_.lastLogin), lastLoginThreshold));
                                }

                                orderBy(asc(disabledUntil));
                            }
                        })
                .setMaxResults(count)
                .getResultList();
    }

    public void setDisabledUntil(List<Long> feedIds, Instant date) {
        if (feedIds.isEmpty()) {
            return;
        }

        updateByIds(feedIds, update -> update.set(Feed_.disabledUntil, date));
    }

    public Feed findByUrl(String normalizedUrl, String normalizedUrlHash) {
        return query(
                        new CriteriaDefinition<>(getEntityManager(), Feed.class) {
                            {
                                var feed = from(Feed.class);
                                where(equal(feed.get(Feed_.normalizedUrlHash), normalizedUrlHash));
                            }
                        })
                .getResultList()
                .stream()
                .filter(f -> Strings.CS.equals(normalizedUrl, f.getNormalizedUrl()))
                .findFirst()
                .orElse(null);
    }

    public List<Feed> findWithoutSubscriptions(int max) {
        return query(
                        new CriteriaDefinition<>(getEntityManager(), Feed.class) {
                            {
                                var feed = from(Feed.class);
                                var subquery = subquery(Integer.class);
                                var subscription = subquery.from(FeedSubscription.class);
                                subquery.select(literal(1))
                                        .where(equal(subscription.get(FeedSubscription_.feed), feed));
                                where(not(exists(subquery)));
                            }
                        })
                .setMaxResults(max)
                .getResultList();
    }
}
