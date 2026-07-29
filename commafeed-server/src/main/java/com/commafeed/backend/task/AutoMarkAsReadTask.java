package com.commafeed.backend.task;

import com.commafeed.backend.service.db.DatabaseCleaningService;

import jakarta.inject.Singleton;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Singleton
public class AutoMarkAsReadTask extends ScheduledTask {

    private final DatabaseCleaningService cleaner;

    @Override
    public void run() {
        cleaner.autoMarkAsRead();
    }

    @Override
    public long getInitialDelay() {
        return 30;
    }

    @Override
    public long getPeriod() {
        return 60;
    }

    @Override
    public TimeUnit getTimeUnit() {
        return TimeUnit.MINUTES;
    }
}
