package com.commafeed.backend.feed;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;

import lombok.extern.slf4j.Slf4j;

/**
 * Wraps a {@link ThreadPoolExecutor} instance. Blocks when queue is full instead of rejecting the task. Allow priority queueing by using
 * {@link Task} instead of {@link Runnable}
 * 
 */
@Slf4j
public class FeedRefreshExecutor {

	private String poolName;
	private ThreadPoolExecutor pool;
	private LinkedBlockingDeque<Runnable> queue;

	public FeedRefreshExecutor(final String poolName, int threads, int queueCapacity, MetricRegistry metrics) {
		log.info("Creating pool {} with {} threads", poolName, threads);
		this.poolName = poolName;
		pool = new ThreadPoolExecutor(threads, threads, 0, TimeUnit.MILLISECONDS, queue = new LinkedBlockingDeque<Runnable>(queueCapacity) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean offer(Runnable r) {
				Task task = (Task) r;
				if (task.isUrgent()) {
					return offerFirst(r);
				} else {
					return offerLast(r);
				}
			}
		}) {
			@Override
			protected void afterExecute(Runnable r, Throwable t) {
				if (t != null) {
					log.error("thread from pool {} threw a runtime exception", poolName, t);
				}
			}
		};
		pool.setRejectedExecutionHandler(new RejectedExecutionHandler() {
			@Override
			public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
				log.debug("{} thread queue full, waiting...", poolName);
				try {
					Task task = (Task) r;
					if (task.isUrgent()) {
						queue.putFirst(r);
					} else {
						queue.put(r);
					}
				} catch (InterruptedException e1) {
					log.error(poolName + " interrupted while waiting for queue.", e1);
				}
			}
		});

		metrics.register(MetricRegistry.name(getClass(), poolName, "active"), new Gauge<Integer>() {
			@Override
			public Integer getValue() {
				return pool.getActiveCount();
			}
		});

		metrics.register(MetricRegistry.name(getClass(), poolName, "pending"), new Gauge<Integer>() {
			@Override
			public Integer getValue() {
				return queue.size();
			}
		});
	}

	public void execute(Task task) {
		pool.execute(task);
	}

	public static interface Task extends Runnable {
		boolean isUrgent();
	}

	public void shutdown() {
		pool.shutdownNow();
		while (!pool.isTerminated()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				log.error("{} interrupted while waiting for threads to finish.", poolName);
			}
		}
	}
}
