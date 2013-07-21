package com.commafeed.backend.feeds;

import java.io.StringReader;
import java.util.List;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.cache.CacheService;
import com.commafeed.backend.dao.FeedCategoryDAO;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.User;
import com.commafeed.backend.services.FeedSubscriptionService;
import com.commafeed.backend.services.FeedSubscriptionService.FeedSubscriptionException;
import com.sun.syndication.feed.opml.Opml;
import com.sun.syndication.feed.opml.Outline;
import com.sun.syndication.io.WireFeedInput;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class OPMLImporter {

	private static Logger log = LoggerFactory.getLogger(OPMLImporter.class);

	@Inject
	FeedSubscriptionService feedSubscriptionService;

	@Inject
	FeedCategoryDAO feedCategoryDAO;

	@Inject
	CacheService cache;

	@SuppressWarnings("unchecked")
	@Asynchronous
	public void importOpml(User user, String xml) {
		xml = xml.substring(xml.indexOf('<'));
		WireFeedInput input = new WireFeedInput();
		try {
			Opml feed = (Opml) input.build(new StringReader(xml));
			List<Outline> outlines = (List<Outline>) feed.getOutlines();
			for (Outline outline : outlines) {
				handleOutline(user, outline, null);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

	}

	@SuppressWarnings("unchecked")
	private void handleOutline(User user, Outline outline, FeedCategory parent) {

		if (StringUtils.isEmpty(outline.getType())) {
			String name = FeedUtils.truncate(outline.getText(), 128);
			if (name == null) {
				name = FeedUtils.truncate(outline.getTitle(), 128);
			}
			FeedCategory category = feedCategoryDAO.findByName(user, name,
					parent);
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

			List<Outline> children = outline.getChildren();
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
			// make sure we continue with the import process even a feed failed
			try {
				feedSubscriptionService.subscribe(user, outline.getXmlUrl(),
						name, parent);
			} catch (FeedSubscriptionException e) {
				throw e;
			} catch (Exception e) {
				log.error("error while importing {}: {}", outline.getXmlUrl(),
						e.getMessage());
			}
		}
		cache.invalidateUserData(user);
	}
}
