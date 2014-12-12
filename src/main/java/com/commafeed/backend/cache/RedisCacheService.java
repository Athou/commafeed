package com.commafeed.backend.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.Models;
import com.commafeed.backend.model.User;
import com.commafeed.frontend.model.Category;
import com.commafeed.frontend.model.UnreadCount;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@RequiredArgsConstructor
public class RedisCacheService extends CacheService {

	private static ObjectMapper MAPPER = new ObjectMapper();

	private final JedisPool pool;

	@Override
	public List<String> getLastEntries(Feed feed) {
		List<String> list = new ArrayList<>();
		try (Jedis jedis = pool.getResource()) {
			String key = buildRedisEntryKey(feed);
			Set<String> members = jedis.smembers(key);
			for (String member : members) {
				list.add(member);
			}
		}
		return list;
	}

	@Override
	public void setLastEntries(Feed feed, List<String> entries) {
		try (Jedis jedis = pool.getResource()) {
			String key = buildRedisEntryKey(feed);

			Pipeline pipe = jedis.pipelined();
			pipe.del(key);
			for (String entry : entries) {
				pipe.sadd(key, entry);
			}
			pipe.expire(key, (int) TimeUnit.DAYS.toSeconds(7));
			pipe.sync();
		}
	}

	@Override
	public Category getUserRootCategory(User user) {
		Category cat = null;
		try (Jedis jedis = pool.getResource()) {
			String key = buildRedisUserRootCategoryKey(user);
			String json = jedis.get(key);
			if (json != null) {
				cat = MAPPER.readValue(json, Category.class);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return cat;
	}

	@Override
	public void setUserRootCategory(User user, Category category) {
		try (Jedis jedis = pool.getResource()) {
			String key = buildRedisUserRootCategoryKey(user);

			Pipeline pipe = jedis.pipelined();
			pipe.del(key);
			pipe.set(key, MAPPER.writeValueAsString(category));
			pipe.expire(key, (int) TimeUnit.MINUTES.toSeconds(30));
			pipe.sync();
		} catch (JsonProcessingException e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public UnreadCount getUnreadCount(FeedSubscription sub) {
		UnreadCount count = null;
		try (Jedis jedis = pool.getResource()) {
			String key = buildRedisUnreadCountKey(sub);
			String json = jedis.get(key);
			if (json != null) {
				count = MAPPER.readValue(json, UnreadCount.class);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return count;
	}

	@Override
	public void setUnreadCount(FeedSubscription sub, UnreadCount count) {
		try (Jedis jedis = pool.getResource()) {
			String key = buildRedisUnreadCountKey(sub);

			Pipeline pipe = jedis.pipelined();
			pipe.del(key);
			pipe.set(key, MAPPER.writeValueAsString(count));
			pipe.expire(key, (int) TimeUnit.MINUTES.toSeconds(30));
			pipe.sync();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void invalidateUserRootCategory(User... users) {
		try (Jedis jedis = pool.getResource()) {
			Pipeline pipe = jedis.pipelined();
			if (users != null) {
				for (User user : users) {
					String key = buildRedisUserRootCategoryKey(user);
					pipe.del(key);
				}
			}
			pipe.sync();
		}
	}

	@Override
	public void invalidateUnreadCount(FeedSubscription... subs) {
		try (Jedis jedis = pool.getResource()) {
			Pipeline pipe = jedis.pipelined();
			if (subs != null) {
				for (FeedSubscription sub : subs) {
					String key = buildRedisUnreadCountKey(sub);
					pipe.del(key);
				}
			}
			pipe.sync();
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
