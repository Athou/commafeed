package com.commafeed.backend.opml;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.dao.FeedCategoryDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.feed.parser.XMLCleaner;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.User;
import com.commafeed.backend.service.FeedEntryFilteringService;
import com.commafeed.backend.service.FeedSubscriptionService;
import com.rometools.rome.io.FeedException;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class OPMLImporterTest {

    private final XMLCleaner xmlCleaner = Mockito.mock(XMLCleaner.class);
    private final FeedCategoryDAO feedCategoryDAO = Mockito.mock(FeedCategoryDAO.class);
    private final FeedSubscriptionService feedSubscriptionService = Mockito.mock(FeedSubscriptionService.class);
    private final FeedSubscriptionDAO feedSubscriptionDAO = Mockito.mock(FeedSubscriptionDAO.class);
    private final CommaFeedConfiguration config =
            Mockito.mock(CommaFeedConfiguration.class, Mockito.RETURNS_DEEP_STUBS);
    private final FeedEntryFilteringService feedEntryFilteringService = new FeedEntryFilteringService(config);
    private final OPMLImporter importer =
            new OPMLImporter(
                    xmlCleaner,
                    feedCategoryDAO,
                    feedSubscriptionDAO,
                    feedSubscriptionService,
                    feedEntryFilteringService);
    private final User user = new User();

    @BeforeEach
    void init() {
        Mockito.when(xmlCleaner.clean(Mockito.anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(config.feedRefresh().filteringExpressionEvaluationTimeout()).thenReturn(Duration.ofSeconds(30));
    }

    @Test
    void testOpmlV10() throws IOException, IllegalArgumentException, FeedException {
        testOpmlVersion("/opml/opml_v1.0.xml");
    }

    @Test
    void testOpmlV11() throws IOException, IllegalArgumentException, FeedException {
        testOpmlVersion("/opml/opml_v1.1.xml");
    }

    @Test
    void testOpmlV20() throws IOException, IllegalArgumentException, FeedException {
        testOpmlVersion("/opml/opml_v2.0.xml");
    }

    @Test
    void testOpmlNoVersion() throws IOException, IllegalArgumentException, FeedException {
        testOpmlVersion("/opml/opml_noversion.xml");
    }

    private void testOpmlVersion(String fileName) throws IOException, IllegalArgumentException, FeedException {
        String xml = IOUtils.toString(getClass().getResourceAsStream(fileName), StandardCharsets.UTF_8);

        importer.importOpml(user, xml);

        Mockito.verify(feedSubscriptionService)
                .subscribe(
                        Mockito.eq(user),
                        Mockito.anyString(),
                        Mockito.anyString(),
                        Mockito.any(FeedCategory.class),
                        Mockito.anyInt());
    }

    @Test
    void testEmptyCategoriesRoundTrip() throws FeedException {
        FeedCategory root = newCategory("root", null);
        FeedCategory parent = newCategory("parent", null);
        parent.setId(1L);
        FeedCategory child = newCategory("child", parent);
        Mockito.when(feedCategoryDAO.findAll(user)).thenReturn(Arrays.asList(root, parent, child));

        OPMLExporter exporter = new OPMLExporter(feedCategoryDAO, Mockito.mock(FeedSubscriptionDAO.class));
        importer.importOpml(user, exporter.exportXml(user));

        ArgumentCaptor<FeedCategory> captor = ArgumentCaptor.forClass(FeedCategory.class);
        Mockito.verify(feedCategoryDAO, Mockito.times(3)).persist(captor.capture());
        List<FeedCategory> imported = captor.getAllValues();
        Assertions.assertEquals(
                List.of("root", "parent", "child"), imported.stream().map(FeedCategory::getName).toList());
        Assertions.assertNull(imported.get(0).getParent());
        Assertions.assertNull(imported.get(1).getParent());
        Assertions.assertSame(imported.get(1), imported.get(2).getParent());
        Mockito.verifyNoInteractions(feedSubscriptionService);
    }

    @Test
    void testOutlineWithTypeAndNoFeedUrlIsNotACategory() throws FeedException {
        String xml =
                """
                <opml version="1.0">
                  <body>
                    <outline type="link" text="bookmark"/>
                  </body>
                </opml>
                """;

        importer.importOpml(user, xml);

        Mockito.verify(feedCategoryDAO, Mockito.never()).persist(Mockito.any(FeedCategory.class));
    }

    @Test
    void testCommaFeedSettingsRoundTrip() throws FeedException {
        Feed feed = new Feed();
        feed.setUrl("https://www.commafeed.com/feed");
        FeedSubscription exported = new FeedSubscription();
        exported.setTitle("feed");
        exported.setFeed(feed);
        exported.setFilter("title.contains('java')");
        exported.setAutoMarkAsReadAfterDays(7);
        exported.setPushNotificationsEnabled(true);

        FeedSubscriptionDAO exportDAO = Mockito.mock(FeedSubscriptionDAO.class);
        Mockito.when(exportDAO.findAll(user)).thenReturn(new ArrayList<>(List.of(exported)));
        String xml = new OPMLExporter(feedCategoryDAO, exportDAO).exportXml(user);
        Assertions.assertTrue(xml.contains("xmlns:commafeed=\"https://www.commafeed.com/opml\""));
        Assertions.assertTrue(xml.contains("commafeed:filter=\"title.contains('java')\""));
        Assertions.assertTrue(xml.contains("commafeed:autoMarkAsReadAfterDays=\"7\""));
        Assertions.assertTrue(xml.contains("commafeed:pushNotificationsEnabled=\"true\""));

        FeedSubscription imported = new FeedSubscription();
        Mockito.when(
                        feedSubscriptionService.subscribe(
                                Mockito.eq(user),
                                Mockito.eq(feed.getUrl()),
                                Mockito.eq("feed"),
                                Mockito.any(),
                                Mockito.anyInt()))
                .thenReturn(42L);
        Mockito.when(feedSubscriptionDAO.findById(user, 42L)).thenReturn(imported);

        importer.importOpml(user, xml);

        Assertions.assertEquals("title.contains('java')", imported.getFilter());
        Assertions.assertEquals(7, imported.getAutoMarkAsReadAfterDays());
        Assertions.assertTrue(imported.isPushNotificationsEnabled());
    }

    @Test
    void testInvalidFilterIsIgnored() throws FeedException {
        String xml =
                """
                <opml version="2.0" xmlns:commafeed="https://www.commafeed.com/opml">
                  <body>
                    <outline type="rss" text="feed" xmlUrl="https://www.commafeed.com/feed"
                             commafeed:filter="this is not ( a valid expression" commafeed:autoMarkAsReadAfterDays="7"/>
                  </body>
                </opml>
                """;
        FeedSubscription imported = new FeedSubscription();
        Mockito.when(
                        feedSubscriptionService.subscribe(
                                Mockito.eq(user),
                                Mockito.anyString(),
                                Mockito.anyString(),
                                Mockito.any(),
                                Mockito.anyInt()))
                .thenReturn(42L);
        Mockito.when(feedSubscriptionDAO.findById(user, 42L)).thenReturn(imported);

        importer.importOpml(user, xml);

        Assertions.assertNull(imported.getFilter());
        Assertions.assertEquals(7, imported.getAutoMarkAsReadAfterDays());
    }

    @Test
    void testOpmlWithoutCommaFeedSettingsDoesNotLoadSubscription() throws FeedException {
        String xml =
                """
                <opml version="2.0">
                  <body>
                    <outline type="rss" text="feed" xmlUrl="https://www.commafeed.com/feed"/>
                  </body>
                </opml>
                """;

        importer.importOpml(user, xml);

        Mockito.verifyNoInteractions(feedSubscriptionDAO);
    }

    private FeedCategory newCategory(String name, FeedCategory parent) {
        FeedCategory category = new FeedCategory();
        category.setName(name);
        category.setParent(parent);
        return category;
    }
}
