package com.commafeed.backend.cache;

import lombok.Getter;

import org.apache.commons.lang3.StringUtils;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

@Getter
public class RedisPoolFactory {
	private String host = "localhost";
	private int port = Protocol.DEFAULT_PORT;
	private String password = null;
	private int timeout = Protocol.DEFAULT_TIMEOUT;
	private int database = Protocol.DEFAULT_DATABASE;

	private int maxTotal = 500;

	public JedisPool build() {
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(maxTotal);

		return new JedisPool(config, host, port, timeout, StringUtils.trimToNull(password), database);
	}

}
