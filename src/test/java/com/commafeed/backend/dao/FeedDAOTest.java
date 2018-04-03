package com.commafeed.backend.dao;

import com.commafeed.backend.dao.datamigrationtoggles.MigrationToggles;
import com.commafeed.backend.dao.newstorage.FeedStorage;
import com.commafeed.backend.model.Feed;
import org.junit.Before;
import org.junit.BeforeClass;

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
        // This opens up the session with the database before each individual
        // tests.
        // It can run more than one query at a time.
        beginTransaction();
        this.feedStorage = FeedStorage.getInstance();
        
    }
}
