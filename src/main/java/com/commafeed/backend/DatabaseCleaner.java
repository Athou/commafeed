package com.commafeed.backend;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import com.commafeed.backend.dao.FeedEntryDAO;

public class DatabaseCleaner {

	@Inject
	FeedEntryDAO feedEntryDAO;

	public void cleanOlderThan(long value, TimeUnit unit) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, -1 * (int) unit.toMinutes(value));

		int deleted = -1;
		do {
			deleted = feedEntryDAO.delete(cal.getTime(), 1000);
		} while (deleted != 0);

	}
}
