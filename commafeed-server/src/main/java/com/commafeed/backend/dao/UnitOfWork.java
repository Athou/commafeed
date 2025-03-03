package com.commafeed.backend.dao;

import java.util.concurrent.Callable;

import jakarta.inject.Singleton;

import io.quarkus.narayana.jta.QuarkusTransaction;

@Singleton
public class UnitOfWork {

	public void run(Runnable runnable) {
		QuarkusTransaction.joiningExisting().run(runnable);
	}

	public <T> T call(Callable<T> callable) {
		return QuarkusTransaction.joiningExisting().call(callable);
	}
}
