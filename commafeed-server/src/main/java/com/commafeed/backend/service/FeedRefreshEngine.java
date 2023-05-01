package com.commafeed.backend.service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.SessionFactory;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.dao.UnitOfWork;
import com.commafeed.backend.feed.FeedRefreshUpdater;
import com.commafeed.backend.feed.FeedRefreshWorker;
import com.commafeed.backend.model.Feed;

import io.dropwizard.lifecycle.Managed;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class FeedRefreshEngine implements Managed {

	private final SessionFactory sessionFactory;
	private final FeedDAO feedDAO;
	private final FeedRefreshWorker worker;
	private final FeedRefreshUpdater updater;
	private final CommaFeedConfiguration config;
	private final Meter refill;

	private final PublishProcessor<Feed> priorityQueue;
	private Disposable flow;

	@Inject
	public FeedRefreshEngine(SessionFactory sessionFactory, FeedDAO feedDAO, FeedRefreshWorker worker, FeedRefreshUpdater updater,
			CommaFeedConfiguration config, MetricRegistry metrics) {
		this.sessionFactory = sessionFactory;
		this.feedDAO = feedDAO;
		this.worker = worker;
		this.updater = updater;
		this.config = config;
		this.refill = metrics.meter(MetricRegistry.name(getClass(), "refill"));
		this.priorityQueue = PublishProcessor.create();
	}

	@Override
	public void start() {
		Flowable<Feed> database = Flowable.fromCallable(() -> findNextUpdatableFeeds(getBatchSize(), getLastLoginThreshold()))
				.onErrorResumeNext(e -> {
					log.error("error while fetching next updatable feeds", e);
					return Flowable.empty();
				})
				// repeat query 15s after the flowable has been emptied
				// https://github.com/ReactiveX/RxJava/issues/448#issuecomment-233244964
				.repeatWhen(o -> o.concatMap(v -> Flowable.timer(15, TimeUnit.SECONDS)))
				.flatMap(Flowable::fromIterable);
		Flowable<Feed> source = Flowable.merge(priorityQueue, database);

		this.flow = source.subscribeOn(Schedulers.io())
				// feed fetching
				.parallel(config.getApplicationSettings().getBackgroundThreads())
				.runOn(Schedulers.io())
				.flatMap(f -> Flowable.fromCallable(() -> worker.update(f)).onErrorResumeNext(e -> {
					log.error("error while fetching feed", e);
					return Flowable.empty();
				}))
				.sequential()
				// database updating
				.parallel(config.getApplicationSettings().getDatabaseUpdateThreads())
				.runOn(Schedulers.io())
				.flatMap(fae -> Flowable.fromCallable(() -> updater.update(fae.getFeed(), fae.getEntries())).onErrorResumeNext(e -> {
					log.error("error while updating database", e);
					return Flowable.empty();
				}))
				.sequential()
				// end flow
				.subscribe();
	}

	public void refreshImmediately(Feed feed) {
		priorityQueue.onNext(feed);
	}

	private List<Feed> findNextUpdatableFeeds(int max, Date lastLoginThreshold) {
		refill.mark();

		return UnitOfWork.call(sessionFactory, () -> {
			List<Feed> list = feedDAO.findNextUpdatable(max, lastLoginThreshold);

			// set the disabledDate as we use it in feedDAO.findNextUpdatable() to decide what to refresh next
			Date nextRefreshDate = DateUtils.addMinutes(new Date(), config.getApplicationSettings().getRefreshIntervalMinutes());
			list.forEach(f -> f.setDisabledUntil(nextRefreshDate));
			feedDAO.saveOrUpdate(list);

			return list;
		});
	}

	private int getBatchSize() {
		return Math.min(Flowable.bufferSize(), 3 * config.getApplicationSettings().getBackgroundThreads());
	}

	private Date getLastLoginThreshold() {
		return Boolean.TRUE.equals(config.getApplicationSettings().getHeavyLoad()) ? DateUtils.addDays(new Date(), -30) : null;
	}

	@Override
	public void stop() {
		flow.dispose();
	}
}
