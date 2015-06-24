package com.commafeed.backend.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;

public class UnitOfWork {

	@FunctionalInterface
	public static interface SessionRunner {
		public void runInSession();
	}

	@FunctionalInterface
	public static interface SessionRunnerReturningValue<T> {
		public T runInSession();
	}

	public static void run(SessionFactory sessionFactory, SessionRunner sessionRunner) {
		call(sessionFactory, () -> {
			sessionRunner.runInSession();
			return null;
		});
	}

	public static <T> T call(SessionFactory sessionFactory, SessionRunnerReturningValue<T> sessionRunner) {
		final Session session = sessionFactory.openSession();
		if (ManagedSessionContext.hasBind(sessionFactory)) {
			throw new IllegalStateException("Already in a unit of work!");
		}
		T t = null;
		try {
			ManagedSessionContext.bind(session);
			session.beginTransaction();
			try {
				t = sessionRunner.runInSession();
				commitTransaction(session);
			} catch (Exception e) {
				rollbackTransaction(session);
				UnitOfWork.<RuntimeException> rethrow(e);
			}
		} finally {
			session.close();
			ManagedSessionContext.unbind(sessionFactory);
		}
		return t;
	}

	private static void rollbackTransaction(Session session) {
		final Transaction txn = session.getTransaction();
		if (txn != null && txn.isActive()) {
			txn.rollback();
		}
	}

	private static void commitTransaction(Session session) {
		final Transaction txn = session.getTransaction();
		if (txn != null && txn.isActive()) {
			txn.commit();
		}
	}

	@SuppressWarnings("unchecked")
	private static <E extends Exception> void rethrow(Exception e) throws E {
		throw (E) e;
	}

}
