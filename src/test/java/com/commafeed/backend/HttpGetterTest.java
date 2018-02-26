package com.commafeed.backend;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.util.TodayDateConstructor;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class HttpGetterTest {

    // Test for fix of issue #859:
    // https://github.com/Athou/commafeed/issues/859
    @Test
    public void testInvalidDate() {
        // Mocking the data
        String url = "http://www.shadbase.com/feed/";
        String dateTimeToTest = "Thu, 01 Jan 1970 00:00:00 GMT";
        TodayDateConstructor mockTodayDate = mock(TodayDateConstructor.class);
        CommaFeedConfiguration mockConfig = mock(CommaFeedConfiguration.class);

        // Creating and calling the singleton under test
        HttpGetter httpGetter = new HttpGetterForTest(mockConfig, mockTodayDate);
        try {
            httpGetter.getBinary(url, dateTimeToTest, "Any string", 0);
        } catch (Exception e) {
            // Ignore exceptions
        }

        // Check that the method making the fix is called
        verify(mockTodayDate).constructDate();

    }



    private class HttpGetterForTest extends HttpGetter {

        public HttpGetterForTest(CommaFeedConfiguration config, TodayDateConstructor todayDate) {
            super(config);
            this.todayDate = todayDate;
        }
    }
}
