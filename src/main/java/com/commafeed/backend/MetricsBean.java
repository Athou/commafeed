package com.commafeed.backend;

import javax.ejb.Singleton;

@Singleton
public class MetricsBean {

	private int feedsRefreshedLastMinute;
	private int feedsRefreshedThisMinute;

	private int feedsRefreshedLastHour;
	private int feedsRefreshedThisHour;

	private long minuteTimestamp;
	private long hourTimestamp;

	public void feedRefreshed() {
		long now = System.currentTimeMillis();
		if (now - minuteTimestamp > 60000) {
			feedsRefreshedLastMinute = feedsRefreshedThisMinute;
			feedsRefreshedThisMinute = 0;
			minuteTimestamp = now;

		}
		feedsRefreshedThisMinute++;

		if (now - hourTimestamp > 60000 * 60) {
			feedsRefreshedLastHour = feedsRefreshedThisHour;
			feedsRefreshedThisHour = 0;
			hourTimestamp = now;

		}
		feedsRefreshedThisHour++;
	}

	public int getFeedsRefreshedLastMinute() {
		return feedsRefreshedLastMinute;
	}

	public int getFeedsRefreshedLastHour() {
		return feedsRefreshedLastHour;
	}
}
