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
		// if newSession is false, we already are in a unit of work and roll back/commit will happen in the wrapping unit of work
		boolean newSession = !ManagedSessionContext.hasBind(sessionFactory);

		final Session session = newSession ? sessionFactory.openSession() : sessionFactory.getCurrentSession();
		T t = null;
		try {
			if (newSession) {
				ManagedSessionContext.bind(session);
				session.beginTransaction();
			}
			try {
				t = runInSession();
				if (newSession) {
					commitTransaction(session);
				}
			} catch (Exception e) {
				if (newSession) {
					rollbackTransaction(session);
				}
				this.<RuntimeException> rethrow(e);
			}
		} finally {
			if (newSession) {
				session.close();
				ManagedSessionContext.unbind(sessionFactory);
			}
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
