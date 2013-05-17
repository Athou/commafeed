package com.commafeed.backend;

import javax.ejb.Singleton;

@Singleton
public class MetricsBean {

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
		thisHour.feedsRefreshed++;
		thisMinute.feedsUpdated++;
	}

	public Metric getLastMinute() {
		return lastMinute;
	}

	public Metric getLastHour() {
		return lastHour;
	}

	public static class Metric {
		private int feedsRefreshed;
		private int feedsUpdated;

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

	}
}
