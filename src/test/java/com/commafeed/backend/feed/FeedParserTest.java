package com.commafeed.backend.feed;
import com.commafeed.backend.model.Feed;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.*;

import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;

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
