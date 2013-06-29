package com.commafeed.backend.cache;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;

import com.commafeed.backend.model.Feed;
import com.google.api.client.util.Lists;

@Alternative
@ApplicationScoped
public class RedisCacheService extends CacheService {

	private JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");

	@Override
	public List<String> getLastEntries(Feed feed) {
		List<String> list = Lists.newArrayList();
		Jedis jedis = pool.getResource();
		try {
			String key = buildKey(feed);
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
			String key = buildKey(feed);

			Pipeline pipe = jedis.pipelined();
			pipe.del(key);
			for (String entry : entries) {
				pipe.sadd(key, entry);
			}
			pipe.expire(key, (int) TimeUnit.HOURS.toSeconds(24));
			pipe.sync();
		} finally {
			pool.returnResource(jedis);
		}
	}

	private String buildKey(Feed feed) {
		return "feed:" + feed.getId();
	}

}
