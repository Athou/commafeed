package com.commafeed.frontend.model.request;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

import com.wordnik.swagger.annotations.ApiClass;
import com.wordnik.swagger.annotations.ApiProperty;

@SuppressWarnings("serial")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@ApiClass("Mark Request")
@Data
public class MarkRequest implements Serializable {

	@ApiProperty(value = "entry id, category id, 'all' or 'starred'", required = true)
	private String id;

	@ApiProperty(value = "feed id, only required when marking an entry")
	private Long feedId;

	@ApiProperty(value = "mark as read or unread")
	private boolean read;

	@ApiProperty(
			value = "only entries older than this, pass the timestamp you got from the entry list to prevent marking an entry that was not retrieved",
			required = false)
	private Long olderThan;

}
