package com.commafeed.backend.feed;

import com.commafeed.backend.model.Feed;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.*;


public class FeedQueuesTest {

    @Test
    public void testTake(){

        FeedQueues fakeFeedQueues = mock(FeedQueues.class);

        doNothing().when(fakeFeedQueues).take();

        verify(fakeFeedQueues, times(1)).take();

    }

    @Test
    public void testAdd(){
        //create a fake feed queues objects by mocking
        FeedQueues fakeFeedQueues = mock(FeedQueues.class);

        Feed fakeFeed = mock(Feed.class);

        //Simple mocking  of doNothing for the void method
        doNothing().when(fakeFeedQueues).add(isA(Feed.class), isA(boolean.class));

        fakeFeedQueues.add(fakeFeed, true);

        //simple verifying
        verify(fakeFeedQueues, times(1)).add(fakeFeed, true);
    }

    @Test
    public void testRefill(){
        //create a fake feed queues objects by mocking
        FeedQueues fakeFeedQueues = mock(FeedQueues.class);

        //Simple mocking  of doNothing for the void method
        doNothing().when(fakeFeedQueues).refill();

        fakeFeedQueues.refill();

        //simple verifying
        verify(fakeFeedQueues, times(1)).refill();
    }

    @Test
    public void testGiveBack(){

        FeedQueues fakeFeedQueues = mock(FeedQueues.class);

        Feed fakeFeed = mock(Feed.class);

        //simple mocking for a void method
        doNothing().when(fakeFeedQueues).giveBack(isA(Feed.class));

        fakeFeedQueues.giveBack(fakeFeed);

        //simple verifying
        verify(fakeFeedQueues, times(1)).giveBack(fakeFeed);
    }

    @Test
    public void testGetLastLoginThreshold(){

        FeedQueues childOF = mock(FeedQueues.class);

        //Test with no value
        Assert.assertEquals(childOF.getLastLoginThreshold(), null);
    }
}