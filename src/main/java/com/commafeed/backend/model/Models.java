package com.commafeed.backend.model;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

public class Models {

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
		if (model instanceof HibernateProxy) {
			LazyInitializer lazyInitializer = ((HibernateProxy) model).getHibernateLazyInitializer();
			if (lazyInitializer.isUninitialized()) {
				return (Long) lazyInitializer.getIdentifier();
			}
		}
		return model.getId();
	}
}
