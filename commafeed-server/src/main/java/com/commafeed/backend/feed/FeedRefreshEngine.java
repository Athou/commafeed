package com.commafeed.backend.feed;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import jakarta.inject.Singleton;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.dao.UnitOfWork;
import com.commafeed.backend.model.AbstractModel;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedSubscription;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class FeedRefreshEngine {

	private final UnitOfWork unitOfWork;
	private final FeedDAO feedDAO;
	private final FeedRefreshWorker worker;
	private final FeedRefreshUpdater updater;
	private final FeedUpdateNotifier notifier;
	private final CommaFeedConfiguration config;
	private final Meter refill;

	private final BlockingDeque<Feed> queue;

	private final ExecutorService feedProcessingLoopExecutor;
	private final ExecutorService refillLoopExecutor;
	private final ExecutorService refillExecutor;
	private final ThreadPoolExecutor workerExecutor;
	private final ThreadPoolExecutor databaseUpdaterExecutor;
	private final ThreadPoolExecutor notifierExecutor;

	public FeedRefreshEngine(UnitOfWork unitOfWork, FeedDAO feedDAO, FeedRefreshWorker worker, FeedRefreshUpdater updater,
			FeedUpdateNotifier notifier, CommaFeedConfiguration config, MetricRegistry metrics) {
		this.unitOfWork = unitOfWork;
		this.feedDAO = feedDAO;
		this.worker = worker;
		this.updater = updater;
		this.notifier = notifier;
		this.config = config;
		this.refill = metrics.meter(MetricRegistry.name(getClass(), "refill"));

		this.queue = new LinkedBlockingDeque<>();

		this.feedProcessingLoopExecutor = Executors.newSingleThreadExecutor();
		this.refillLoopExecutor = Executors.newSingleThreadExecutor();
		this.refillExecutor = newDiscardingSingleThreadExecutorService();
		this.workerExecutor = newBlockingExecutorService(config.feedRefresh().httpThreads());
		this.databaseUpdaterExecutor = newBlockingExecutorService(config.feedRefresh().databaseThreads());
		this.notifierExecutor = newDiscardingExecutorService(config.pushNotifications().threads(),
				config.pushNotifications().queueCapacity());

		metrics.register(MetricRegistry.name(getClass(), "queue", "size"), (Gauge<Integer>) queue::size);
		metrics.register(MetricRegistry.name(getClass(), "worker", "active"), (Gauge<Integer>) workerExecutor::getActiveCount);
		metrics.register(MetricRegistry.name(getClass(), "updater", "active"), (Gauge<Integer>) databaseUpdaterExecutor::getActiveCount);
		metrics.register(MetricRegistry.name(getClass(), "notifier", "active"), (Gauge<Integer>) notifierExecutor::getActiveCount);
		metrics.register(MetricRegistry.name(getClass(), "notifier", "queue"), (Gauge<Integer>) () -> notifierExecutor.getQueue().size());
	}

	public void start() {
		startFeedProcessingLoop();
		startRefillLoop();
	}

	private void startFeedProcessingLoop() {
		// take a feed from the queue, process it, rince, repeat
		feedProcessingLoopExecutor.submit(() -> {
			while (!feedProcessingLoopExecutor.isShutdown()) {
				try {
					// take() is blocking until a feed is available from the queue
					Feed feed = queue.take();

					// send the feed to be processed
					log.debug("got feed {} from the queue, send it for processing", feed.getId());
					processFeedAsync(feed);

					// we removed a feed from the queue, try to refill it as it may now be empty
					if (queue.isEmpty()) {
						log.debug("took the last feed from the queue, try to refill");
						refillQueueAsync();
					}
				} catch (InterruptedException e) {
					log.debug("interrupted while waiting for a feed in the queue");
					Thread.currentThread().interrupt();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		});
	}

	private void startRefillLoop() {
		// refill the queue at regular intervals if it's empty
		refillLoopExecutor.submit(() -> {
			while (!refillLoopExecutor.isShutdown()) {
				try {
					if (queue.isEmpty()) {
						log.debug("refilling queue");
						refillQueueAsync();
					}

					log.debug("sleeping for 15s");
					TimeUnit.SECONDS.sleep(15);
				} catch (InterruptedException e) {
					log.debug("interrupted while sleeping");
					Thread.currentThread().interrupt();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		});
	}

	public void refreshImmediately(Feed feed) {
		log.debug("add feed {} at the start of the queue", feed.getId());
		// remove the feed from the queue if it was already queued to avoid refreshing it twice
		queue.removeIf(f -> f.getId().equals(feed.getId()));
		queue.addFirst(feed);
	}

	private void refillQueueAsync() {
		CompletableFuture.runAsync(() -> {
			if (!queue.isEmpty()) {
				return;
			}

			refill.mark();

			List<Feed> nextUpdatableFeeds = getNextUpdatableFeeds(getBatchSize());
			log.debug("found {} feeds that are up for refresh", nextUpdatableFeeds.size());
			for (Feed feed : nextUpdatableFeeds) {
				// add the feed only if it was not already queued
				if (queue.stream().noneMatch(f -> f.getId().equals(feed.getId()))) {
					queue.addLast(feed);
				}
			}
		}, refillExecutor).whenComplete((data, ex) -> {
			if (ex != null) {
				log.error("error while refilling the queue", ex);
			}
		});
	}

	private void processFeedAsync(Feed feed) {
		CompletableFuture.supplyAsync(() -> worker.update(feed), workerExecutor)
				.thenApplyAsync(r -> updater.update(r.feed(), r.entries()), databaseUpdaterExecutor)
				.thenCompose(r -> {
					List<CompletableFuture<Void>> futures = r.insertedUnreadEntriesBySubscription().entrySet().stream().map(e -> {
						FeedSubscription sub = e.getKey();
						List<FeedEntry> entries = e.getValue();

						notifier.notifyOverWebsocket(sub, entries);
						return CompletableFuture.runAsync(() -> notifier.sendPushNotifications(sub, entries), notifierExecutor);
					}).toList();
					return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
				})
				.exceptionally(ex -> {
					log.error("error while processing feed {}", feed.getUrl(), ex);
					return null;
				});
	}

	private List<Feed> getNextUpdatableFeeds(int max) {
		return unitOfWork.call(() -> {
			Instant lastLoginThreshold = config.feedRefresh().userInactivityPeriod().isZero() ? null
					: Instant.now().minus(config.feedRefresh().userInactivityPeriod());
			List<Feed> feeds = feedDAO.findNextUpdatable(max, lastLoginThreshold);
			if (!feeds.isEmpty()) {
				// update disabledUntil to prevent feeds from being returned again by feedDAO.findNextUpdatable()
				Instant nextUpdateDate = Instant.now().plus(config.feedRefresh().interval());
				feedDAO.setDisabledUntil(feeds.stream().map(AbstractModel::getId).toList(), nextUpdateDate);
			}
			return feeds;
		});
	}

	private int getBatchSize() {
		return Math.min(100, 3 * config.feedRefresh().httpThreads());
	}

	public void stop() {
		this.feedProcessingLoopExecutor.shutdownNow();
		this.refillLoopExecutor.shutdownNow();
		this.refillExecutor.shutdownNow();
		this.workerExecutor.shutdownNow();
		this.databaseUpdaterExecutor.shutdownNow();
		this.notifierExecutor.shutdownNow();
	}

	/**
	 * returns an ExecutorService with a single thread that discards tasks if a task is already running
	 */
	private ThreadPoolExecutor newDiscardingSingleThreadExecutorService() {
		ThreadPoolExecutor pool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new SynchronousQueue<>());
		pool.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
		return pool;
	}

	/**
	 * returns an ExecutorService that discards tasks if the queue is full
	 */
	private ThreadPoolExecutor newDiscardingExecutorService(int threads, int queueCapacity) {
		ThreadPoolExecutor pool = new ThreadPoolExecutor(threads, threads, 0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<>(queueCapacity));
		pool.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
		return pool;
	}

	/**
	 * returns an ExecutorService that blocks submissions until a thread is available
	 */
	private ThreadPoolExecutor newBlockingExecutorService(int threads) {
		ThreadPoolExecutor pool = new ThreadPoolExecutor(threads, threads, 0L, TimeUnit.MILLISECONDS, new SynchronousQueue<>());
		pool.setRejectedExecutionHandler((r, e) -> {
			if (e.isShutdown()) {
				return;
			}

			try {
				e.getQueue().put(r);
			} catch (InterruptedException ex) {
				log.debug("interrupted while waiting for a slot in the queue.", ex);
				Thread.currentThread().interrupt();
			}
		});
		return pool;
	}
}
