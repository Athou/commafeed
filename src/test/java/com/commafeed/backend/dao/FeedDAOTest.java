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
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

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

        // Putting some feeds in the database
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

    @Test
    public void testShadowWrites() {
        MigrationToggles.turnShadowWritesOn();

        // Putting some feed in the database
        feed1 = getSomeFeed("http://www.geek.com", "Hello you", "A geek", "geek.com", "geek");
        feed2 = getSomeFeed("http://www.robert.com", "Bob", "bob", "bob.com", "bob");

        feedDAO.saveOrUpdate(feed1);
        feedDAO.saveOrUpdate(feed2);

        // Checking that the data in the storage is ok
        assert(this.feedStorage.exists(feed1));
        assert(this.feedStorage.exists(feed2));
        assert(this.feedStorage.read(feed1).equals(feed1));
        assert(this.feedStorage.read(feed2).equals(feed2));

        feedDAO.delete(feed1);
        feedDAO.delete(feed2);
    }

    @Test
    public void testShadowReads() {
        MigrationToggles.turnShadowReadsOn();

        // Putting some feed in the database
        feed1 = getSomeFeed("http://www.geek.com", "Hello you", "A geek", "geek.com", "geek");
        feed2 = getSomeFeed("http://www.robert.com", "Bob", "bob", "bob.com", "bob");
        feedDAO.saveOrUpdate(feed1);
        feedDAO.saveOrUpdate(feed2);

        // Checking that the data in the storage is ok
        assert(this.feedStorage.exists(feed1));
        assert(this.feedStorage.exists(feed2));
        assert(this.feedStorage.read(feed1).equals(feed1));
        assert(this.feedStorage.read(feed2).equals(feed2));

        // Pulling data from the db with the read method
        Feed feed3 = feedDAO.findById(feed1.getId());

        assert(feed3.equals(feed1));

        // Corrupting the data from the storage and checking that the error
        // is automatically corrected
        Feed feed4 = getSomeFeed("http://www.robertii.com", "Bobi", "bobi", "bobi.com", "bobi");
        feed4.setId(feed2.getId());
        this.feedStorage.update(feed4);

        assertNotEquals(feed4, feed2);

        // Reading the feed
        Feed feed5 = feedDAO.findById(feed2.getId());

        // Now the corrupted data should have been corrected
        assertEquals(feed2, feed5);

        // Waiting for the asynchronous call to finish
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(this.feedStorage.read(feed4.getId()), feed5);

        feedDAO.delete(feed1);
        feedDAO.delete(feed2);
    }

    @Test
    public void testReadAndWriteMigration() {
        MigrationToggles.turnShadowReadsOn();

        // Putting some feed in the database
        feed1 = getSomeFeed("http://www.geek.com", "Hello you", "A geek", "geek.com", "geek");
        feed2 = getSomeFeed("http://www.robert.com", "Bob", "bob", "bob.com", "bob");
        feedDAO.saveOrUpdate(feed1);
        feedDAO.saveOrUpdate(feed2);

        // Getting the number of entries in the database
        int totalEntry = feedDAO.findAll().size();

        // Checking that the data in the storage is ok
        assert(this.feedStorage.exists(feed1));
        assert(this.feedStorage.exists(feed2));
        assert(this.feedStorage.read(feed1).equals(feed1));
        assert(this.feedStorage.read(feed2).equals(feed2));

        // Corrupting the data in the datastorage
        Feed feed3 = getSomeFeed("http://www.greek.com", "Hellorrrr you", "A greek", "greek.com", "greek");
        Feed feed4 = getSomeFeed("http://www.robertii.com", "Bobi", "bobi", "bobi.com", "bobi");

        feed3.setId(feed1.getId());
        feed4.setId(feed2.getId());
        this.feedStorage.update(feed3);
        this.feedStorage.update(feed4);

        int inconsistencyCounter = 0;
        double threshold = 1;

        // First time, there should be two inconsistencies
        assertEquals(2, inconsistencyCounter = feedDAO.consistencyChecker());
        int entriesChecked = 0;
        do {
            entriesChecked += totalEntry;
            threshold = inconsistencyCounter / totalEntry;
            // Other times, there should be no inconsistency
            assertEquals(0, inconsistencyCounter = feedDAO.consistencyChecker
                    ());
        } while(threshold > 0.01);

        // Now that the inconsistencies are below a certain threshold, we can
        // discard the old database
        MigrationToggles.turnReadAndWriteOn();

        // Removing the feed1 from the storage
        feedDAO.delete(feed1);
        assert(!this.feedStorage.exists(feed1));

        // Turning off the toggles to check that the feed1 in the db wasn't
        // affected
        MigrationToggles.turnAllTogglesOff();

        // Getting the feed1 from the database
        Feed feed6 = feedDAO.findById(feed1.getId());
        // Result should be equal to feed 1
        assertEquals(feed1, feed6);

        feedDAO.delete(feed1);
        feedDAO.delete(feed2);
    }

    @Test
    public void testLongTermConsistencyCheck() {
        MigrationToggles.turnLongTermConsistencyOn();

        // Putting some feed in the database
        feed1 = getSomeFeed("http://www.geek.com", "Hello you", "A geek", "geek.com", "geek");
        feed2 = getSomeFeed("http://www.robert.com", "Bob", "bob", "bob.com", "bob");
        feed1.setId(1L);
        feedDAO.saveOrUpdate(feed1);
        feed2.setId(2L);
        feedDAO.saveOrUpdate(feed2);

        HashMap<Long, Feed> longTermHashMapConsistencyChecker = new
                HashMap<Long, Feed>();

        longTermHashMapConsistencyChecker.put(feed1.getId(), feed1);
        longTermHashMapConsistencyChecker.put(feed2.getId(), feed2);

        feedDAO.setLongTermHashMap(longTermHashMapConsistencyChecker);

        // Checking that the data in the storage is ok
        assert(this.feedStorage.exists(feed1));
        assert(this.feedStorage.exists(feed2));
        assert(this.feedStorage.read(feed1).equals(feed1));
        assert(this.feedStorage.read(feed2).equals(feed2));

        // Corrupting the data in the datastorage
        Feed feed3 = getSomeFeed("http://www.greek.com", "Hellorrrr you", "A greek", "greek.com", "greek");
        Feed feed4 = getSomeFeed("http://www.robertii.com", "Bobi", "bobi", "bobi.com", "bobi");

        feed3.setId(feed1.getId());
        feed4.setId(feed2.getId());
        this.feedStorage.update(feed3);
        this.feedStorage.update(feed4);

        // First time, there should be two inconsistencies
        assertEquals(2, feedDAO.consistencyChecker());

        // Second time, there should be no inconsistency
        assertEquals(0, feedDAO.consistencyChecker());
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
