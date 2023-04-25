package com.commafeed.backend.cache;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

@Getter
public class RedisPoolFactory {
	private final String host = "localhost";
	private final int port = Protocol.DEFAULT_PORT;
	private String password;
	private final int timeout = Protocol.DEFAULT_TIMEOUT;
	private final int database = Protocol.DEFAULT_DATABASE;

	private final int maxTotal = 500;

	public JedisPool build() {
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(maxTotal);

		return new JedisPool(config, host, port, timeout, StringUtils.trimToNull(password), database);
	}

}
