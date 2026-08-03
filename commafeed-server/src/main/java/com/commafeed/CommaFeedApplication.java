package com.commafeed;

import com.commafeed.backend.feed.FeedRefreshEngine;
import com.commafeed.backend.feed.ImageProxyUrl;
import com.commafeed.backend.task.TaskScheduler;
import com.commafeed.security.password.PasswordConstraintValidator;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

import jakarta.enterprise.event.Observes;
import jakarta.inject.Singleton;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.SystemProperties;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class CommaFeedApplication {

    private final FeedRefreshEngine feedRefreshEngine;
    private final TaskScheduler taskScheduler;
    private final CommaFeedConfiguration config;

    public void start(@Observes StartupEvent ev) {
        log.info("starting up...");

        // disable entity expansion limits added in JDK24+ (#1961)
        // we already strip doctype declarations in XMLCleaner to prevent xxe attacks
        // we also already limit the size of feeds we download in HttpGetter
        System.setProperty(SystemProperties.JDK_XML_MAX_GENERAL_ENTITY_SIZE_LIMIT, "0");
        System.setProperty(SystemProperties.JDK_XML_TOTAL_ENTITY_SIZE_LIMIT, "0");

        PasswordConstraintValidator.setMinimumPasswordLength(
                config.users().minimumPasswordLength());

        if (config.imageProxyEnabled()) {
            ImageProxyUrl.generateKey();
        }

        feedRefreshEngine.start();
        taskScheduler.start();
    }

    public void stop(@Observes ShutdownEvent ev) {
        log.info("shutting down...");

        feedRefreshEngine.stop();
        taskScheduler.stop();
    }
}
