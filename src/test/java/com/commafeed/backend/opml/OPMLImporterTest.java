package com.commafeed.backend.opml;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import com.commafeed.backend.cache.CacheService;
import com.commafeed.backend.dao.FeedCategoryDAO;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.User;
import com.commafeed.backend.service.FeedSubscriptionService;

public class OPMLImporterTest {

	@Test
	public void testOpmlV10() throws IOException {
		testOpmlVersion("/opml/opml_v1.0.xml");
	}

	@Test
	public void testOpmlV11() throws IOException {
		testOpmlVersion("/opml/opml_v1.1.xml");
	}

	@Test
	public void testOpmlV20() throws IOException {
		testOpmlVersion("/opml/opml_v2.0.xml");
	}

	@Test
	public void testOpmlNoVersion() throws IOException {
		testOpmlVersion("/opml/opml_noversion.xml");
	}

	private void testOpmlVersion(String fileName) throws IOException {
		FeedCategoryDAO feedCategoryDAO = mock(FeedCategoryDAO.class);
		FeedSubscriptionService feedSubscriptionService = mock(FeedSubscriptionService.class);
		CacheService cacheService = mock(CacheService.class);
		User user = mock(User.class);

		String xml = IOUtils.toString(getClass().getResourceAsStream(fileName));

		OPMLImporter importer = new OPMLImporter(feedCategoryDAO, feedSubscriptionService, cacheService);
		importer.importOpml(user, xml);

		verify(feedSubscriptionService).subscribe(eq(user), anyString(), anyString(),
				any(FeedCategory.class), anyInt());
	}

}
