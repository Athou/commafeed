package com.commafeed.backend.opml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.commafeed.backend.dao.FeedCategoryDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.User;
import com.rometools.opml.feed.opml.Opml;
import com.rometools.opml.feed.opml.Outline;

@ExtendWith(MockitoExtension.class)
class OPMLExporterTest {

	@Mock
	private FeedCategoryDAO feedCategoryDAO;
	@Mock
	private FeedSubscriptionDAO feedSubscriptionDAO;

	private final User user = new User();

	private final FeedCategory cat1 = new FeedCategory();
	private final FeedCategory cat2 = new FeedCategory();

	private final FeedSubscription rootFeed = newFeedSubscription("rootFeed", "rootFeed.com");
	private final FeedSubscription cat1Feed = newFeedSubscription("cat1Feed", "cat1Feed.com");
	private final FeedSubscription cat2Feed = newFeedSubscription("cat2Feed", "cat2Feed.com");

	private final List<FeedCategory> categories = new ArrayList<>();
	private final List<FeedSubscription> subscriptions = new ArrayList<>();

	@BeforeEach
	void init() {
		user.setName("John Doe");

		cat1.setId(1L);
		cat1.setName("cat1");
		cat1.setParent(null);
		cat1.setChildren(new HashSet<>());
		cat1.setSubscriptions(new HashSet<>());

		cat2.setId(2L);
		cat2.setName("cat2");
		cat2.setParent(cat1);
		cat2.setChildren(new HashSet<>());
		cat2.setSubscriptions(new HashSet<>());

		cat1.getChildren().add(cat2);

		rootFeed.setCategory(null);
		cat1Feed.setCategory(cat1);
		cat2Feed.setCategory(cat2);

		cat1.getSubscriptions().add(cat1Feed);
		cat2.getSubscriptions().add(cat2Feed);

		categories.add(cat1);
		categories.add(cat2);

		subscriptions.add(rootFeed);
		subscriptions.add(cat1Feed);
		subscriptions.add(cat2Feed);
	}

	private Feed newFeed(String url) {
		Feed feed = new Feed();
		feed.setUrl(url);
		return feed;
	}

	private FeedSubscription newFeedSubscription(String title, String url) {
		FeedSubscription feedSubscription = new FeedSubscription();
		feedSubscription.setTitle(title);
		feedSubscription.setFeed(newFeed(url));
		return feedSubscription;
	}

	@Test
	void generatesOpmlCorrectly() {
		Mockito.when(feedCategoryDAO.findAll(user)).thenReturn(categories);
		Mockito.when(feedSubscriptionDAO.findAll(user)).thenReturn(subscriptions);

		Opml opml = new OPMLExporter(feedCategoryDAO, feedSubscriptionDAO).export(user);

		List<Outline> rootOutlines = opml.getOutlines();
		Assertions.assertEquals(2, rootOutlines.size());
		Assertions.assertTrue(containsCategory(rootOutlines, "cat1"));
		Assertions.assertTrue(containsFeed(rootOutlines, "rootFeed", "rootFeed.com"));

		Outline cat1Outline = getCategoryOutline(rootOutlines, "cat1");
		List<Outline> cat1Children = cat1Outline.getChildren();
		Assertions.assertEquals(2, cat1Children.size());
		Assertions.assertTrue(containsCategory(cat1Children, "cat2"));
		Assertions.assertTrue(containsFeed(cat1Children, "cat1Feed", "cat1Feed.com"));

		Outline cat2Outline = getCategoryOutline(cat1Children, "cat2");
		List<Outline> cat2Children = cat2Outline.getChildren();
		Assertions.assertEquals(1, cat2Children.size());
		Assertions.assertTrue(containsFeed(cat2Children, "cat2Feed", "cat2Feed.com"));
	}

	private boolean containsCategory(List<Outline> outlines, String category) {
		for (Outline o : outlines) {
			if (!"rss".equals(o.getType())) {
				if (category.equals(o.getTitle())) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean containsFeed(List<Outline> outlines, String title, String url) {
		for (Outline o : outlines) {
			if ("rss".equals(o.getType())) {
				if (title.equals(o.getTitle()) && o.getAttributeValue("xmlUrl").equals(url)) {
					return true;
				}
			}
		}

		return false;
	}

	private Outline getCategoryOutline(List<Outline> outlines, String title) {
		for (Outline o : outlines) {
			if (o.getTitle().equals(title)) {
				return o;
			}
		}

		return null;
	}
}