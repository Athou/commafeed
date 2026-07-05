package com.commafeed.backend.opml;

import com.commafeed.backend.dao.FeedCategoryDAO;
import com.commafeed.backend.feed.parser.XMLCleaner;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.User;
import com.commafeed.backend.service.FeedSubscriptionService;
import com.rometools.rome.io.FeedException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class OPMLImporterTest {

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

    private void testOpmlVersion(String fileName)
            throws IOException, IllegalArgumentException, FeedException {
        XMLCleaner xmlCleaner = Mockito.mock(XMLCleaner.class);
        FeedCategoryDAO feedCategoryDAO = Mockito.mock(FeedCategoryDAO.class);
        FeedSubscriptionService feedSubscriptionService =
                Mockito.mock(FeedSubscriptionService.class);
        User user = Mockito.mock(User.class);

        Mockito.when(xmlCleaner.clean(Mockito.anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        String xml =
                IOUtils.toString(getClass().getResourceAsStream(fileName), StandardCharsets.UTF_8);

        OPMLImporter importer =
                new OPMLImporter(xmlCleaner, feedCategoryDAO, feedSubscriptionService);
        importer.importOpml(user, xml);

        Mockito.verify(feedSubscriptionService)
                .subscribe(
                        Mockito.eq(user),
                        Mockito.anyString(),
                        Mockito.anyString(),
                        Mockito.any(FeedCategory.class),
                        Mockito.anyInt());
    }
}
