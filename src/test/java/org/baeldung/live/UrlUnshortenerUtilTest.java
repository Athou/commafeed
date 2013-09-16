package org.baeldung.live;


import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import org.junit.Assert;
import org.junit.Test;

/**
 * Original source code from 
 * https://github.com/eugenp/tutorials/blob/master/spring-security-rest-custom/src/test/java/org/baeldung/live/HttpLiveServiceTemp.java
 * 
 * @author dersteppen
 *
 */
@Slf4j
public class UrlUnshortenerUtilTest {

    // tests

    @Test
    public final void givenShortenedOnce_whenUrlIsUnshortened_thenCorrectResult() throws IOException {
        final String expectedResult = "http://www.baeldung.com/rest-versioning";
        final String shortenedUrl = "http://bit.ly/13jEoS1";
        final String actualResult = UrlUnshortenerUtil.expandSingleLevel(shortenedUrl);
        //log.debug("expectedResult:"+expectedResult);
        //log.debug("actualResult:"+actualResult);
        Assert.assertEquals(actualResult, expectedResult);
    }

    @Test
    public final void givenShortenedMultiple_whenUrlIsUnshortened_thenCorrectResult() throws IOException {
        final String expectedResult = "http://www.baeldung.com/rest-versioning";
        final String shortenedUrl =  "http://t.co/e4rDDbnzmk";
        final String actualResult = UrlUnshortenerUtil.expand(shortenedUrl);
        //log.debug("expectedResult:"+expectedResult);
        //log.debug("shortenedUrl:"+shortenedUrl);
        //log.debug("actualResult:"+actualResult);
        Assert.assertEquals(actualResult, expectedResult);
    }
    
    @Test
    public final void givenShortenedFeed_whenUrlIsUnshortened_thenCorrectResult() throws IOException {
        final String expectedResult = "http://www.elespectador.com/opinion/colombia-columna-444525";
        final String shortenedUrl =  "http://bit.ly/1eej3QE";
        final String actualResult = UrlUnshortenerUtil.expand(shortenedUrl);
        //log.debug("expectedResult:"+expectedResult);
        //log.debug("shortenedUrl:"+shortenedUrl);
        //log.debug("actualResult:"+actualResult);
        Assert.assertEquals(actualResult, expectedResult);
    }

    

    

}
