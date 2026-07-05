package com.commafeed.backend.dao;

import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.inject.Singleton;
import java.util.concurrent.Callable;

@Singleton
public class UnitOfWork {

    public void run(Runnable runnable) {
        QuarkusTransaction.joiningExisting().run(runnable);
    }

    public <T> T call(Callable<T> callable) {
        return QuarkusTransaction.joiningExisting().call(callable);
    }
}
