package com.commafeed.backend.model;

import java.sql.Types;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hibernate.annotations.JdbcTypeCode;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "FEEDENTRYCONTENTS")
@SuppressWarnings("serial")
@Getter
@Setter
public class FeedEntryContent extends AbstractModel {

	public enum Direction {
		@JsonProperty("ltr")
		LTR,

		@JsonProperty("rtl")
		RTL,

		@JsonProperty("unknown")
		UNKNOWN
	}

	@Column(length = 2048)
	private String title;

	@Column(length = 40)
	private String titleHash;

	@Lob
	@Column(length = Integer.MAX_VALUE)
	@JdbcTypeCode(Types.LONGVARCHAR)
	private String content;

	@Column(length = 40)
	private String contentHash;

	@Column(name = "author", length = 128)
	private String author;

	@Column(length = 2048)
	private String enclosureUrl;

	@Column(length = 255)
	private String enclosureType;

	@Lob
	@Column(length = Integer.MAX_VALUE)
	@JdbcTypeCode(Types.LONGVARCHAR)
	private String mediaDescription;

	@Column(length = 2048)
	private String mediaThumbnailUrl;

	private Integer mediaThumbnailWidth;
	private Integer mediaThumbnailHeight;

	@Column(length = 4096)
	private String categories;

	@Column
	@Enumerated(EnumType.STRING)
	private Direction direction = Direction.UNKNOWN;

	@OneToMany(mappedBy = "content")
	private Set<FeedEntry> entries;

	public boolean equivalentTo(FeedEntryContent c) {
		if (c == null) {
			return false;
		}

		return new EqualsBuilder().append(title, c.title)
				.append(content, c.content)
				.append(author, c.author)
				.append(categories, c.categories)
				.append(enclosureUrl, c.enclosureUrl)
				.append(enclosureType, c.enclosureType)
				.append(mediaDescription, c.mediaDescription)
				.append(mediaThumbnailUrl, c.mediaThumbnailUrl)
				.append(mediaThumbnailWidth, c.mediaThumbnailWidth)
				.append(mediaThumbnailHeight, c.mediaThumbnailHeight)
				.build();
	}
}
