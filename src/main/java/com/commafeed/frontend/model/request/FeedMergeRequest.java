package com.commafeed.frontend.model.request;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.wordnik.swagger.annotations.ApiClass;
import com.wordnik.swagger.annotations.ApiProperty;

@SuppressWarnings("serial")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@ApiClass("Feed merge Request")
public class FeedMergeRequest implements Serializable {

	@ApiProperty(value = "merge into this feed", required = true)
	private Long intoFeedId;

	@ApiProperty(value = "id of the feeds to merge", required = true)
	private List<Long> feedIds;

	public Long getIntoFeedId() {
		return intoFeedId;
	}

	public void setIntoFeedId(Long intoFeedId) {
		this.intoFeedId = intoFeedId;
	}

	public List<Long> getFeedIds() {
		return feedIds;
	}

	public void setFeedIds(List<Long> feedIds) {
		this.feedIds = feedIds;
	}

}
