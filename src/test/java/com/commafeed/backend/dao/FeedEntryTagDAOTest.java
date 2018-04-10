package com.commafeed.backend.dao;

import com.commafeed.backend.dao.datamigrationtoggles.MigrationToggles;
import com.commafeed.backend.dao.newstorage.FeedEntryTagStorage;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryContent;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.FeedEntryTag;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.User;


import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class FeedEntryTagDAOTest extends AbstractDAOTest{
	private static FeedEntryTagDAO tagDAO;
	private FeedEntryTagStorage tagStorage;
	private static FeedEntryTag tag1;
	private static FeedEntryTag tag2;
	private static List<Class> classes = new ArrayList<>();
	@BeforeClass
    public static void beforeClass() {
		classes.add(FeedEntryTag.class);
		classes.add(User.class);
		classes.add(FeedEntry.class);
		classes.add(Feed.class);
		classes.add(FeedEntryContent.class);
		classes.add(FeedEntryStatus.class);
		classes.add(FeedSubscription.class);
		classes.add(FeedCategory.class);
		tagDAO = new FeedEntryTagDAO(createSessionFactory(classes));
		MigrationToggles.turnAllTogglesOff();
	}
	
	@Before
    public void beforeEachTest() {
        // This opens up the session with the database before each individual
        // tests.
        // It can run more than one query at a time.
        beginTransaction();
        this.tagStorage = FeedEntryTagStorage.getInstance();
        tagDAO.supercedeIStorageModelDAOForTests(this.tagStorage);
    }
	
	@After
    public void afterEachTest(){
        closeTransaction();
    }
	
	@Test
	public void forkliftTest()
	{
		System.out.println("this is a test");
		User user1 = new User();
		user1.setId(Long.valueOf(2000));
		FeedEntry entry1 = new FeedEntry();
		entry1.setId(Long.valueOf(2000));
		
		User user2 = new User();
		user2.setId(Long.valueOf(2001));
		FeedEntry entry2 = new FeedEntry();
		entry2.setId(Long.valueOf(2030));
		
		MigrationToggles.turnForkLiftOn();
		
		tag1 = setupTag(entry1,user1,"bitcoin");
		tag2 = setupTag(entry2,user2,"marijuana");
		tagDAO.saveOrUpdate(tag1);
		tagDAO.saveOrUpdate(tag2);
		
		tagDAO.forklift();
		
		tagDAO.delete(tag1);
		tagDAO.delete(tag2);
		
		 assert(this.tagStorage.exists(tag1));
		 assert(this.tagStorage.exists(tag2));
		 assert(this.tagStorage.read(tag1).equals(tag1));
		 assert(this.tagStorage.read(tag2).equals(tag2));
		 
	}
	
	@Test
	public void testConsistency()
	{
		MigrationToggles.turnConsistencyCheckerOn();
		
		User user1 = new User();
		user1.setId(Long.valueOf(2000));
		FeedEntry entry1 = new FeedEntry();
		entry1.setId(Long.valueOf(2000));
		
		User user2 = new User();
		user2.setId(Long.valueOf(2001));
		FeedEntry entry2 = new FeedEntry();
		entry2.setId(Long.valueOf(2030));
		
		User user3 = new User();
		user3.setId(Long.valueOf(2002));
		FeedEntry entry3 = new FeedEntry();
		entry3.setId(Long.valueOf(2040));
		
		MigrationToggles.turnForkLiftOn();
		
		tag1 = setupTag(entry1,user1,"bitcoin");
		tag2 = setupTag(entry2,user2,"marijuana");
		tagDAO.saveOrUpdate(tag1);
		tagDAO.saveOrUpdate(tag2);
		
		tagDAO.forklift();
		
		assert(this.tagStorage.exists(tag1));
		assert(this.tagStorage.exists(tag2));
		assert(this.tagStorage.read(tag1).equals(tag1));
		assert(this.tagStorage.read(tag2).equals(tag2));
		
		//corrupting data in tagStorage
		FeedEntryTag tag3 = new FeedEntryTag();
		tag3 = setupTag(entry3,user3,"123");
		this.tagStorage.update(tag3);
		tagDAO.consistencyChecker();
		
		// First time, there should be two inconsistencies
        assertEquals(1, tagDAO.consistencyChecker());
        
        // Second time, there should be no inconsistency
        assertEquals(0, tagDAO.consistencyChecker());
        
        tagDAO.delete(tag3);
				
	}
	
	 @Test
	 public void testShadowWrites() 
	 {
		MigrationToggles.turnShadowWritesOn();
		 
		User user1 = new User();
		user1.setId(Long.valueOf(2000));
		FeedEntry entry1 = new FeedEntry();
		entry1.setId(Long.valueOf(2000));
		
		User user2 = new User();
		user2.setId(Long.valueOf(2001));
		FeedEntry entry2 = new FeedEntry();
		entry2.setId(Long.valueOf(2030));
		
		tag1 = setupTag(entry1,user1,"bitcoin");
		tag2 = setupTag(entry2,user2,"marijuana");
		tagDAO.saveOrUpdate(tag1);
		tagDAO.saveOrUpdate(tag2);
		
		 assert(this.tagStorage.exists(tag1));
		 assert(this.tagStorage.exists(tag2));
		 assert(this.tagStorage.read(tag1).equals(tag1));
		 assert(this.tagStorage.read(tag2).equals(tag2));
		 
		tagDAO.delete(tag1);
		tagDAO.delete(tag2);
		 
	 }
	 @Test
	 public void testShadowReads() 
	 {
		MigrationToggles.turnShadowWritesOn();
		 
		User user1 = new User();
		user1.setId(Long.valueOf(2000));
		FeedEntry entry1 = new FeedEntry();
		entry1.setId(Long.valueOf(2000));
		
		User user2 = new User();
		user2.setId(Long.valueOf(2001));
		FeedEntry entry2 = new FeedEntry();
		entry2.setId(Long.valueOf(2030));
		
		tag1 = setupTag(entry1,user1,"bitcoin");
		tag2 = setupTag(entry2,user2,"marijuana");
		tagDAO.saveOrUpdate(tag1);
		tagDAO.saveOrUpdate(tag2);
		
		assert(this.tagStorage.exists(tag1));
		assert(this.tagStorage.exists(tag2));
		assert(this.tagStorage.read(tag1).equals(tag1));
		assert(this.tagStorage.read(tag2).equals(tag2));
		 
		FeedEntryTag tag3 = tagDAO.findById(Long.valueOf(tag1.getId()));
		 assert(tag3.equals(tag1));
		 
		User user4 = new User();
		user4.setId(Long.valueOf(2020));
		FeedEntry entry4 = new FeedEntry();
		entry4.setId(Long.valueOf(2060));
		FeedEntryTag tag4 = setupTag(entry4, user4, "random");
		tag4.setId(Long.valueOf(tag2.getId()));
		this.tagStorage.update(tag4);
		
		assertNotEquals(tag4, tag2);
		
		FeedEntryTag tag5 = tagDAO.findById(tag2.getId());
		
		// Now the corrupted data should have been corrected
        assertEquals(tag2, tag5);
        
        // Waiting for the asynchronous call to finish
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(this.tagStorage.read(tag4.getId()), tag5);

        tagDAO.delete(tag1);
        tagDAO.delete(tag2);
	 }
	 
	 @Test
     public void testReadAndWriteMigration() 
	 {
        MigrationToggles.turnShadowReadsOn();
        
        User user1 = new User();
		user1.setId(Long.valueOf(2000));
		FeedEntry entry1 = new FeedEntry();
		entry1.setId(Long.valueOf(2000));
		
		User user2 = new User();
		user2.setId(Long.valueOf(2001));
		FeedEntry entry2 = new FeedEntry();
		entry2.setId(Long.valueOf(2030));
		
		tag1 = setupTag(entry1,user1,"bitcoin");
		tag2 = setupTag(entry2,user2,"marijuana");
		tagDAO.saveOrUpdate(tag1);
		tagDAO.saveOrUpdate(tag2);
		
		
		// Getting the number of entries in the database
        int totalEntry = tagDAO.findAllTags().size();

        // Checking that the data in the storage is ok
        assert(this.tagStorage.exists(tag1));
        assert(this.tagStorage.exists(tag2));
        assert(this.tagStorage.read(tag1).equals(tag1));
        assert(this.tagStorage.read(tag2).equals(tag2));

        // Corrupting the data in the datastorage
        User user3 = new User();
		user3.setId(Long.valueOf(3000));
		FeedEntry entry3 = new FeedEntry();
		entry3.setId(Long.valueOf(4000));
        FeedEntryTag tag3 = setupTag(entry3,user3, "facebook");
        tag3.setId(tag1.getId());
        this.tagStorage.update(tag3);
        User user4 = new User();
		user4.setId(Long.valueOf(3000));
		FeedEntry entry4 = new FeedEntry();
		entry4.setId(Long.valueOf(4000));
        FeedEntryTag tag4 = setupTag(entry4,user4, "twitter");
        tag4.setId(tag2.getId());
        this.tagStorage.update(tag4);

        int inconsistencyCounter = 0;
        double threshold = 1;

        // First time, there should be 6 inconsistencies
        assertEquals(6, inconsistencyCounter = tagDAO.consistencyChecker());
        int entriesChecked = 0;
        do {
            entriesChecked += totalEntry;
            threshold = inconsistencyCounter / totalEntry;
            // Other times, there should be no inconsistency
            assertEquals(0, inconsistencyCounter = tagDAO.consistencyChecker
                    ());
        } while(threshold > 0.01);

        // Now that the inconsistencies are below a certain threshold, we can
        // discard the old database
        MigrationToggles.turnReadAndWriteOn();

        // Removing the user1 from the storage
        tagDAO.delete(tag1);
        assert(!this.tagStorage.exists(tag1));

        // Turning off the toggles to check that the user1 in the db wasn't
        // affected
        MigrationToggles.turnAllTogglesOff();

        // Getting the user1 from the database
        FeedEntryTag tag6 = tagDAO.findById(tag1.getId());
        // Result should be equal to user 1
        assertEquals(tag1, tag6);

        tagDAO.delete(tag1);
        tagDAO.delete(tag2);
        
	 }
	 
	 
	 @Test
     public void testLongTermConsistencyCheck() 
	 {
        MigrationToggles.turnLongTermConsistencyOn();
        
        User user1 = new User();
		user1.setId(Long.valueOf(2000));
		FeedEntry entry1 = new FeedEntry();
		entry1.setId(Long.valueOf(2000));
		
		User user2 = new User();
		user2.setId(Long.valueOf(2001));
		FeedEntry entry2 = new FeedEntry();
		entry2.setId(Long.valueOf(2030));
		
		User user3 = new User();
		user3.setId(Long.valueOf(2002));
		FeedEntry entry3 = new FeedEntry();
		entry3.setId(Long.valueOf(2040));
		
		User user4 = new User();
		user4.setId(Long.valueOf(2002));
		FeedEntry entry4 = new FeedEntry();
		entry4.setId(Long.valueOf(2040));
        // Putting some users in the database
        tag1 = setupTag(entry1,user1, "facebook");
        user1.setId(1L);
        tagDAO.saveOrUpdate(tag1);
        tag2 = setupTag(entry2,user2, "weather");
        user2.setId(2L);
        tagDAO.saveOrUpdate(tag2);

        HashMap<Long, FeedEntryTag> longTermHashMapConsistencyChecker = new
                HashMap<Long, FeedEntryTag>();

        longTermHashMapConsistencyChecker.put(tag1.getId(), tag1);
        longTermHashMapConsistencyChecker.put(tag2.getId(), tag2);

        tagDAO.setLongTermHashMap(longTermHashMapConsistencyChecker);

        // Checking that the data in the storage is ok
        assert(this.tagStorage.exists(tag1));
        assert(this.tagStorage.exists(tag2));
        assert(this.tagStorage.read(tag1).equals(tag1));
        assert(this.tagStorage.read(tag2).equals(tag2));

        // Corrupting the data in the datastorage
        FeedEntryTag tag3 = setupTag(entry3,user3, "food");
        tag3.setId(tag1.getId());
        this.tagStorage.update(tag3);
        FeedEntryTag tag4 = setupTag(entry4,user4, "cars");
        tag4.setId(tag2.getId());
        this.tagStorage.update(tag4);

        // First time, there should be two inconsistencies
        assertEquals(2, tagDAO.consistencyChecker());

        // Second time, there should be no inconsistency
        assertEquals(0, tagDAO.consistencyChecker());
    }

	
	private static FeedEntryTag setupTag(FeedEntry entry_id, User user_id, String tagName)
	{
		FeedEntryTag  tag = new FeedEntryTag();
		tag.setEntry(entry_id);
		tag.setUser(user_id);
		tag.setName(tagName);
		return tag;	
	}
}
