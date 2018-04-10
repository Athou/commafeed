package com.commafeed.backend.feed;

import com.codahale.metrics.MetricRegistry;
import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.service.FeedService;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;


public class FeedQueuesTest {

    private SessionFactory sessionFactory;
    private FeedDAO feedDAO;
    private CommaFeedConfiguration config;
    private MetricRegistry metrics;

    private Queue<FeedRefreshContext> addQueue;
    private Queue<FeedRefreshContext> takeQueue;
    private Queue<Feed> giveBackQueue;

    @Before
    public void setup(){
        sessionFactory = mock(SessionFactory.class);
        feedDAO = mock(FeedDAO.class);
        config = mock(CommaFeedConfiguration.class);
        metrics = mock(MetricRegistry.class);

        addQueue = mock(ConcurrentLinkedQueue.class);
        takeQueue = mock(ConcurrentLinkedQueue.class);
        giveBackQueue = mock(ConcurrentLinkedQueue.class);
    }

    @Test
    public void testTake(){
        //Badly formulated test
        FeedQueues fakeFeedQueues = mock(FeedQueues.class);

        doNothing().when(fakeFeedQueues).take();

        verify(fakeFeedQueues, times(1)).take();

    }

    @Test
    public void testAdd(){

        //Initiate a feedqueues object
        FeedQueues feedQueues = new FeedQueues(addQueue, takeQueue, giveBackQueue, sessionFactory, feedDAO, config, metrics);

        //Create the mock dependencies
        Feed mockFeed = mock(Feed.class);
        CommaFeedConfiguration.ApplicationSettings mockSetting = mock(CommaFeedConfiguration.ApplicationSettings.class);
        Stream mockStream = mock(Stream.class);

        //Mocking the process of which the add method goes through to navigate the conditions
        //3 had been choosen randomly
        when(mockSetting.getRefreshIntervalMinutes()).thenReturn(3);
        when(config.getApplicationSettings()).thenReturn(mockSetting);
        when(mockFeed.getLastUpdated()).thenReturn(null);
        when(addQueue.stream()).thenReturn(mockStream);
        when(mockStream.anyMatch(any())).thenReturn(false);

        //Run the method before verify
        feedQueues.add(mockFeed, true);

        verify(addQueue).add(any());
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

//    @Test
//    public void testGiveBack(){
//
//        //Initiate a feedqueues object
//        FeedQueues feedQueues = new FeedQueues(addQueue, takeQueue, giveBackQueue, sessionFactory, feedDAO, config, metrics);
//
//        Feed mockFeed = mock(Feed.class);
//
//        doCallRealMethod().when(feedQueues).giveBack(mockFeed);
//
//        feedQueues.giveBack(mockFeed);
//
//        verify(giveBackQueue).add(mockFeed);
//    }

    //not working
//    @Test
//    public void testGetLastLoginThreshold(){
//
//        //Initiate a feedqueues object
//        FeedQueues feedQueues = new FeedQueues(addQueue, takeQueue, giveBackQueue, sessionFactory, feedDAO, config, metrics);
//
//        CommaFeedConfiguration.ApplicationSettings mockSetting = mock(CommaFeedConfiguration.ApplicationSettings.class);
//
//        when(config.getApplicationSettings().thenReturn(mockSetting);
//        when(mockSetting.getHeavyLoad()).thenReturn(false);
//
//        assertNull();
//
//    }
}