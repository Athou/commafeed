package com.commafeed.backend.dao;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.model.AbstractModel;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.FeedSubscription_;
import com.commafeed.backend.model.Models;
import com.commafeed.backend.model.User;

import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.JoinType;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostCommitInsertEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.query.criteria.CriteriaDefinition;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Singleton
public class FeedSubscriptionDAO extends GenericDAO<FeedSubscription> {

    public FeedSubscriptionDAO(EntityManager entityManager, CommaFeedConfiguration config) {
        super(entityManager, FeedSubscription.class, config);
    }

    public void onPostCommitInsert(Consumer<FeedSubscription> consumer) {
        getEntityManager()
                .unwrap(SharedSessionContractImplementor.class)
                .getFactory()
                .getServiceRegistry()
                .getService(EventListenerRegistry.class)
                .getEventListenerGroup(EventType.POST_COMMIT_INSERT)
                .appendListener(
                        new PostCommitInsertEventListener() {
                            @Override
                            public void onPostInsert(PostInsertEvent event) {
                                if (event.getEntity() instanceof FeedSubscription s) {
                                    consumer.accept(s);
                                }
                            }

                            @Override
                            public boolean requiresPostCommitHandling(EntityPersister persister) {
                                return true;
                            }

                            @Override
                            public void onPostInsertCommitFailed(PostInsertEvent event) {
                                // do nothing
                            }
                        });
    }

    public FeedSubscription findById(User user, Long id) {
        List<FeedSubscription> subs =
                query(
                                new CriteriaDefinition<>(getEntityManager(), FeedSubscription.class) {
                                    {
                                        var sub = from(FeedSubscription.class);
                                        sub.fetch(FeedSubscription_.feed, JoinType.LEFT);
                                        sub.fetch(FeedSubscription_.category, JoinType.LEFT);
                                        where(
                                                equal(sub.get(FeedSubscription_.user), user),
                                                equal(sub.get(FeedSubscription_.id), id));
                                    }
                                })
                        .getResultList();
        FeedSubscription sub = subs.stream().findFirst().orElse(null);
        return initRelations(sub);
    }

    public List<FeedSubscription> findByFeed(Feed feed) {
        return query(
                        new CriteriaDefinition<>(getEntityManager(), FeedSubscription.class) {
                            {
                                var sub = from(FeedSubscription.class);
                                where(equal(sub.get(FeedSubscription_.feed), feed));
                            }
                        })
                .getResultList();
    }

    public FeedSubscription findByFeed(User user, Feed feed) {
        List<FeedSubscription> subs =
                query(
                                new CriteriaDefinition<>(getEntityManager(), FeedSubscription.class) {
                                    {
                                        var sub = from(FeedSubscription.class);
                                        where(
                                                equal(sub.get(FeedSubscription_.user), user),
                                                equal(sub.get(FeedSubscription_.feed), feed));
                                    }
                                })
                        .getResultList();
        FeedSubscription sub = subs.stream().findFirst().orElse(null);
        return initRelations(sub);
    }

    public List<FeedSubscription> findAll(User user) {
        List<FeedSubscription> subs =
                query(
                                new CriteriaDefinition<>(getEntityManager(), FeedSubscription.class) {
                                    {
                                        var sub = from(FeedSubscription.class);
                                        sub.fetch(FeedSubscription_.feed, JoinType.LEFT);
                                        sub.fetch(FeedSubscription_.category, JoinType.LEFT);
                                        where(equal(sub.get(FeedSubscription_.user), user));
                                    }
                                })
                        .getResultList();
        return initRelations(subs);
    }

    public Long count(User user) {
        return query(
                        new CriteriaDefinition<>(getEntityManager(), Long.class) {
                            {
                                var sub = from(FeedSubscription.class);
                                select(count(sub));
                                where(equal(sub.get(FeedSubscription_.user), user));
                            }
                        })
                .getSingleResult();
    }

    public List<FeedSubscription> findByCategory(User user, FeedCategory category) {
        List<FeedSubscription> subs =
                query(
                                new CriteriaDefinition<>(getEntityManager(), FeedSubscription.class) {
                                    {
                                        var sub = from(FeedSubscription.class);
                                        var categoryPath = sub.get(FeedSubscription_.category);
                                        where(
                                                equal(sub.get(FeedSubscription_.user), user),
                                                category == null
                                                        ? isNull(categoryPath)
                                                        : equal(categoryPath, category));
                                    }
                                })
                        .getResultList();
        return initRelations(subs);
    }

    public List<FeedSubscription> findByCategories(User user, List<FeedCategory> categories) {
        Set<Long> categoryIds = categories.stream().map(AbstractModel::getId).collect(Collectors.toSet());
        return findAll(user).stream()
                .filter(s -> s.getCategory() != null && categoryIds.contains(s.getCategory().getId()))
                .toList();
    }

    private List<FeedSubscription> initRelations(List<FeedSubscription> list) {
        list.forEach(this::initRelations);
        return list;
    }

    private FeedSubscription initRelations(FeedSubscription sub) {
        if (sub != null) {
            Models.initialize(sub.getFeed());
            Models.initialize(sub.getCategory());
        }
        return sub;
    }
}
