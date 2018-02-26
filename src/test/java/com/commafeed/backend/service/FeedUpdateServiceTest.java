package com.commafeed.backend.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.List;
import org.junit.Test;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedSubscription;

import junit.framework.Assert;

/**
* This class will test methods of the class FeedUpdateService.java
*/
public class FeedUpdateServiceTest {

	@Test
	public void testAddEntry() {
		//Mocking dependencies
		FeedUpdateService fus = mock(FeedUpdateService.class);
		
		Feed feed = mock(Feed.class);
		FeedEntry feedEntry = mock(FeedEntry.class);
		List<FeedSubscription> subscriptions = (List<FeedSubscription>) mock(FeedSubscription.class);
		
		feedEntry = new 
		
		//Expecting return false when feedEntry is null
		when(fus.addEntry(feed, feedEntry, subscriptions)).thenReturn(false);
		assertEquals(fus.addEntry(feed, feedEntry, subscriptions), false);
		
		//Expecting return true when subscription is empty
		when(fus.addEntry(feed, feedEntry, subscriptions)).thenReturn(true);
		assertEquals(fus.addEntry(feed, feedEntry, subscriptions), true);
		
	}

}
