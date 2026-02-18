package com.commafeed.backend;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedMap;
import java.util.zip.GZIPInputStream;

import jakarta.inject.Singleton;

import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.SystemDefaultDnsResolver;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.TlsConfig;
import org.apache.hc.client5.http.entity.DeflateInputStream;
import org.apache.hc.client5.http.entity.InputStreamFactory;
import org.apache.hc.client5.http.entity.compress.ContentCoding;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.brotli.dec.BrotliInputStream;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.CommaFeedVersion;
import com.google.common.net.HttpHeaders;

import lombok.RequiredArgsConstructor;
import nl.altindag.ssl.SSLFactory;
import nl.altindag.ssl.apache5.util.Apache5SslUtils;

@Singleton
@RequiredArgsConstructor
public class HttpClientFactory {

	private final CommaFeedConfiguration config;
	private final CommaFeedVersion version;

	public CloseableHttpClient newClient(int poolSize) {
		PoolingHttpClientConnectionManager connectionManager = newConnectionManager(config, poolSize);
		String userAgent = config.httpClient()
				.userAgent()
				.orElseGet(() -> String.format("CommaFeed/%s (https://github.com/Athou/commafeed)", version.getVersion()));
		return newClient(connectionManager, userAgent, config.httpClient().idleConnectionsEvictionInterval());
	}

	private CloseableHttpClient newClient(HttpClientConnectionManager connectionManager, String userAgent,
			Duration idleConnectionsEvictionInterval) {
		List<Header> headers = new ArrayList<>();
		headers.add(new BasicHeader(HttpHeaders.ACCEPT_LANGUAGE, "en"));
		headers.add(new BasicHeader(HttpHeaders.PRAGMA, "No-cache"));
		headers.add(new BasicHeader(HttpHeaders.CACHE_CONTROL, "no-cache"));

		SequencedMap<String, InputStreamFactory> contentDecoderMap = new LinkedHashMap<>();
		contentDecoderMap.put(ContentCoding.GZIP.token(), GZIPInputStream::new);
		contentDecoderMap.put(ContentCoding.DEFLATE.token(), DeflateInputStream::new);
		contentDecoderMap.put(ContentCoding.BROTLI.token(), BrotliInputStream::new);

		return HttpClientBuilder.create()
				.useSystemProperties()
				.disableAutomaticRetries()
				.disableCookieManagement()
				.setUserAgent(userAgent)
				.setDefaultHeaders(headers)
				.setConnectionManager(connectionManager)
				.evictExpiredConnections()
				.evictIdleConnections(TimeValue.of(idleConnectionsEvictionInterval))
				.setContentDecoderRegistry(new LinkedHashMap<>(contentDecoderMap))
				.build();
	}

	private PoolingHttpClientConnectionManager newConnectionManager(CommaFeedConfiguration config, int poolSize) {
		SSLFactory sslFactory = SSLFactory.builder().withUnsafeTrustMaterial().withUnsafeHostnameVerifier().build();
		DnsResolver dnsResolver = config.httpClient().blockLocalAddresses()
				? new BlockLocalAddressesDnsResolver(SystemDefaultDnsResolver.INSTANCE)
				: SystemDefaultDnsResolver.INSTANCE;

		return PoolingHttpClientConnectionManagerBuilder.create()
				.setTlsSocketStrategy(Apache5SslUtils.toTlsSocketStrategy(sslFactory))
				.setDefaultConnectionConfig(ConnectionConfig.custom()
						.setConnectTimeout(Timeout.of(config.httpClient().connectTimeout()))
						.setSocketTimeout(Timeout.of(config.httpClient().socketTimeout()))
						.setTimeToLive(Timeout.of(config.httpClient().connectionTimeToLive()))
						.build())
				.setDefaultTlsConfig(TlsConfig.custom().setHandshakeTimeout(Timeout.of(config.httpClient().sslHandshakeTimeout())).build())
				.setMaxConnPerRoute(poolSize)
				.setMaxConnTotal(poolSize)
				.setDnsResolver(dnsResolver)
				.build();

	}

	private record BlockLocalAddressesDnsResolver(DnsResolver delegate) implements DnsResolver {
		@Override
		public InetAddress[] resolve(String host) throws UnknownHostException {
			InetAddress[] addresses = delegate.resolve(host);
			for (InetAddress addr : addresses) {
				if (isLocalAddress(addr)) {
					throw new UnknownHostException("Access to address blocked: " + addr.getHostAddress());
				}
			}
			return addresses;
		}

		@Override
		public String resolveCanonicalHostname(String host) throws UnknownHostException {
			return delegate.resolveCanonicalHostname(host);
		}

		private boolean isLocalAddress(InetAddress address) {
			return address.isSiteLocalAddress() || address.isAnyLocalAddress() || address.isLinkLocalAddress()
					|| address.isLoopbackAddress() || address.isMulticastAddress();
		}
	}

}
