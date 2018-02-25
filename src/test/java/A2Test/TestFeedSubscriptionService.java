package A2Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.HttpGetter.HttpResult;
import com.commafeed.backend.cache.CacheService;
import com.commafeed.backend.dao.FeedEntryStatusDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.feed.FeedQueues;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.User;
import com.commafeed.backend.service.FeedService;
import com.commafeed.backend.service.FeedSubscriptionService;

public class TestFeedSubscriptionService {
	 FeedEntryStatusDAO mockFeedEntryStatusDAO;
	 FeedSubscriptionDAO mockFeedSubscriptionDAO;
	 FeedService mockFeedService;
	 FeedQueues mockQueues;
	 CacheService mockCache;
	 CommaFeedConfiguration mockConfig;
	 User testUser;
	 FeedCategory category;
	 String url = "www.9gag.com";
	 Feed feed;
	 FeedSubscription feedSub;
	 
	 @Before
	 public void setUp() throws Exception 
	 {
		mockFeedEntryStatusDAO = mock(FeedEntryStatusDAO.class);
		mockFeedSubscriptionDAO = mock(FeedSubscriptionDAO.class);
		mockFeedService = mock(FeedService.class);
		mockQueues = mock(FeedQueues.class);
		mockCache = mock(CacheService.class);
		mockConfig = mock(CommaFeedConfiguration.class, Mockito.RETURNS_DEEP_STUBS);
		
		//set a feed
		feed = new Feed(); 
		feed.setUrl(url);
		
		//set a FeedSubscription
		feedSub = new FeedSubscription();
		feedSub.setFeed(feed);
		feedSub.setTitle("9gag Subscription");
		
		 //set the user
		testUser = new User();
		testUser.setName("Chris");
		 
		 //set the category
		category = new FeedCategory();
		category.setName("memes");
    }

	@Test
	public void testSubscribe() {
		
		Feed testingFeed = new Feed();
		//stub CommaFeedConfiguration
		when(mockConfig.getApplicationSettings().getPublicUrl()).thenReturn("badUrl");
				
		//stub FeedService
		when(mockFeedService.findOrCreate(url)).thenReturn(feed);
		
		
		//stub FeedSubscriptionDAO
		when(mockFeedSubscriptionDAO.findByFeed(testUser, feed)).thenReturn(feedSub);
		
		FeedSubscriptionService fss = new FeedSubscriptionService(mockFeedEntryStatusDAO,mockFeedSubscriptionDAO, mockFeedService, mockQueues, mockCache, mockConfig);
		
		testingFeed = fss.subscribe(testUser, url, "9gag", category, 1);
		
		
		//check for category 
		assertEquals(feedSub.getCategory().getName(), category.getName());
		
		//check if the right feed is returned
		assertEquals(testingFeed.getUrl(), mockFeedService.findOrCreate(url).getUrl());
	
	}

}
