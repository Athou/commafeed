package com.commafeed.backend.dao;


import com.commafeed.backend.dao.datamigrationtoggles.MigrationToggles;
import com.commafeed.backend.dao.newstorage.UserStorage;
import com.commafeed.backend.model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

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
