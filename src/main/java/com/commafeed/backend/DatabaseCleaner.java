package com.commafeed.backend;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import com.commafeed.backend.dao.FeedEntryStatusDAO;

public class DatabaseCleaner {

	@Inject
	FeedEntryStatusDAO feedEntryStatusDAO;

	public long cleanOlderThan(long value, TimeUnit unit) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, -1 * (int) unit.toMinutes(value));

		long total = 0;
		int deleted = -1;
		do {
			deleted = feedEntryStatusDAO.delete(cal.getTime(), 100);
			total += deleted;
		} while (deleted != 0);
		return total;
	}
}
