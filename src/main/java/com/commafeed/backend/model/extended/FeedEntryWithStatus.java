package com.commafeed.backend.model.extended;

import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryStatus;

public class FeedEntryWithStatus {
	private FeedEntry entry;
	private FeedEntryStatus status;

	public FeedEntryWithStatus(FeedEntry entry, FeedEntryStatus status) {
		this.entry = entry;
		this.status = status;
	}

	public FeedEntry getEntry() {
		return entry;
	}

	public void setEntry(FeedEntry entry) {
		this.entry = entry;
	}

	public FeedEntryStatus getStatus() {
		return status;
	}

	public void setStatus(FeedEntryStatus status) {
		this.status = status;
	}

}
