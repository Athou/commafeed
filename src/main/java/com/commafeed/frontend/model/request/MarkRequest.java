package com.commafeed.frontend.model.request;

import java.io.Serializable;

import lombok.Data;

import com.wordnik.swagger.annotations.ApiClass;
import com.wordnik.swagger.annotations.ApiProperty;

@SuppressWarnings("serial")
@ApiClass("Mark Request")
@Data
public class MarkRequest implements Serializable {

	@ApiProperty(value = "entry id, category id, 'all' or 'starred'", required = true)
	private String id;

	@ApiProperty(value = "mark as read or unread")
	private boolean read;

	@ApiProperty(
			value = "only entries older than this, pass the timestamp you got from the entry list to prevent marking an entry that was not retrieved",
			required = false)
	private Long olderThan;

}
