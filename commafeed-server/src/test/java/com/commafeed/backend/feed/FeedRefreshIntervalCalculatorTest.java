package com.commafeed.backend.feed;

import java.time.Duration;
import java.time.Instant;
import java.time.InstantSource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.CommaFeedConfiguration.FeedRefreshErrorHandling;

@ExtendWith(MockitoExtension.class)
class FeedRefreshIntervalCalculatorTest {

	private static final Instant NOW = Instant.now();
	private static final Duration DEFAULT_INTERVAL = Duration.ofHours(1);
	private static final Duration MAX_INTERVAL = Duration.ofDays(1);

	@Mock
	private InstantSource instantSource;

	@Mock
	private CommaFeedConfiguration config;

	@Mock
	private FeedRefreshErrorHandling errorHandling;

	private FeedRefreshIntervalCalculator calculator;

	@BeforeEach
	void setUp() {
		Mockito.when(instantSource.instant()).thenReturn(NOW);
		Mockito.when(config.feedRefresh()).thenReturn(Mockito.mock(CommaFeedConfiguration.FeedRefresh.class));
		Mockito.when(config.feedRefresh().interval()).thenReturn(DEFAULT_INTERVAL);
		Mockito.when(config.feedRefresh().maxInterval()).thenReturn(MAX_INTERVAL);
		Mockito.when(config.feedRefresh().errors()).thenReturn(errorHandling);

		calculator = new FeedRefreshIntervalCalculator(config, instantSource);
	}

	@Nested
	class FetchSuccess {

		@Nested
		class EmpiricalDisabled {
			@ParameterizedTest
			@ValueSource(longs = { 0, 1, 300, 86400000L })
			void withoutValidFor(long averageEntryInterval) {
				// averageEntryInterval is ignored when empirical is disabled
				Instant result = calculator.onFetchSuccess(NOW.minus(Duration.ofDays(5)), averageEntryInterval, Duration.ZERO);
				Assertions.assertEquals(NOW.plus(DEFAULT_INTERVAL), result);
			}

			@Test
			void withValidForGreaterThanMaxInterval() {
				Instant result = calculator.onFetchSuccess(NOW.minus(Duration.ofDays(5)), 1L, MAX_INTERVAL.plusDays(1));
				Assertions.assertEquals(NOW.plus(MAX_INTERVAL), result);
			}

			@Test
			void withValidForLowerThanMaxInterval() {
				Instant result = calculator.onFetchSuccess(NOW.minus(Duration.ofDays(5)), 1L, MAX_INTERVAL.minusSeconds(1));
				Assertions.assertEquals(NOW.plus(MAX_INTERVAL).minusSeconds(1), result);
			}
		}

		@Nested
		class EmpiricalEnabled {
			@BeforeEach
			void setUp() {
				Mockito.when(config.feedRefresh().intervalEmpirical()).thenReturn(true);
				calculator = new FeedRefreshIntervalCalculator(config, instantSource);
			}

			@Test
			void withNullPublishedDate() {
				Instant result = calculator.onFetchSuccess(null, 1L, Duration.ZERO);
				Assertions.assertEquals(NOW.plus(MAX_INTERVAL), result);
			}

			@Test
			void with31DaysOldPublishedDate() {
				Instant result = calculator.onFetchSuccess(NOW.minus(Duration.ofDays(31)), 1L, Duration.ZERO);
				Assertions.assertEquals(NOW.plus(MAX_INTERVAL), result);
			}

			@Test
			void with15DaysOldPublishedDate() {
				Instant result = calculator.onFetchSuccess(NOW.minus(Duration.ofDays(15)), 1L, Duration.ZERO);
				Assertions.assertEquals(NOW.plus(MAX_INTERVAL.dividedBy(2)), result);
			}

			@Test
			void with8DaysOldPublishedDate() {
				Instant result = calculator.onFetchSuccess(NOW.minus(Duration.ofDays(8)), 1L, Duration.ZERO);
				Assertions.assertEquals(NOW.plus(MAX_INTERVAL.dividedBy(4)), result);
			}

			@Nested
			class FiveDaysOld {
				@Test
				void averageBetweenBounds() {
					Instant result = calculator.onFetchSuccess(NOW.minus(Duration.ofDays(5)), Duration.ofHours(4).toMillis(),
							Duration.ZERO);
					Assertions.assertEquals(NOW.plus(Duration.ofHours(2)), result);
				}

				@Test
				void averageBelowMinimum() {
					Instant result = calculator.onFetchSuccess(NOW.minus(Duration.ofDays(5)), 10L, Duration.ZERO);
					Assertions.assertEquals(NOW.plus(DEFAULT_INTERVAL), result);
				}

				@Test
				void averageAboveMaximum() {
					Instant result = calculator.onFetchSuccess(NOW.minus(Duration.ofDays(5)), Long.MAX_VALUE, Duration.ZERO);
					Assertions.assertEquals(NOW.plus(MAX_INTERVAL.dividedBy(4)), result);
				}

				@Test
				void noAverage() {
					Instant result = calculator.onFetchSuccess(NOW.minus(Duration.ofDays(5)), null, Duration.ZERO);
					Assertions.assertEquals(NOW.plus(MAX_INTERVAL), result);
				}
			}
		}
	}

	@Nested
	class FeedNotModified {

		@Nested
		class EmpiricalDisabled {
			@ParameterizedTest
			@ValueSource(longs = { 0, 1, 300, 86400000L })
			void withoutValidFor(long averageEntryInterval) {
				// averageEntryInterval is ignored when empirical is disabled
				Instant result = calculator.onFeedNotModified(NOW.minus(Duration.ofDays(5)), averageEntryInterval);
				Assertions.assertEquals(NOW.plus(DEFAULT_INTERVAL), result);
			}
		}

		@Nested
		class EmpiricalEnabled {
			@BeforeEach
			void setUp() {
				Mockito.when(config.feedRefresh().intervalEmpirical()).thenReturn(true);
				calculator = new FeedRefreshIntervalCalculator(config, instantSource);
			}

			@Test
			void withNullPublishedDate() {
				Instant result = calculator.onFeedNotModified(null, 1L);
				Assertions.assertEquals(NOW.plus(MAX_INTERVAL), result);
			}

			@Test
			void with31DaysOldPublishedDate() {
				Instant result = calculator.onFeedNotModified(NOW.minus(Duration.ofDays(31)), 1L);
				Assertions.assertEquals(NOW.plus(MAX_INTERVAL), result);
			}

			@Test
			void with15DaysOldPublishedDate() {
				Instant result = calculator.onFeedNotModified(NOW.minus(Duration.ofDays(15)), 1L);
				Assertions.assertEquals(NOW.plus(MAX_INTERVAL.dividedBy(2)), result);
			}

			@Test
			void with8DaysOldPublishedDate() {
				Instant result = calculator.onFeedNotModified(NOW.minus(Duration.ofDays(8)), 1L);
				Assertions.assertEquals(NOW.plus(MAX_INTERVAL.dividedBy(4)), result);
			}

			@Nested
			class FiveDaysOld {
				@Test
				void averageBetweenBounds() {
					Instant result = calculator.onFeedNotModified(NOW.minus(Duration.ofDays(5)), Duration.ofHours(4).toMillis());
					Assertions.assertEquals(NOW.plus(Duration.ofHours(2)), result);
				}

				@Test
				void averageBelowMinimum() {
					Instant result = calculator.onFeedNotModified(NOW.minus(Duration.ofDays(5)), 10L);
					Assertions.assertEquals(NOW.plus(DEFAULT_INTERVAL), result);
				}

				@Test
				void averageAboveMaximum() {
					Instant result = calculator.onFeedNotModified(NOW.minus(Duration.ofDays(5)), Long.MAX_VALUE);
					Assertions.assertEquals(NOW.plus(MAX_INTERVAL.dividedBy(4)), result);
				}

				@Test
				void noAverage() {
					Instant result = calculator.onFeedNotModified(NOW.minus(Duration.ofDays(5)), null);
					Assertions.assertEquals(NOW.plus(MAX_INTERVAL), result);
				}
			}
		}
	}

	@Nested
	class FetchError {
		@BeforeEach
		void setUp() {
			Mockito.when(config.feedRefresh().errors().retriesBeforeBackoff()).thenReturn(3);
		}

		@Test
		void lowErrorCount() {
			Instant result = calculator.onFetchError(1);
			Assertions.assertEquals(NOW.plus(DEFAULT_INTERVAL), result);
		}

		@Test
		void highErrorCount() {
			Mockito.when(config.feedRefresh().errors().backoffInterval()).thenReturn(Duration.ofHours(1));

			Instant result = calculator.onFetchError(5);
			Assertions.assertEquals(NOW.plus(Duration.ofHours(3)), result);
		}

		@Test
		void veryHighErrorCount() {
			Mockito.when(config.feedRefresh().errors().backoffInterval()).thenReturn(Duration.ofHours(1));

			Instant result = calculator.onFetchError(100000);
			Assertions.assertEquals(NOW.plus(MAX_INTERVAL), result);
		}
	}

	@Nested
	class TooManyRequests {

		@BeforeEach
		void setUp() {
			Mockito.when(config.feedRefresh().errors().retriesBeforeBackoff()).thenReturn(3);
		}

		@Test
		void withRetryAfterZero() {
			Instant result = calculator.onTooManyRequests(NOW, 1);
			Assertions.assertEquals(NOW.plus(DEFAULT_INTERVAL), result);
		}

		@Test
		void withRetryAfterLowerThanInterval() {
			Instant retryAfter = NOW.plus(DEFAULT_INTERVAL.minusSeconds(10));
			Instant result = calculator.onTooManyRequests(retryAfter, 1);
			Assertions.assertEquals(NOW.plus(DEFAULT_INTERVAL), result);
		}

		@Test
		void withRetryAfterBetweenBounds() {
			Instant retryAfter = NOW.plus(DEFAULT_INTERVAL.plusSeconds(10));
			Instant result = calculator.onTooManyRequests(retryAfter, 1);
			Assertions.assertEquals(retryAfter, result);
		}

		@Test
		void withRetryAfterGreaterThanMaxInterval() {
			Instant retryAfter = NOW.plus(MAX_INTERVAL.plusSeconds(10));
			Instant result = calculator.onTooManyRequests(retryAfter, 1);
			Assertions.assertEquals(NOW.plus(MAX_INTERVAL), result);
		}
	}
}