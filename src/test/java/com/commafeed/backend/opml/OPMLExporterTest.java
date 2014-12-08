package com.commafeed.backend.opml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.commafeed.backend.dao.FeedCategoryDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.User;
import com.rometools.opml.feed.opml.Opml;
import com.rometools.opml.feed.opml.Outline;

public class OPMLExporterTest {

	@Mock
	private FeedCategoryDAO feedCategoryDAO;
	@Mock
	private FeedSubscriptionDAO feedSubscriptionDAO;

	private User user = new User();

	private FeedCategory cat1 = new FeedCategory();
	private FeedCategory cat2 = new FeedCategory();

	private FeedSubscription rootFeed = newFeedSubscription("rootFeed", "rootFeed.com");
	private FeedSubscription cat1Feed = newFeedSubscription("cat1Feed", "cat1Feed.com");
	private FeedSubscription cat2Feed = newFeedSubscription("cat2Feed", "cat2Feed.com");

	private List<FeedCategory> categories = new ArrayList<>();
	private List<FeedSubscription> subscriptions = new ArrayList<>();

	@Before
	public void before_each_test() {
		MockitoAnnotations.initMocks(this);

		user.setName("John Doe");

		cat1.setId(1l);
		cat1.setName("cat1");
		cat1.setParent(null);
		cat1.setChildren(new HashSet<FeedCategory>());
		cat1.setSubscriptions(new HashSet<FeedSubscription>());

		cat2.setId(2l);
		cat2.setName("cat2");
		cat2.setParent(cat1);
		cat2.setChildren(new HashSet<FeedCategory>());
		cat2.setSubscriptions(new HashSet<FeedSubscription>());

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
	public void generates_OPML_correctly() {
		when(feedCategoryDAO.findAll(user)).thenReturn(categories);
		when(feedSubscriptionDAO.findAll(user)).thenReturn(subscriptions);

		Opml opml = new OPMLExporter(feedCategoryDAO, feedSubscriptionDAO).export(user);

		List<Outline> rootOutlines = opml.getOutlines();
		assertEquals(2, rootOutlines.size());
		assertTrue(containsCategory(rootOutlines, "cat1"));
		assertTrue(containsFeed(rootOutlines, "rootFeed", "rootFeed.com"));

		Outline cat1Outline = getCategoryOutline(rootOutlines, "cat1");
		List<Outline> cat1Children = cat1Outline.getChildren();
		assertEquals(2, cat1Children.size());
		assertTrue(containsCategory(cat1Children, "cat2"));
		assertTrue(containsFeed(cat1Children, "cat1Feed", "cat1Feed.com"));

		Outline cat2Outline = getCategoryOutline(cat1Children, "cat2");
		List<Outline> cat2Children = cat2Outline.getChildren();
		assertEquals(1, cat2Children.size());
		assertTrue(containsFeed(cat2Children, "cat2Feed", "cat2Feed.com"));
	}

	private boolean containsCategory(List<Outline> outlines, String category) {
		for (Outline o : outlines)
			if (!"rss".equals(o.getType()))
				if (category.equals(o.getTitle()))
					return true;

		return false;
	}

	private boolean containsFeed(List<Outline> outlines, String title, String url) {
		for (Outline o : outlines)
			if ("rss".equals(o.getType()))
				if (title.equals(o.getTitle()) && o.getAttributeValue("xmlUrl").equals(url))
					return true;

		return false;
	}

	private Outline getCategoryOutline(List<Outline> outlines, String title) {
		for (Outline o : outlines)
			if (o.getTitle().equals(title))
				return o;

		return null;
	}
}