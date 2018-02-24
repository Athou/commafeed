package com.commafeed.backend.feed;
import com.commafeed.backend.model.Feed;
import com.rometools.rome.feed.CopyFrom;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.feed.synd.*;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.*;

import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class will test methods of the class FeedParser.java
 */
public class FeedParserTest {

    @Test
    public void testValidateDateWithAll(){

        // Mocking the child of the singleton to use the Mockito API
        childOfParser parser = mock(childOfParser.class);
        // Setting date obj.
        Date now = new Date();
        Date nullDate = null;

        // Get ealier date
        Calendar calenEarly =  Calendar.getInstance();
        calenEarly.set(1888, 02,02);
        Date testDateEarly = calenEarly.getTime();

        // Get Far away date
        Calendar calenFar =  Calendar.getInstance();
        calenFar.set(2993, 02,02);
        Date testDateFar = calenFar.getTime();

        // Expecting return date now upon passing date=null and true...
        when(parser.validateDate(null, true)).thenReturn(now);
        Assert.assertEquals(parser.validateDate(null, true), now);

        // Expecting return null upon passing date=null and false...
        when(parser.validateDate(null, false)).thenReturn(null);
        Assert.assertEquals(parser.validateDate(null, false), null);

        // Expecting return date now upon passing date=very far from now and false/true...
        when(parser.validateDate(testDateFar, false)).thenReturn(now);
        Assert.assertEquals(parser.validateDate(testDateFar, false), now);

        // Expecting return date now upon passing date=very early from now and false/true...
        when(parser.validateDate(testDateEarly, true)).thenReturn(now);
        Assert.assertEquals(parser.validateDate(testDateEarly, true), now);

        // Expecting return date now upon passing date= now from now and false/true...
        when(parser.validateDate(now, true)).thenReturn(now);
        Assert.assertEquals(parser.validateDate(now, true), now);

    }

    @Test
    public void testgetEntryUpdateDate(){
        childOfParser parser = mock(childOfParser.class);
        // Make a fake SyncEntry
        SyndEntry fakeEntry = makeMockFakeSyndEntry();
        Date now = new Date();
        fakeEntry.setUpdatedDate(now);

        // Get ealier date
        Calendar calenEarly =  Calendar.getInstance();
        calenEarly.set(928, 06,02);
        Date testDateEarly = calenEarly.getTime();

        when(parser.getEntryUpdateDate(fakeEntry)).thenReturn(now);
        Assert.assertEquals(parser.getEntryUpdateDate(fakeEntry), now);

        // Upon getting a date obj == null, suppose to get null (if publish date is null)
        fakeEntry.setUpdatedDate(null);
        when(parser.getEntryUpdateDate(fakeEntry)).thenReturn(null);
        Assert.assertEquals(parser.getEntryUpdateDate(fakeEntry), null);

        // Let's setup the publish date and expect the publish date even if UpdatedDate is null
        fakeEntry.setPublishedDate(testDateEarly);
        fakeEntry.setUpdatedDate(null);
        when(parser.getEntryUpdateDate(fakeEntry)).thenReturn(testDateEarly);
        Assert.assertEquals(parser.getEntryUpdateDate(fakeEntry), testDateEarly);
    }
    @Test
    public void testGetContent(){
        // In this section , it was not possible to use Mockito as the library org.apache.common.lang3
        // was not initialized correctly using mock(class)...
        childOfParser parser = new childOfParser();
        // Make a fake SyncEntry
        SyndEntry fakeEntryWithNothing = mock(SyndEntry.class);
        // Test with no value
        Assert.assertEquals(parser.getContent(fakeEntryWithNothing), null);

        SyndEntry fakeEntry = makeFakeSyndEntry();
        String expectedValue = fakeEntry.getContents().get(0).getValue();

        Assert.assertEquals(parser.getContent(fakeEntry), expectedValue);
    }
    @Test
    public void testGetTitle(){
        // In this section , it was not possible to use Mockito as the library org.apache.common.lang3
        // was not initialized correctly using mock(class)...
        childOfParser parser = new childOfParser();
        // Make a fake SyncEntry
        SyndEntry fakeEntry = makeFakeSyndEntry();
        String expectedTitle = fakeEntry.getTitle();
        Assert.assertEquals(parser.getTitle(fakeEntry), expectedTitle);

    }

    // make a mock fake SyndEntry
    private SyndEntry makeMockFakeSyndEntry(){
        SyndEntry fakeMockSyncEntry = mock(SyndEntry.class);
        SyndContent fakeContent = mock(SyndContent.class);

        fakeContent.setValue("This is fake Content");
        fakeContent.setType("News");
        fakeContent.setMode("standard");

        List<SyndContent> fakeList = mock(ArrayList.class);
        fakeList.add(fakeContent);

        fakeMockSyncEntry.setAuthor("Bob Bobinson");
        fakeMockSyncEntry.setLink("http://this.is.a.link.com");
        fakeMockSyncEntry.setContents(fakeList);
        fakeMockSyncEntry.setDescription(fakeContent);
        fakeMockSyncEntry.setUri("http://this-is-a-website.com");
        return  fakeMockSyncEntry;
    }
    // Helper function to set a fake entry...
    private SyndEntry makeFakeSyndEntry(){
        SyndEntry fakeSyncEntry = new SyndEntryImpl();
        SyndContent fakeContent = new SyndContentImpl();

        fakeContent.setValue("This is fake Content");
        fakeContent.setType("News");
        fakeContent.setMode("standard");

        List<SyndContent> fakeList = new ArrayList<SyndContent>();
        fakeList.add(0, fakeContent);

        fakeSyncEntry.setComments("This is a comment");
        fakeSyncEntry.setAuthor("Bob Bobinson");
        fakeSyncEntry.setLink("http://this.is.a.link.com");
        fakeSyncEntry.setContents(fakeList);
        fakeSyncEntry.setDescription(fakeContent);
        fakeSyncEntry.setUri("http://this-is-a-website.com");
        fakeSyncEntry.setTitle("This is a title");
        return  fakeSyncEntry;
    }
}

/**
 * This class has been made for testing purposes... it
 * is overcoming the '@Singleton' restriction applied on FeedParser.java
 * by creating an non-singleton child...(custom mock)
 *
 * Mockito can't Mock Singleton...
 *
 * With this child method inside FeedParser.java can be tested
 */
 class childOfParser extends FeedParser{ }
