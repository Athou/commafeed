package com.commafeed.backend.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;

public abstract class UnitOfWork<T> {

	private SessionFactory sessionFactory;

	public UnitOfWork(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	protected abstract T runInSession() throws Exception;

	public T run() {
		final Session session = sessionFactory.openSession();
		if (ManagedSessionContext.hasBind(sessionFactory)) {
			throw new IllegalStateException("Already in a unit of work!");
		}
		T t = null;
		try {
			ManagedSessionContext.bind(session);
			session.beginTransaction();
			try {
				t = runInSession();
				commitTransaction(session);
			} catch (Exception e) {
				rollbackTransaction(session);
				this.<RuntimeException> rethrow(e);
			}
		} finally {
			session.close();
			ManagedSessionContext.unbind(sessionFactory);
		}
		return t;
	}

	private void rollbackTransaction(Session session) {
		final Transaction txn = session.getTransaction();
		if (txn != null && txn.isActive()) {
			txn.rollback();
		}
	}

	private void commitTransaction(Session session) {
		final Transaction txn = session.getTransaction();
		if (txn != null && txn.isActive()) {
			txn.commit();
		}
	}

	@SuppressWarnings("unchecked")
	private <E extends Exception> void rethrow(Exception e) throws E {
		throw (E) e;
	}

}
