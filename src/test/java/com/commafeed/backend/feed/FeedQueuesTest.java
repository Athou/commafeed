package com.commafeed.backend.feed;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;


import static org.mockito.Mockito.*;


public class FeedQueuesTest {


    @Test
    public void testGetLastLoginThreshold(){

        FeedQueues childOF = mock(FeedQueues.class);

        //Test with no value
        Assert.assertEquals(childOF.getLastLoginThreshold(), null);
    }
}