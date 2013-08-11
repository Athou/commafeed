package com.commafeed.backend.cache;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.Models;
import com.commafeed.backend.model.User;
import com.commafeed.frontend.model.Category;
import com.commafeed.frontend.model.UnreadCount;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

@Alternative
@ApplicationScoped
@Slf4j
public class RedisCacheService extends CacheService {

	private static ObjectMapper mapper = new ObjectMapper();

	private JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");

	@Override
	public List<String> getLastEntries(Feed feed) {
		List<String> list = Lists.newArrayList();
		Jedis jedis = pool.getResource();
		try {
			String key = buildRedisEntryKey(feed);
			Set<String> members = jedis.smembers(key);
			for (String member : members) {
				list.add(member);
			}
		} finally {
			pool.returnResource(jedis);
		}
		return list;
	}

	@Override
	public void setLastEntries(Feed feed, List<String> entries) {
		Jedis jedis = pool.getResource();
		try {
			String key = buildRedisEntryKey(feed);

			Pipeline pipe = jedis.pipelined();
			pipe.del(key);
			for (String entry : entries) {
				pipe.sadd(key, entry);
			}
			pipe.expire(key, (int) TimeUnit.DAYS.toSeconds(7));
			pipe.sync();
		} finally {
			pool.returnResource(jedis);
		}
	}

	@Override
	public Category getUserRootCategory(User user) {
		Category cat = null;
		Jedis jedis = pool.getResource();
		try {
			String key = buildRedisUserRootCategoryKey(user);
			String json = jedis.get(key);
			if (json != null) {
				cat = mapper.readValue(json, Category.class);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			pool.returnResource(jedis);
		}
		return cat;
	}

	@Override
	public void setUserRootCategory(User user, Category category) {
		Jedis jedis = pool.getResource();
		try {
			String key = buildRedisUserRootCategoryKey(user);

			Pipeline pipe = jedis.pipelined();
			pipe.del(key);
			pipe.set(key, mapper.writeValueAsString(category));
			pipe.expire(key, (int) TimeUnit.MINUTES.toSeconds(30));
			pipe.sync();
		} catch (JsonProcessingException e) {
			log.error(e.getMessage(), e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	@Override
	public UnreadCount getUnreadCount(FeedSubscription sub) {
		UnreadCount count = null;
		Jedis jedis = pool.getResource();
		try {
			String key = buildRedisUnreadCountKey(sub);
			String json = jedis.get(key);
			if (json != null) {
				count = mapper.readValue(json, UnreadCount.class);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			pool.returnResource(jedis);
		}
		return count;
	}

	@Override
	public void setUnreadCount(FeedSubscription sub, UnreadCount count) {
		Jedis jedis = pool.getResource();
		try {
			String key = buildRedisUnreadCountKey(sub);

			Pipeline pipe = jedis.pipelined();
			pipe.del(key);
			pipe.set(key, mapper.writeValueAsString(count));
			pipe.expire(key, (int) TimeUnit.MINUTES.toSeconds(30));
			pipe.sync();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	@Override
	public void invalidateUserRootCategory(User... users) {
		Jedis jedis = pool.getResource();
		try {
			Pipeline pipe = jedis.pipelined();
			if (users != null) {
				for (User user : users) {
					String key = buildRedisUserRootCategoryKey(user);
					pipe.del(key);
				}
			}
			pipe.sync();
		} finally {
			pool.returnResource(jedis);
		}
	}

	@Override
	public void invalidateUnreadCount(FeedSubscription... subs) {
		Jedis jedis = pool.getResource();
		try {
			Pipeline pipe = jedis.pipelined();
			if (subs != null) {
				for (FeedSubscription sub : subs) {
					String key = buildRedisUnreadCountKey(sub);
					pipe.del(key);
				}
			}
			pipe.sync();
		} finally {
			pool.returnResource(jedis);
		}
	}

	private String buildRedisEntryKey(Feed feed) {
		return "f:" + Models.getId(feed);
	}

	private String buildRedisUserRootCategoryKey(User user) {
		return "c:" + Models.getId(user);
	}

	private String buildRedisUnreadCountKey(FeedSubscription sub) {
		return "u:" + Models.getId(sub);
	}

}
