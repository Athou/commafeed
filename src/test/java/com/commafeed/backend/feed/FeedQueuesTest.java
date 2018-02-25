package com.commafeed.backend.feed;

import com.commafeed.backend.model.Feed;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;


import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.mockito.Mockito.*;


public class FeedQueuesTest {

    @Test
    public void testAdd(){
        //create a fake feed queues objects by mocking
        FeedQueues fakeFeedQueues = mock(FeedQueues.class);

        Feed fakeFeed = mock(Feed.class);

        //Simple mocking and verifying
        doNothing().when(fakeFeedQueues).add(isA(Feed.class), isA(boolean.class));

        fakeFeedQueues.add(fakeFeed, true);

        verify(fakeFeedQueues, times(1)).add(fakeFeed, true);
    }


    @Test
    public void testGetLastLoginThreshold(){

        FeedQueues childOF = mock(FeedQueues.class);

        //Test with no value
        Assert.assertEquals(childOF.getLastLoginThreshold(), null);
    }
}