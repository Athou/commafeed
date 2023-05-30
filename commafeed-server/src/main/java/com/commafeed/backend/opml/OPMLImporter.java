package com.commafeed.backend.opml;

import java.io.StringReader;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.commafeed.backend.cache.CacheService;
import com.commafeed.backend.dao.FeedCategoryDAO;
import com.commafeed.backend.feed.FeedUtils;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.User;
import com.commafeed.backend.service.FeedSubscriptionService;
import com.rometools.opml.feed.opml.Opml;
import com.rometools.opml.feed.opml.Outline;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.WireFeedInput;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
@Singleton
public class OPMLImporter {

	private final FeedCategoryDAO feedCategoryDAO;
	private final FeedSubscriptionService feedSubscriptionService;
	private final CacheService cache;

	public void importOpml(User user, String xml) throws IllegalArgumentException, FeedException {
		xml = xml.substring(xml.indexOf('<'));
		WireFeedInput input = new WireFeedInput();
		Opml feed = (Opml) input.build(new StringReader(xml));
		List<Outline> outlines = feed.getOutlines();
		for (int i = 0; i < outlines.size(); i++) {
			handleOutline(user, outlines.get(i), null, i);
		}
	}

	private void handleOutline(User user, Outline outline, FeedCategory parent, int position) {
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
				category.setPosition(position);
				feedCategoryDAO.saveOrUpdate(category);
			}

			for (int i = 0; i < children.size(); i++) {
				handleOutline(user, children.get(i), category, i);
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
				feedSubscriptionService.subscribe(user, outline.getXmlUrl(), name, parent, position);
			} catch (Exception e) {
				log.error("error while importing {}: {}", outline.getXmlUrl(), e.getMessage());
			}
		}
		cache.invalidateUserRootCategory(user);
	}
}
