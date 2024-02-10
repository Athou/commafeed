package com.commafeed.backend.model;

import java.time.Duration;
import java.time.Instant;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Models {

	public static final Instant MINIMUM_INSTANT = Instant.EPOCH
			// mariadb timestamp range starts at 1970-01-01 00:00:01
			.plusSeconds(1)
			// make sure the timestamp fits for all timezones
			.plus(Duration.ofHours(24));

	/**
	 * initialize a proxy
	 */
	public static void initialize(Object proxy) throws HibernateException {
		Hibernate.initialize(proxy);
	}

	/**
	 * extract the id from the proxy without initializing it
	 */
	public static Long getId(AbstractModel model) {
		if (model instanceof HibernateProxy proxy) {
			LazyInitializer lazyInitializer = proxy.getHibernateLazyInitializer();
			if (lazyInitializer.isUninitialized()) {
				return (Long) lazyInitializer.getIdentifier();
			}
		}
		return model.getId();
	}
}
