package com.commafeed.backend.opml;

import com.commafeed.backend.dao.FeedCategoryDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.User;
import com.rometools.opml.feed.opml.Opml;
import com.rometools.opml.feed.opml.Outline;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.WireFeedInput;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class OPMLExporterTest {

    @Mock private FeedCategoryDAO feedCategoryDAO;
    @Mock private FeedSubscriptionDAO feedSubscriptionDAO;

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

        cat2.setId(2L);
        cat2.setName("cat2");
        cat2.setParent(cat1);

        rootFeed.setCategory(null);
        cat1Feed.setCategory(cat1);
        cat2Feed.setCategory(cat2);

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
    void generatesOpmlCorrectly() throws FeedException {
        Mockito.when(feedCategoryDAO.findAll(user)).thenReturn(categories);
        Mockito.when(feedSubscriptionDAO.findAll(user)).thenReturn(subscriptions);

        Opml opml = export();

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

    @Test
    void exportsCommaFeedSettings() throws FeedException {
        cat1Feed.setFilter("title.contains('java')");
        cat1Feed.setAutoMarkAsReadAfterDays(7);
        cat1Feed.setPushNotificationsEnabled(true);
        Mockito.when(feedCategoryDAO.findAll(user)).thenReturn(categories);
        Mockito.when(feedSubscriptionDAO.findAll(user)).thenReturn(subscriptions);

        Opml opml = export();

        Outline rootFeedOutline = getCategoryOutline(opml.getOutlines(), "rootFeed");
        Assertions.assertNull(rootFeedOutline.getAttributeValue(OPMLConstants.FILTER));
        Assertions.assertNull(rootFeedOutline.getAttributeValue(OPMLConstants.AUTO_MARK_AS_READ_AFTER_DAYS));
        Assertions.assertNull(rootFeedOutline.getAttributeValue(OPMLConstants.PUSH_NOTIFICATIONS_ENABLED));

        Outline cat1FeedOutline =
                getCategoryOutline(getCategoryOutline(opml.getOutlines(), "cat1").getChildren(), "cat1Feed");
        Assertions.assertEquals("title.contains('java')", cat1FeedOutline.getAttributeValue(OPMLConstants.FILTER));
        Assertions.assertEquals("7", cat1FeedOutline.getAttributeValue(OPMLConstants.AUTO_MARK_AS_READ_AFTER_DAYS));
        Assertions.assertEquals("true", cat1FeedOutline.getAttributeValue(OPMLConstants.PUSH_NOTIFICATIONS_ENABLED));
    }

    @Test
    void exportsCommaFeedSettingsAsNamespacedAttributes() throws FeedException {
        cat1Feed.setFilter("title.contains('java')");
        Mockito.when(feedCategoryDAO.findAll(user)).thenReturn(categories);
        Mockito.when(feedSubscriptionDAO.findAll(user)).thenReturn(subscriptions);

        String xml = new OPMLExporter(feedCategoryDAO, feedSubscriptionDAO).exportXml(user);

        Assertions.assertTrue(xml.contains("xmlns:commafeed=\"https://www.commafeed.com/opml\""));
        Assertions.assertTrue(xml.contains("commafeed:filter=\"title.contains('java')\""));
        // declared once on the root only
        Assertions.assertEquals(1, xml.split("xmlns:commafeed", -1).length - 1);
    }

    private Opml export() throws FeedException {
        String xml = new OPMLExporter(feedCategoryDAO, feedSubscriptionDAO).exportXml(user);
        return (Opml) new WireFeedInput().build(new StringReader(xml));
    }

    private boolean containsCategory(List<Outline> outlines, String category) {
        for (Outline o : outlines) {
            if (!"rss".equals(o.getType()) && category.equals(o.getTitle())) {
                return true;
            }
        }

        return false;
    }

    private boolean containsFeed(List<Outline> outlines, String title, String url) {
        for (Outline o : outlines) {
            if ("rss".equals(o.getType()) && title.equals(o.getTitle()) && o.getAttributeValue("xmlUrl").equals(url)) {
                return true;
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
