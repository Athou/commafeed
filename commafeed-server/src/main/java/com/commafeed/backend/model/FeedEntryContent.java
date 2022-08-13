package com.commafeed.backend.model;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hibernate.annotations.Type;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "FEEDENTRYCONTENTS")
@SuppressWarnings("serial")
@Getter
@Setter
public class FeedEntryContent extends AbstractModel {

	@Column(length = 2048)
	private String title;

	@Column(length = 40)
	private String titleHash;

	@Lob
	@Column(length = Integer.MAX_VALUE)
	@Type(type = "org.hibernate.type.TextType")
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
	@Type(type = "org.hibernate.type.TextType")
	private String mediaDescription;

	@Column(length = 2048)
	private String mediaThumbnailUrl;

	private Integer mediaThumbnailWidth;
	private Integer mediaThumbnailHeight;

	@Column(length = 4096)
	private String categories;

	@OneToMany(mappedBy = "content")
	private Set<FeedEntry> entries;

	public boolean equivalentTo(FeedEntryContent c) {
		if (c == null) {
			return false;
		}

		return new EqualsBuilder().append(title, c.title)
				.append(content, c.content)
				.append(author, c.author)
				.append(enclosureUrl, c.enclosureUrl)
				.append(enclosureType, c.enclosureType)
				.append(mediaDescription, c.mediaDescription)
				.append(mediaThumbnailUrl, c.mediaThumbnailUrl)
				.append(mediaThumbnailWidth, c.mediaThumbnailWidth)
				.append(mediaThumbnailHeight, c.mediaThumbnailHeight)
				.append(categories, c.categories)
				.build();
	}

}
