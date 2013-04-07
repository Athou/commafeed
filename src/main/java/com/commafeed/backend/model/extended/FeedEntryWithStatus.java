package com.commafeed.backend.model.extended;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryStatus;

public class FeedEntryWithStatus {
	private FeedEntry entry;
	private FeedEntryStatus status;

	public FeedEntryWithStatus(FeedEntry entry, FeedEntryStatus status) {
		this.entry = entry;
		this.status = status;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(entry.getId())
				.append(status == null ? null : status.getId()).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		FeedEntryWithStatus rhs = (FeedEntryWithStatus) obj;
		return new EqualsBuilder()
				.append(status == null ? null : status.getId(),
						rhs.status == null ? null : rhs.status.getId())
				.append(entry.getId(), rhs.entry.getId()).isEquals();

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
