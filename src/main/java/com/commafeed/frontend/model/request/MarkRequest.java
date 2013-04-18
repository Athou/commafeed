package com.commafeed.frontend.model.request;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.wordnik.swagger.annotations.ApiClass;
import com.wordnik.swagger.annotations.ApiProperty;

@SuppressWarnings("serial")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@ApiClass("Mark Request")
public class MarkRequest implements Serializable {

	@ApiProperty(value = "id", required = true)
	private String id;

	@ApiProperty(value = "mark as read or unread")
	private boolean read;

	@ApiProperty(value = "only entries older than this, prevent marking an entry that was not retrieved", required = false)
	private Long olderThan;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public Long getOlderThan() {
		return olderThan;
	}

	public void setOlderThan(Long olderThan) {
		this.olderThan = olderThan;
	}

}
