package com.commafeed.backend.dao;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
@Singleton
public class UnitOfWork {

	private final SessionFactory sessionFactory;

	public void run(SessionRunner sessionRunner) {
		call(() -> {
			sessionRunner.runInSession();
			return null;
		});
	}

	public <T> T call(SessionRunnerReturningValue<T> sessionRunner) {
		T t = null;

		boolean sessionAlreadyBound = ManagedSessionContext.hasBind(sessionFactory);
		try (Session session = sessionFactory.openSession()) {
			if (!sessionAlreadyBound) {
				ManagedSessionContext.bind(session);
			}

			Transaction tx = session.beginTransaction();
			try {
				t = sessionRunner.runInSession();
				commitTransaction(tx);
			} catch (Exception e) {
				rollbackTransaction(tx);
				UnitOfWork.rethrow(e);
			}
		} finally {
			if (!sessionAlreadyBound) {
				ManagedSessionContext.unbind(sessionFactory);
			}
		}

		return t;
	}

	private static void rollbackTransaction(Transaction tx) {
		if (tx != null && tx.isActive()) {
			tx.rollback();
		}
	}

	private static void commitTransaction(Transaction tx) {
		if (tx != null && tx.isActive()) {
			tx.commit();
		}
	}

	@SuppressWarnings("unchecked")
	private static <E extends Exception> void rethrow(Exception e) throws E {
		throw (E) e;
	}

	@FunctionalInterface
	public interface SessionRunner {
		void runInSession();
	}

	@FunctionalInterface
	public interface SessionRunnerReturningValue<T> {
		T runInSession();
	}

}
