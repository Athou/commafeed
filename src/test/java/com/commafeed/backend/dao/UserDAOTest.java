package com.commafeed.backend.dao;


import com.commafeed.backend.model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class UserDAOTest extends AbstractDAOTest {

    private static UserDAO userDAO;

    @BeforeClass
    public static void beforeClass() {
        userDAO = new UserDAO(createSessionFactory(User.class));
    }

    @Before
    public void beforeEachTest() {
        // This opens up the session with the database before each individual
        // tests.
        // It can run more than one query at a time.
        beginTransaction();
    }

    @After
    public void afterEachTest() {
        // This closes up the session with the database after each
        // individual tests.
        closeTransaction();
    }

    @Test
    public void test() {
        // DO SOMETHING
    }
}
