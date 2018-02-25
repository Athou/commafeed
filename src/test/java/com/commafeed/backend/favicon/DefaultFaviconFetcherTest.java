package com.commafeed.backend.favicon;

import com.commafeed.backend.HttpGetter;
import com.commafeed.backend.model.Feed;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultFaviconFetcherTest {

    @Test
    public void fetchFaviconThroughLinkTest() {

        // Creating the mocks ///////////////////////////////////////////////////////////////////////////////////
        HttpGetter mockGetter = mock(HttpGetter.class);
        Feed mockFeed = mock(Feed.class);
        HttpGetter.HttpResult mockResult = mock(HttpGetter.HttpResult.class);
        byte[] fakeByteStream = ("This is a test array of bytes that must have more than 100 bytes of length. " +
                "To get it to this size, I have to type more characters.").getBytes();
        String mockUrl = "Test value";
        String mockUrlProcessed = "Test value/favicon.ico";
        String mockContentType = "Text";
        ////////////////////////////////////////////////////////////////////////////////////////////////////////

        // Instantiating our class with the mocked dependency//
        DefaultFaviconFetcher defaultFavicon = new DefaultFaviconFetcher(mockGetter);


        // Programming the mock objects to return expected results on specific method calls///////////
        when(mockFeed.getLink()).thenReturn(mockUrl);
        try {
            when(mockGetter.getBinary(mockUrlProcessed, 4000)).thenReturn(mockResult);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (HttpGetter.NotModifiedException e) {
            e.printStackTrace();
        }
        when(mockResult.getContent()).thenReturn(fakeByteStream);
        when(mockResult.getContentType()).thenReturn(mockContentType);
        /////////////////////////////////////////////////////////////////////////////////////////////


        // Running the method being tested and catching the end result
        AbstractFaviconFetcher.Favicon favicon = defaultFavicon.fetch(mockFeed);

        // Assertions that the end object is what we expected
        assertEquals(favicon.getIcon(),fakeByteStream);
        assertEquals(favicon.getMediaType(),mockContentType);
    }

    @Test
    public void fetchFaviconThroughUrlTest() {

        // Creating the mocks ///////////////////////////////////////////////////////////////////////////////////
        HttpGetter mockGetter = mock(HttpGetter.class);
        Feed mockFeed = mock(Feed.class);
        HttpGetter.HttpResult mockResult = mock(HttpGetter.HttpResult.class);
        byte[] fakeByteStream = ("This is a test array of bytes that must have more than 100 bytes of length. " +
                "To get it to this size, I have to type more characters.").getBytes();
        String mockUrl = "Test value";
        String mockUrlProcessed = "Test value/favicon.ico";
        String mockContentType = "Text";
        ////////////////////////////////////////////////////////////////////////////////////////////////////////

        // Instantiating our class with the mocked dependency//
        DefaultFaviconFetcher defaultFavicon = new DefaultFaviconFetcher(mockGetter);


        // Programming the mock objects to return expected results on specific method calls///////////
        when(mockFeed.getUrl()).thenReturn(mockUrl);
        try {
            when(mockGetter.getBinary(mockUrlProcessed, 4000)).thenReturn(mockResult);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (HttpGetter.NotModifiedException e) {
            e.printStackTrace();
        }
        when(mockResult.getContent()).thenReturn(fakeByteStream);
        when(mockResult.getContentType()).thenReturn(mockContentType);
        /////////////////////////////////////////////////////////////////////////////////////////////


        // Running the method being tested and catching the end result
        AbstractFaviconFetcher.Favicon favicon = defaultFavicon.fetch(mockFeed);

        // Assertions that the end object is what we expected
        assertEquals(favicon.getIcon(),fakeByteStream);
        assertEquals(favicon.getMediaType(),mockContentType);
    }

    @Test
    public void fetchFaviconTooShortTest() {

        // Creating the mocks ///////////////////////////////////////////////////////////////////////////////////
        HttpGetter mockGetter = mock(HttpGetter.class);
        Feed mockFeed = mock(Feed.class);
        HttpGetter.HttpResult mockResult = mock(HttpGetter.HttpResult.class);
        byte[] fakeByteStream = ("This is a test array of bytes that is too short in length. Should return null").getBytes();
        String mockUrl = "Test value";
        String mockUrlProcessed = "Test value/favicon.ico";
        String mockContentType = "Text";
        ////////////////////////////////////////////////////////////////////////////////////////////////////////

        // Instantiating our class with the mocked dependency//
        DefaultFaviconFetcher defaultFavicon = new DefaultFaviconFetcher(mockGetter);


        // Programming the mock objects to return expected results on specific method calls///////////
        when(mockFeed.getUrl()).thenReturn(mockUrl);
        try {
            when(mockGetter.getBinary(mockUrlProcessed, 4000)).thenReturn(mockResult);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (HttpGetter.NotModifiedException e) {
            e.printStackTrace();
        }
        when(mockResult.getContent()).thenReturn(fakeByteStream);
        when(mockResult.getContentType()).thenReturn(mockContentType);
        /////////////////////////////////////////////////////////////////////////////////////////////


        // Running the method being tested and catching the end result
        AbstractFaviconFetcher.Favicon favicon = defaultFavicon.fetch(mockFeed);

        // Assertions that the end object is what we expected
        assertEquals(favicon, null);

    }
}
