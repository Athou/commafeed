package com.commafeed.backend.opml;

import java.io.StringReader;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.commafeed.backend.cache.CacheService;
import com.commafeed.backend.dao.FeedCategoryDAO;
import com.commafeed.backend.feed.FeedUtils;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.User;
import com.commafeed.backend.service.FeedSubscriptionService;
import com.commafeed.backend.service.FeedSubscriptionService.FeedSubscriptionException;
import com.sun.syndication.feed.opml.Opml;
import com.sun.syndication.feed.opml.Outline;
import com.sun.syndication.io.WireFeedInput;

@Slf4j
public class OPMLImporter {

	private FeedCategoryDAO feedCategoryDAO;
	private FeedSubscriptionService feedSubscriptionService;
	private CacheService cache;

	public OPMLImporter(FeedCategoryDAO feedCategoryDAO, FeedSubscriptionService feedSubscriptionService, CacheService cache) {
		super();
		this.feedCategoryDAO = feedCategoryDAO;
		this.feedSubscriptionService = feedSubscriptionService;
		this.cache = cache;
	}

	@SuppressWarnings("unchecked")
	public void importOpml(User user, String xml) {
		xml = xml.substring(xml.indexOf('<'));
		WireFeedInput input = new WireFeedInput();
		try {
			Opml feed = (Opml) input.build(new StringReader(xml));
			List<Outline> outlines = feed.getOutlines();
			for (Outline outline : outlines) {
				handleOutline(user, outline, null);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

	}

	@SuppressWarnings("unchecked")
	private void handleOutline(User user, Outline outline, FeedCategory parent) {
		List<Outline> children = outline.getChildren();
		if (CollectionUtils.isNotEmpty(children)) {
			String name = FeedUtils.truncate(outline.getText(), 128);
			if (name == null) {
				name = FeedUtils.truncate(outline.getTitle(), 128);
			}
			FeedCategory category = feedCategoryDAO.findByName(user, name, parent);
			if (category == null) {
				if (StringUtils.isBlank(name)) {
					name = "Unnamed category";
				}

				category = new FeedCategory();
				category.setName(name);
				category.setParent(parent);
				category.setUser(user);
				feedCategoryDAO.saveOrUpdate(category);
			}

			for (Outline child : children) {
				handleOutline(user, child, category);
			}
		} else {
			String name = FeedUtils.truncate(outline.getText(), 128);
			if (name == null) {
				name = FeedUtils.truncate(outline.getTitle(), 128);
			}
			if (StringUtils.isBlank(name)) {
				name = "Unnamed subscription";
			}
			// make sure we continue with the import process even if a feed failed
			try {
				feedSubscriptionService.subscribe(user, outline.getXmlUrl(), name, parent);
			} catch (FeedSubscriptionException e) {
				throw e;
			} catch (Exception e) {
				log.error("error while importing {}: {}", outline.getXmlUrl(), e.getMessage());
			}
		}
		cache.invalidateUserRootCategory(user);
	}
}
