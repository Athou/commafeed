package com.commafeed.backend.opml;

import com.commafeed.backend.dao.FeedCategoryDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.feed.parser.XMLCleaner;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.User;
import com.commafeed.backend.service.FeedSubscriptionService;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.WireFeedOutput;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

class OPMLImporterTest {

    private final XMLCleaner xmlCleaner = Mockito.mock(XMLCleaner.class);
    private final FeedCategoryDAO feedCategoryDAO = Mockito.mock(FeedCategoryDAO.class);
    private final FeedSubscriptionService feedSubscriptionService = Mockito.mock(FeedSubscriptionService.class);
    private final OPMLImporter importer = new OPMLImporter(xmlCleaner, feedCategoryDAO, feedSubscriptionService);
    private final User user = new User();

    @BeforeEach
    void init() {
        Mockito.when(xmlCleaner.clean(Mockito.anyString())).thenAnswer(invocation -> invocation.getArgument(0));
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
        importer.importOpml(user, new WireFeedOutput().outputString(exporter.export(user)));

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

    private FeedCategory newCategory(String name, FeedCategory parent) {
        FeedCategory category = new FeedCategory();
        category.setName(name);
        category.setParent(parent);
        return category;
    }
}
