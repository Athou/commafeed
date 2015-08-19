package com.commafeed.frontend.model.request;

import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@SuppressWarnings("serial")
@ApiModel("Feed merge Request")
@Data
public class FeedMergeRequest implements Serializable {

	@ApiModelProperty(value = "merge into this feed", required = true)
	private Long intoFeedId;

	@ApiModelProperty(value = "id of the feeds to merge", required = true)
	private List<Long> feedIds;

}
