package com.commafeed.backend.feed;

import java.time.Duration;
import java.time.Instant;
import java.time.InstantSource;
import java.time.temporal.ChronoUnit;

import org.apache.commons.lang3.ObjectUtils;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.CommaFeedConfiguration.FeedRefreshErrorHandling;
import com.google.common.primitives.Longs;

import jakarta.inject.Singleton;

@Singleton
public class FeedRefreshIntervalCalculator {

	private final Duration interval;
	private final Duration maxInterval;
	private final boolean empirical;
	private final FeedRefreshErrorHandling errorHandling;
	private final InstantSource instantSource;

	public FeedRefreshIntervalCalculator(CommaFeedConfiguration config, InstantSource instantSource) {
		this.interval = config.feedRefresh().interval();
		this.maxInterval = config.feedRefresh().maxInterval();
		this.empirical = config.feedRefresh().intervalEmpirical();
		this.errorHandling = config.feedRefresh().errors();
		this.instantSource = instantSource;
	}

	public Instant onFetchSuccess(Instant publishedDate, Long averageEntryInterval, Duration validFor) {
		Instant instant = empirical ? computeEmpiricalRefreshInterval(publishedDate, averageEntryInterval)
				: instantSource.instant().plus(interval);
		return constrainToBounds(ObjectUtils.max(instant, instantSource.instant().plus(validFor)));
	}

	public Instant onFeedNotModified(Instant publishedDate, Long averageEntryInterval) {
		return onFetchSuccess(publishedDate, averageEntryInterval, Duration.ZERO);
	}

	public Instant onTooManyRequests(Instant retryAfter, int errorCount) {
		return constrainToBounds(ObjectUtils.max(retryAfter, onFetchError(errorCount)));
	}

	public Instant onFetchError(int errorCount) {
		if (errorCount < errorHandling.retriesBeforeBackoff()) {
			return constrainToBounds(instantSource.instant().plus(interval));
		}

		Duration retryInterval = errorHandling.backoffInterval().multipliedBy(errorCount - errorHandling.retriesBeforeBackoff() + 1L);
		return constrainToBounds(instantSource.instant().plus(retryInterval));
	}

	private Instant computeEmpiricalRefreshInterval(Instant publishedDate, Long averageEntryInterval) {
		Instant now = instantSource.instant();

		if (publishedDate == null) {
			return now.plus(maxInterval);
		}

		long daysSinceLastPublication = ChronoUnit.DAYS.between(publishedDate, now);
		if (daysSinceLastPublication >= 30) {
			return now.plus(maxInterval);
		} else if (daysSinceLastPublication >= 14) {
			return now.plus(maxInterval.dividedBy(2));
		} else if (daysSinceLastPublication >= 7) {
			return now.plus(maxInterval.dividedBy(4));
		} else if (averageEntryInterval != null) {
			// use average time between entries to decide when to refresh next, divided by factor
			int factor = 2;
			long millis = Longs.constrainToRange(averageEntryInterval / factor, interval.toMillis(), maxInterval.dividedBy(4).toMillis());
			return now.plusMillis(millis);
		} else {
			// unknown case
			return now.plus(maxInterval);
		}
	}

	private Instant constrainToBounds(Instant instant) {
		return ObjectUtils.max(ObjectUtils.min(instant, instantSource.instant().plus(maxInterval)), instantSource.instant().plus(interval));
	}
}
