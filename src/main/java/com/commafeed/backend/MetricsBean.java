package com.commafeed.backend;

import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.ejb.EntityManagerImpl;
import org.hibernate.stat.Statistics;

@Singleton
public class MetricsBean {

	@PersistenceContext
	EntityManager em;

	private Metric lastMinute = new Metric();
	private Metric thisMinute = new Metric();

	private Metric lastHour = new Metric();
	private Metric thisHour = new Metric();

	private long minuteTimestamp;
	private long hourTimestamp;

	public void feedRefreshed() {
		long now = System.currentTimeMillis();
		if (now - minuteTimestamp > 60000) {
			lastMinute = thisMinute;
			thisMinute = new Metric();
			minuteTimestamp = now;

		}
		thisMinute.feedsRefreshed++;

		if (now - hourTimestamp > 60000 * 60) {
			lastHour = thisHour;
			thisHour = new Metric();
			hourTimestamp = now;

		}
		thisHour.feedsRefreshed++;
	}

	public void feedUpdated() {
		thisHour.feedsUpdated++;
		thisMinute.feedsUpdated++;

	}

	public void entryUpdated(int statusesCount) {

		thisHour.entriesInserted++;
		thisMinute.entriesInserted++;

		thisHour.statusesInserted += statusesCount;
		thisMinute.statusesInserted += statusesCount;
	}

	public void threadWaited() {
		thisHour.threadWaited++;
		thisMinute.threadWaited++;
	}

	public Metric getLastMinute() {
		return lastMinute;
	}

	public Metric getLastHour() {
		return lastHour;
	}

	public String getCacheStats() {
		EntityManagerImpl impl = (EntityManagerImpl) em.getDelegate();
		Session session = impl.getSession();
		SessionFactory sessionFactory = session.getSessionFactory();
		Statistics statistics = sessionFactory.getStatistics();
		return statistics.toString();
	}

	public static class Metric {
		private int feedsRefreshed;
		private int feedsUpdated;
		private int entriesInserted;
		private int statusesInserted;
		private int threadWaited;

		public int getFeedsRefreshed() {
			return feedsRefreshed;
		}

		public void setFeedsRefreshed(int feedsRefreshed) {
			this.feedsRefreshed = feedsRefreshed;
		}

		public int getFeedsUpdated() {
			return feedsUpdated;
		}

		public void setFeedsUpdated(int feedsUpdated) {
			this.feedsUpdated = feedsUpdated;
		}

		public int getEntriesInserted() {
			return entriesInserted;
		}

		public void setEntriesInserted(int entriesInserted) {
			this.entriesInserted = entriesInserted;
		}

		public int getStatusesInserted() {
			return statusesInserted;
		}

		public void setStatusesInserted(int statusesInserted) {
			this.statusesInserted = statusesInserted;
		}

		public int getThreadWaited() {
			return threadWaited;
		}

		public void setThreadWaited(int threadWaited) {
			this.threadWaited = threadWaited;
		}

	}
}
