package com.commafeed.backend.dao;

import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.FeedEntryStatus_;

import jakarta.persistence.criteria.AbstractQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import lombok.experimental.UtilityClass;

import org.hibernate.query.criteria.HibernateCriteriaBuilder;

@UtilityClass
public class Predicates {

    public static Predicate isNotStarred(HibernateCriteriaBuilder cb, AbstractQuery<?> query, Path<FeedEntry> entry) {
        Subquery<Integer> subquery = query.subquery(Integer.class);
        Root<FeedEntryStatus> status = subquery.from(FeedEntryStatus.class);
        subquery.select(cb.literal(1))
                .where(
                        cb.equal(status.get(FeedEntryStatus_.entry), entry),
                        cb.isTrue(status.get(FeedEntryStatus_.starred)));
        return cb.not(cb.exists(subquery));
    }
}
