package com.commafeed.backend.dao;

import com.commafeed.backend.dao.datamigrationtoggles.MigrationToggles;
import com.commafeed.backend.dao.newstorage.FeedStorage;
import com.commafeed.backend.model.Feed;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class FeedDAOTest extends AbstractDAOTest {

    private static FeedDAO feedDAO;
    private FeedStorage feedStorage;
    private static Feed feed1;
    private static Feed feed2;

    @BeforeClass
    public static void beforeClass(){
        feedDAO = new FeedDAO(createSessionFactory(Feed.class));
        MigrationToggles.turnAllTogglesOff();
    }

    @Before
    public void beforeEachTest() {

        beginTransaction();
        this.feedStorage = FeedStorage.getInstance();
        feedDAO.supercedeIStorageModelDAOForTests(this.feedStorage);
    }

    @After
    public void afterEachTest(){
        closeTransaction();
    }
   @Test
    public void ForkLiftTest(){
        MigrationToggles.turnForkLiftOn();
        feed1 = getSomeFeed("http://www.geek.com", "Hello you", "A geek", "geek.com", "geek");
        feed2 = getSomeFeed("http://www.robert.com", "Bob", "bob", "bob.com", "bob");
        feedDAO.saveOrUpdate(feed1);
        feedDAO.saveOrUpdate(feed2);

        feedDAO.forkLift();

        feedDAO.delete(feed1);
        feedDAO.delete(feed2);

        assert(this.feedStorage.exists(feed1));
        assert(this.feedStorage.exists(feed2));
        assert(this.feedStorage.read(feed1).equals(feed1));
        assert(this.feedStorage.read(feed2).equals(feed2));



    }
    @Test
    public void testConsistencyCheck() {
        MigrationToggles.turnConsistencyCheckerOn();

        // Putting some users in the database
        feed1 = getSomeFeed("http://www.geek.com", "Hello you", "A geek", "geek.com", "geek");
        feed2 = getSomeFeed("http://www.robert.com", "Bob", "bob", "bob.com", "bob");

        feedDAO.saveOrUpdate(feed1);
        feedDAO.saveOrUpdate(feed2);

        // Forklifting the data from the database to the storage
        feedDAO.forkLift();

        // Checking that the data in the storage is ok
        assert(this.feedStorage.exists(feed1));
        assert(this.feedStorage.exists(feed2));
        assert(this.feedStorage.read(feed2).equals(feed2));
        assert(this.feedStorage.read(feed1).equals(feed1));

        // Corrupting the data in the datastorage
        Feed feed3 = getSomeFeed("http://www.greek.com", "Hellorrrr you", "A greek", "greek.com", "greek");
        feed3.setId(feed1.getId());
        Feed feed4 = getSomeFeed("http://www.robertii.com", "Bobi", "bobi", "bobi.com", "bobi");
        feed4.setId(feed2.getId());


        this.feedStorage.update(feed3);
        this.feedStorage.update(feed4);

        // First time, there should be two inconsistencies
        assertEquals(2, feedDAO.consistencyChecker());

        // Second time, there should be no inconsistency
        assertEquals(0, feedDAO.consistencyChecker());

        feedDAO.delete(feed1);
        feedDAO.delete(feed2);
    }

    private static Feed getSomeFeed(String url, String message, String topic, String normalURL, String header){
        Feed feed = new Feed();
        // SETTER METHOD
        long interval = 11222;
        Date now = Calendar.getInstance().getTime();
        feed.setUrl(url);
        feed.setMessage(message);
        feed.setErrorCount(1);
        feed.setPushTopic(topic);
        feed.setAverageEntryInterval(interval);
        feed.setNormalizedUrl(normalURL);
        feed.setDisabledUntil(now);
        feed.setEtagHeader(header);
        feed.setUrlAfterRedirect(url);
        feed.setLastContentHash("THISISACONTENTHASH");
        feed.setLastModifiedHeader(header);
        feed.setPushLastPing(now);

        return feed;
    }
}
