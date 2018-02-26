package MockingTest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.commafeed.backend.cache.RedisCacheService;
import com.commafeed.backend.model.Feed;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class RedisCacheServiceTest {
	
	JedisPool mockpool;
	Feed feedMock;
	Jedis j;
	Set<String> members;
	
	
	@Before
	public void setUp() {
		mockpool = mock(JedisPool.class);
		feedMock = mock(Feed.class);
		j = mock(Jedis.class);
		members = new HashSet<String>();
        members.add("Chris");
	}
	
	
	@Test
    public void testGetLastEntries() {
		when(mockpool.getResource()).thenReturn(j);
        when(feedMock.getId()).thenReturn(1L);
        when(j.smembers("f:1")).thenReturn(members);
        RedisCacheService rcs = new RedisCacheService(mockpool);

        List<String> tempList = new  ArrayList<>();
        tempList = rcs.getLastEntries(feedMock);
        assertEquals(tempList.get(0),"Chris");
        
    }
}