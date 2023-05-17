package com.commafeed.backend.cache;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

@Getter
public class RedisPoolFactory {

	@JsonProperty
	private String host = "localhost";

	@JsonProperty
	private int port = Protocol.DEFAULT_PORT;

	@JsonProperty
	private String username;

	@JsonProperty
	private String password;

	@JsonProperty
	private int timeout = Protocol.DEFAULT_TIMEOUT;

	@JsonProperty
	private int database = Protocol.DEFAULT_DATABASE;

	@JsonProperty
	private int maxTotal = 500;

	public JedisPool build() {
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(maxTotal);

		JedisClientConfig clientConfig = DefaultJedisClientConfig.builder()
				.user(username)
				.password(password)
				.timeoutMillis(timeout)
				.database(database)
				.build();

		return new JedisPool(poolConfig, new HostAndPort(host, port), clientConfig);
	}

}
