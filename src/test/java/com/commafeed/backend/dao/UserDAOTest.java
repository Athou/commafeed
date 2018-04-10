package com.commafeed.backend.dao;


import com.commafeed.backend.dao.datamigrationtoggles.MigrationToggles;
import com.commafeed.backend.dao.newstorage.UserStorage;
import com.commafeed.backend.model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class UserDAOTest extends AbstractDAOTest {

    private static UserDAO userDAO;
    private UserStorage userStorage;
    private static User user1;
    private static User user2;

    @BeforeClass
    public static void beforeClass() {
        userDAO = new UserDAO(createSessionFactory(User.class));
        MigrationToggles.turnAllTogglesOff();
    }

    @Before
    public void beforeEachTest() {
        // This opens up the session with the database before each individual
        // tests.
        // It can run more than one query at a time.
        beginTransaction();
        this.userStorage = UserStorage.getInstance();
        userDAO.supercedeIStorageModelDAOForTests(this.userStorage);
    }

    @After
    public void afterEachTest() {
        // This closes up the session with the database after each
        // individual tests.
        closeTransaction();
    }

    @Test
    public void testForklift() {
        MigrationToggles.turnForkLiftOn();

        // Putting some users in the database
        user1 = getUser("Hello", "hello@gmail.com");
        userDAO.saveOrUpdate(user1);
        user2 = getUser("Hello2", "hello2@gmail.com");
        userDAO.saveOrUpdate(user2);

        // Forklifting the data from the database to the storage
        userDAO.forklift();

        // Cleanup the database afterwards
        userDAO.delete(user1);
        userDAO.delete(user2);

        // Checking that the data in the storage is ok
        assert(this.userStorage.exists(user1));
        assert(this.userStorage.exists(user2));
        assert(this.userStorage.read(user1).equals(user1));
        assert(this.userStorage.read(user2).equals(user2));
    }

    @Test
    public void testConsistencyCheck() {
        MigrationToggles.turnConsistencyCheckerOn();

        // Putting some users in the database
        user1 = getUser("Hello", "hello@gmail.com");
        userDAO.saveOrUpdate(user1);
        user2 = getUser("Hello2", "hello2@gmail.com");
        userDAO.saveOrUpdate(user2);

        // Forklifting the data from the database to the storage
        userDAO.forklift();

        // Checking that the data in the storage is ok
        assert(this.userStorage.exists(user1));
        assert(this.userStorage.exists(user2));
        assert(this.userStorage.read(user1).equals(user1));
        assert(this.userStorage.read(user2).equals(user2));

        // Corrupting the data in the datastorage
        User user3 = getUser("Hiiiiii", "hello@gmail.com");
        user3.setId(user1.getId());
        this.userStorage.update(user3);
        User user4 = getUser("another name", "hello@gmail.com");
        user4.setId(user2.getId());
        this.userStorage.update(user4);

        // First time, there should be two inconsistencies
        assertEquals(2, userDAO.consistencyChecker());

        // Second time, there should be no inconsistency
        assertEquals(0, userDAO.consistencyChecker());

        userDAO.delete(user1);
        userDAO.delete(user2);
    }

    @Test
    public void testShadowWrites() {
        MigrationToggles.turnShadowWritesOn();

        // Putting some users in the database and in the storage
        user1 = getUser("Hello", "hello@gmail.com");
        userDAO.saveOrUpdate(user1);
        user2 = getUser("Hello2", "hello2@gmail.com");
        userDAO.saveOrUpdate(user2);

        // Checking that the data in the storage is ok
        assert(this.userStorage.exists(user1));
        assert(this.userStorage.exists(user2));
        assert(this.userStorage.read(user1).equals(user1));
        assert(this.userStorage.read(user2).equals(user2));

        userDAO.delete(user1);
        userDAO.delete(user2);
    }

    @Test
    public void testShadowReads() {
        MigrationToggles.turnShadowReadsOn();

        // Putting some users in the database and in the storage
        user1 = getUser("Hello", "hello@gmail.com");
        userDAO.saveOrUpdate(user1);
        user2 = getUser("Hello2", "hello2@gmail.com");
        userDAO.saveOrUpdate(user2);

        // Checking that the data in the storage is ok
        assert(this.userStorage.exists(user1));
        assert(this.userStorage.exists(user2));
        assert(this.userStorage.read(user1).equals(user1));
        assert(this.userStorage.read(user2).equals(user2));

        // Pulling data from the db with the read method
        User user3 = userDAO.findById(user1.getId());

        assert(user3.equals(user1));

        // Corrupting the data from the storage and checking that the error
        // is automatically corrected
        User user4 = getUser("New user", "Corruption!");
        user4.setId(user2.getId());
        this.userStorage.update(user4);

        assertNotEquals(user4, user2);

        // Reading the user
        User user5 = userDAO.findById(user2.getId());

        // Now the corrupted data should have been corrected
        assertEquals(user2, user5);

        // Waiting for the asynchronous call to finish
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(this.userStorage.read(user4.getId()), user5);

        userDAO.delete(user1);
        userDAO.delete(user2);
    }

    @Test
    public void testReadAndWriteMigration() {
        MigrationToggles.turnShadowReadsOn();

        // Putting some users in the database and local storage
        user1 = getUser("Hello", "hello@gmail.com");
        userDAO.saveOrUpdate(user1);
        user2 = getUser("Hello2", "hello2@gmail.com");
        userDAO.saveOrUpdate(user2);

        // Getting the number of entries in the database
        int totalEntry = userDAO.findAll().size();

        // Checking that the data in the storage is ok
        assert(this.userStorage.exists(user1));
        assert(this.userStorage.exists(user2));
        assert(this.userStorage.read(user1).equals(user1));
        assert(this.userStorage.read(user2).equals(user2));

        // Corrupting the data in the datastorage
        User user3 = getUser("Hiiiiii", "hello@gmail.com");
        user3.setId(user1.getId());
        this.userStorage.update(user3);
        User user4 = getUser("another name", "hello@gmail.com");
        user4.setId(user2.getId());
        this.userStorage.update(user4);

        int inconsistencyCounter = 0;
        double threshold = 1;

        // First time, there should be two inconsistencies
        assertEquals(2, inconsistencyCounter = userDAO.consistencyChecker());
        int entriesChecked = 0;
        do {
            entriesChecked += totalEntry;
            threshold = inconsistencyCounter / totalEntry;
            // Other times, there should be no inconsistency
            assertEquals(0, inconsistencyCounter = userDAO.consistencyChecker
                    ());
        } while(threshold > 0.01);

        // Now that the inconsistencies are below a certain threshold, we can
        // discard the old database
        MigrationToggles.turnReadAndWriteOn();

        // Removing the user1 from the storage
        userDAO.delete(user1);
        assert(!this.userStorage.exists(user1));

        // Turning off the toggles to check that the user1 in the db wasn't
        // affected
        MigrationToggles.turnAllTogglesOff();

        // Getting the user1 from the database
        User user6 = userDAO.findById(user1.getId());
        // Result should be equal to user 1
        assertEquals(user1, user6);

        userDAO.delete(user1);
        userDAO.delete(user2);
    }


    @Test
    public void testLongTermConsistencyCheck() {
        MigrationToggles.turnLongTermConsistencyOn();

        // Putting some users in the database
        user1 = getUser("Hello", "hello@gmail.com");
        user1.setId(1L);
        userDAO.saveOrUpdate(user1);
        user2 = getUser("Hello2", "hello2@gmail.com");
        user2.setId(2L);
        userDAO.saveOrUpdate(user2);

        HashMap<Long, User> longTermHashMapConsistencyChecker = new
                HashMap<Long, User>();

        longTermHashMapConsistencyChecker.put(user1.getId(), user1);
        longTermHashMapConsistencyChecker.put(user2.getId(), user2);

        userDAO.setLongTermHashMap(longTermHashMapConsistencyChecker);

        // Checking that the data in the storage is ok
        assert(this.userStorage.exists(user1));
        assert(this.userStorage.exists(user2));
        assert(this.userStorage.read(user1).equals(user1));
        assert(this.userStorage.read(user2).equals(user2));

        // Corrupting the data in the datastorage
        User user3 = getUser("Hiiiiii", "hello@gmail.com");
        user3.setId(user1.getId());
        this.userStorage.update(user3);
        User user4 = getUser("another name", "hello@gmail.com");
        user4.setId(user2.getId());
        this.userStorage.update(user4);

        // First time, there should be two inconsistencies
        assertEquals(2, userDAO.consistencyChecker());

        // Second time, there should be no inconsistency
        assertEquals(0, userDAO.consistencyChecker());
    }

    private static User getUser(String name, String email) {
        User user = new User();
        Date date = new Date(000000000);
        user.setApiKey("ApiKey");
        user.setCreated(date);
        user.setDisabled(false);
        user.setEmail(email);
        Date dateFullRefresh = new Date(000000123);
        user.setLastFullRefresh(dateFullRefresh);
        Date dateLastLogin = new Date(001234567);
        user.setLastLogin(dateLastLogin);
        user.setName(name);
        byte[] passwordArray = "Hello".getBytes();
        user.setPassword(passwordArray);
        user.setRecoverPasswordToken("token");
        Date dateToken = new Date(12345678);
        user.setRecoverPasswordTokenDate(dateToken);
        byte[] saltArray = "Salt".getBytes();
        user.setSalt(saltArray);
        return user;
    }
}
