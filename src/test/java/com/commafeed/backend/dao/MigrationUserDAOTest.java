package com.commafeed.backend.dao;

import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;

/**
 * This class has been created to test the migration script for UserDAO class
 * and data...
 */
public class MigrationUserDAOTest {

    @Test
    public void TestForkLiftUserDAO(){
        SessionFactory sessionFactory = mock(SessionFactory.class);
        UserDAO userDAO = new UserDAO(sessionFactory);
        long result = userDAO.count();
        Assert.assertTrue(result > 0);
    }

}
