package com.commafeed.config;

import java.util.Optional;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

@ConfigMapping(prefix = "redis")
public interface RedisConfiguration {

	@WithDefault("localhost")
	String host();

	@WithDefault("" + Protocol.DEFAULT_PORT)
	int port();

	Optional<String> username();

	Optional<String> password();

	@WithDefault("" + Protocol.DEFAULT_TIMEOUT)
	int timeout();

	@WithDefault("" + Protocol.DEFAULT_DATABASE)
	int database();

	@WithDefault("500")
	int maxTotal();

	default JedisPool build() {
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(maxTotal());

		JedisClientConfig clientConfig = DefaultJedisClientConfig.builder()
				.user(username().orElse(null))
				.password(password().orElse(null))
				.timeoutMillis(timeout())
				.database(database())
				.build();

		return new JedisPool(poolConfig, new HostAndPort(host(), port()), clientConfig);
	}
}
