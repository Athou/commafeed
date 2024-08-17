package com.commafeed.backend.feed;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.commafeed.CommaFeedConfiguration;

import jakarta.inject.Singleton;

@Singleton
public class FeedRefreshIntervalCalculator {

	private final Duration refreshInterval;
	private final boolean empiricalInterval;

	public FeedRefreshIntervalCalculator(CommaFeedConfiguration config) {
		this.refreshInterval = config.feedRefresh().interval();
		this.empiricalInterval = config.feedRefresh().intervalEmpirical();
	}

	public Instant onFetchSuccess(Instant publishedDate, Long averageEntryInterval) {
		Instant defaultRefreshInterval = getDefaultRefreshInterval();
		return empiricalInterval ? computeEmpiricalRefreshInterval(publishedDate, averageEntryInterval, defaultRefreshInterval)
				: defaultRefreshInterval;
	}

	public Instant onFeedNotModified(Instant publishedDate, Long averageEntryInterval) {
		return onFetchSuccess(publishedDate, averageEntryInterval);
	}

	public Instant onFetchError(int errorCount) {
		int retriesBeforeDisable = 3;
		if (errorCount < retriesBeforeDisable || !empiricalInterval) {
			return getDefaultRefreshInterval();
		}

		int disabledHours = Math.min(24 * 7, errorCount - retriesBeforeDisable + 1);
		return Instant.now().plus(Duration.ofHours(disabledHours));
	}

	private Instant getDefaultRefreshInterval() {
		return Instant.now().plus(refreshInterval);
	}

	private Instant computeEmpiricalRefreshInterval(Instant publishedDate, Long averageEntryInterval, Instant defaultRefreshInterval) {
		Instant now = Instant.now();

		if (publishedDate == null) {
			// feed with no entries, recheck in 24 hours
			return now.plus(Duration.ofHours(24));
		} else if (ChronoUnit.DAYS.between(publishedDate, now) >= 30) {
			// older than a month, recheck in 24 hours
			return now.plus(Duration.ofHours(24));
		} else if (ChronoUnit.DAYS.between(publishedDate, now) >= 14) {
			// older than two weeks, recheck in 12 hours
			return now.plus(Duration.ofHours(12));
		} else if (ChronoUnit.DAYS.between(publishedDate, now) >= 7) {
			// older than a week, recheck in 6 hours
			return now.plus(Duration.ofHours(6));
		} else if (averageEntryInterval != null) {
			// use average time between entries to decide when to refresh next, divided by factor
			int factor = 2;

			// not more than 6 hours
			long date = Math.min(now.plus(Duration.ofHours(6)).toEpochMilli(), now.toEpochMilli() + averageEntryInterval / factor);

			// not less than default refresh interval
			date = Math.max(defaultRefreshInterval.toEpochMilli(), date);

			return Instant.ofEpochMilli(date);
		} else {
			// unknown case, recheck in 24 hours
			return now.plus(Duration.ofHours(24));
		}
	}

}
