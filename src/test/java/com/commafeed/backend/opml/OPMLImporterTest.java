package com.commafeed.backend.opml;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.mockito.Mockito;

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
		FeedCategoryDAO feedCategoryDAO = Mockito.mock(FeedCategoryDAO.class);
		FeedSubscriptionService feedSubscriptionService = Mockito.mock(FeedSubscriptionService.class);
		CacheService cacheService = Mockito.mock(CacheService.class);
		User user = Mockito.mock(User.class);

		String xml = IOUtils.toString(getClass().getResourceAsStream(fileName));

		OPMLImporter importer = new OPMLImporter(feedCategoryDAO, feedSubscriptionService, cacheService);
		importer.importOpml(user, xml);

		Mockito.verify(feedSubscriptionService).subscribe(Mockito.eq(user), Mockito.anyString(), Mockito.anyString(),
				Mockito.any(FeedCategory.class), Mockito.anyInt());
	}

}
