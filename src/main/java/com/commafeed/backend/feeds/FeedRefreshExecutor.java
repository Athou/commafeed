package com.commafeed.backend.feeds;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FeedRefreshExecutor {

	private static Logger log = LoggerFactory
			.getLogger(FeedRefreshExecutor.class);

	private String poolName;
	private ThreadPoolExecutor pool;
	private LinkedBlockingDeque<Runnable> queue;

	public FeedRefreshExecutor(final String poolName, int threads,
			int queueCapacity) {
		log.info("Creating pool {} with {} threads", poolName, threads);
		this.poolName = poolName;
		pool = new ThreadPoolExecutor(threads, threads, 0,
				TimeUnit.MILLISECONDS,
				queue = new LinkedBlockingDeque<Runnable>(queueCapacity) {
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
				});
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
					log.error(poolName
							+ " interrupted while waiting for queue.", e1);
				}
			}
		});
		pool.setThreadFactory(new NamedThreadFactory(poolName));
	}

	public void execute(Task task) {
		pool.execute(task);
	}

	public int getQueueSize() {
		return queue.size();
	}

	public int getActiveCount() {
		return pool.getActiveCount();
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
				log.error(
						"{} interrupted while waiting for threads to finish.",
						poolName);
			}
		}
	}
	
	private static class NamedThreadFactory implements ThreadFactory {
		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;

		private NamedThreadFactory(String poolName) {
			SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() :
					Thread.currentThread().getThreadGroup();
			namePrefix = poolName + "-thread-";
		}

		public Thread newThread(Runnable r) {
			Thread t = new Thread(group, r,
					namePrefix + threadNumber.getAndIncrement(),
					0);
			if (t.isDaemon())
				t.setDaemon(false);
			if (t.getPriority() != Thread.NORM_PRIORITY)
				t.setPriority(Thread.NORM_PRIORITY);
			return t;
		}
	}
}
