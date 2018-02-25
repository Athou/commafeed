package A2Test;

import static org.junit.Assert.*;

import org.apache.commons.lang3.time.DateUtils;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

import com.commafeed.backend.service.FeedSubscriptionService;
import com.commafeed.backend.service.internal.PostLoginActivities;
import com.ibm.icu.util.Calendar;

import lombok.RequiredArgsConstructor;


import java.util.Collection;
import java.util.Date;

import javax.inject.Inject;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.model.User;


@RequiredArgsConstructor(onConstructor = @__({ @Inject }))


public class TestPostLoginActivities {
	boolean value = true;
	UserDAO mockUserDAO;
	FeedSubscriptionService mockFeedSubscriptionService;
	CommaFeedConfiguration mockConfig;
	User testUser;
	PostLoginActivities postLoginaActivity;
	Date now = new Date();
	Date lastLogin;
	Calendar myCal;
	
	@Before
	public void setUp() 
	{ 
		mockUserDAO = mock(UserDAO.class);
		mockFeedSubscriptionService = mock(FeedSubscriptionService.class);
		mockConfig = mock(CommaFeedConfiguration.class, Mockito.RETURNS_DEEP_STUBS);
		
		testUser = new User();
		
		postLoginaActivity = new PostLoginActivities(mockUserDAO, mockFeedSubscriptionService, mockConfig);
		
		Calendar myCal = Calendar.getInstance();
		myCal.set(Calendar.YEAR, 2018);
		myCal.set(Calendar.MONTH, 01);
		myCal.set(Calendar.DAY_OF_MONTH, 13);
		lastLogin = myCal.getTime();
	}	
	
	@Test
	public void testUserWithNoPreviousLogin() 
	{
		
		System.out.println("Initial login = " + testUser.getLastLogin());
		postLoginaActivity.executeFor(testUser);
		
		//after execute
		System.out.println("New Login = " + testUser.getLastLogin());
		//date is the same but needs format to but get TRUE, getDate() works fine for now
		assertEquals(testUser.getLastLogin().getDate(), now.getDate());
	}
	
	@Test
	public void testUserWithPreviousLogin() 
	{
		
		//set LastLogin to Feb 11 2018
		testUser.setLastLogin(lastLogin);
		//System.out.println(testUser.getLastLogin());
		
		//stub with TRUE for second IF
		when(mockConfig.getApplicationSettings().getHeavyLoad()).thenReturn(value);
		
		postLoginaActivity.executeFor(testUser);
		
		//date is the same but needs format to but get TRUE, getDate() works fine for now
		assertEquals(testUser.getLastFullRefresh().getDate(), now.getDate());
	}
	
}
