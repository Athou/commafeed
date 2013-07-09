package com.commafeed.backend.cache;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.Models;
import com.commafeed.backend.model.User;
import com.commafeed.frontend.model.Category;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.google.api.client.util.Lists;

@Alternative
@ApplicationScoped
public class RedisCacheService extends CacheService {

	private static final Logger log = LoggerFactory
			.getLogger(RedisCacheService.class);

	private JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");
	private ObjectMapper mapper = new ObjectMapper();

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
	public Category getRootCategory(User user) {
		Category cat = null;
		Jedis jedis = pool.getResource();
		try {
			String key = buildRedisRootCategoryKey(user);
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
	public void setRootCategory(User user, Category category) {
		Jedis jedis = pool.getResource();
		try {
			String key = buildRedisRootCategoryKey(user);

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
	public Map<Long, Long> getUnreadCounts(User user) {
		Map<Long, Long> map = null;
		Jedis jedis = pool.getResource();
		try {
			String key = buildRedisUnreadCountKey(user);
			String json = jedis.get(key);
			if (json != null) {
				MapType type = mapper.getTypeFactory().constructMapType(
						Map.class, Long.class, Long.class);
				map = mapper.readValue(json, type);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			pool.returnResource(jedis);
		}
		return map;
	}

	@Override
	public void setUnreadCounts(User user, Map<Long, Long> map) {
		Jedis jedis = pool.getResource();
		try {
			String key = buildRedisUnreadCountKey(user);

			Pipeline pipe = jedis.pipelined();
			pipe.del(key);
			pipe.set(key, mapper.writeValueAsString(map));
			pipe.expire(key, (int) TimeUnit.MINUTES.toSeconds(30));
			pipe.sync();
		} catch (JsonProcessingException e) {
			log.error(e.getMessage(), e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	@Override
	public void invalidateUserData(User... users) {
		Jedis jedis = pool.getResource();
		try {
			Pipeline pipe = jedis.pipelined();
			if (users != null) {
				for (User user : users) {
					String key = buildRedisRootCategoryKey(user);
					pipe.del(key);
					key = buildRedisUnreadCountKey(user);
					pipe.del(key);
				}
			}
			pipe.sync();
		} finally {
			pool.returnResource(jedis);
		}
	}

	private String buildRedisRootCategoryKey(User user) {
		return "root_cat:" + Models.getId(user);
	}

	private String buildRedisUnreadCountKey(User user) {
		return "unread_count:" + Models.getId(user);
	}

	private String buildRedisEntryKey(Feed feed) {
		return "feed:" + feed.getId();
	}

}
