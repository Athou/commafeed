package com.commafeed.backend;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.CommaFeedVersion;
import com.google.common.net.HttpHeaders;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressNetwork;
import inet.ipaddr.IPAddressString;
import inet.ipaddr.ipv4.IPv4Address;
import inet.ipaddr.ipv6.IPv6Address;

import jakarta.inject.Singleton;

import lombok.RequiredArgsConstructor;

import nl.altindag.ssl.SSLFactory;
import nl.altindag.ssl.apache5.util.Apache5SslUtils;

import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.SystemDefaultDnsResolver;
import org.apache.hc.client5.http.async.methods.InflatingAsyncDataConsumer;
import org.apache.hc.client5.http.async.methods.InflatingGzipDataConsumer;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.TlsConfig;
import org.apache.hc.client5.http.entity.compress.ContentCoding;
import org.apache.hc.client5.http.impl.DefaultRedirectStrategy;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.protocol.RedirectStrategy;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.nio.AsyncDataConsumer;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedMap;
import java.util.function.UnaryOperator;

@Singleton
@RequiredArgsConstructor
public class HttpClientFactory {

    private static final DnsResolver DNS_RESOLVER = SystemDefaultDnsResolver.INSTANCE;
    private static final IPAddress CGNAT_RANGE = new IPAddressString("100.64.0.0/10").getAddress();

    private final CommaFeedConfiguration config;
    private final CommaFeedVersion version;

    public CloseableHttpAsyncClient newClient(int poolSize) {
        PoolingAsyncClientConnectionManager connectionManager =
                newConnectionManager(config, poolSize);
        String userAgent =
                config.httpClient()
                        .userAgent()
                        .orElseGet(() ->
                                String.format(
                                        "CommaFeed/%s (https://github.com/Athou/commafeed)",
                                        version.getVersion()));
        return newClient(config, connectionManager, userAgent);
    }

    private CloseableHttpAsyncClient newClient(
            CommaFeedConfiguration config,
            PoolingAsyncClientConnectionManager connectionManager,
            String userAgent) {
        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader(HttpHeaders.ACCEPT_LANGUAGE, "en"));
        headers.add(new BasicHeader(HttpHeaders.PRAGMA, "No-cache"));
        headers.add(new BasicHeader(HttpHeaders.CACHE_CONTROL, "no-cache"));
        headers.add(new BasicHeader(HttpHeaders.ACCEPT, "*/*"));
        headers.add(new BasicHeader(HttpHeaders.SEC_FETCH_MODE, "navigate"));

        SequencedMap<String, UnaryOperator<AsyncDataConsumer>> contentDecoderMap =
                new LinkedHashMap<>();
        contentDecoderMap.put(
                ContentCoding.DEFLATE.token(), d -> new InflatingAsyncDataConsumer(d, null));
        contentDecoderMap.put(ContentCoding.GZIP.token(), InflatingGzipDataConsumer::new);

        RedirectStrategy redirectStrategy =
                config.httpClient().blockLocalAddresses()
                        ? new BlockLocalAddressesRedirectStrategy(DNS_RESOLVER)
                        : new DefaultRedirectStrategy();

        CloseableHttpAsyncClient client =
                HttpAsyncClients.custom()
                        .disableConnectionState()
                        .useSystemProperties()
                        .disableAutomaticRetries()
                        .disableCookieManagement()
                        .setUserAgent(userAgent)
                        .setDefaultHeaders(headers)
                        .setConnectionManager(connectionManager)
                        .evictExpiredConnections()
                        .evictIdleConnections(
                                TimeValue.of(config.httpClient().idleConnectionsEvictionInterval()))
                        .setContentDecoderMap(new LinkedHashMap<>(contentDecoderMap))
                        .setRedirectStrategy(redirectStrategy)
                        .build();
        client.start();
        return client;
    }

    private PoolingAsyncClientConnectionManager newConnectionManager(
            CommaFeedConfiguration config, int poolSize) {
        SSLFactory sslFactory =
                SSLFactory.builder().withUnsafeTrustMaterial().withUnsafeHostnameVerifier().build();
        DnsResolver dnsResolver =
                config.httpClient().blockLocalAddresses()
                        ? new BlockLocalAddressesDnsResolver(DNS_RESOLVER)
                        : DNS_RESOLVER;

        return PoolingAsyncClientConnectionManagerBuilder.create()
                .setTlsStrategy(Apache5SslUtils.toTlsStrategy(sslFactory))
                .setDefaultConnectionConfig(
                        ConnectionConfig.custom()
                                .setConnectTimeout(Timeout.of(config.httpClient().connectTimeout()))
                                .setSocketTimeout(Timeout.of(config.httpClient().socketTimeout()))
                                .setTimeToLive(
                                        Timeout.of(config.httpClient().connectionTimeToLive()))
                                .build())
                .setDefaultTlsConfig(
                        TlsConfig.custom()
                                .setVersionPolicy(HttpVersionPolicy.NEGOTIATE)
                                .setHandshakeTimeout(
                                        Timeout.of(config.httpClient().sslHandshakeTimeout()))
                                .build())
                .setMaxConnPerRoute(poolSize)
                .setMaxConnTotal(poolSize)
                .setDnsResolver(dnsResolver)
                .build();
    }

    private static boolean isLocalAddress(InetAddress address) {
        return isLocalAddress(new IPAddressNetwork.IPAddressGenerator().from(address));
    }

    private static boolean isLocalAddress(IPAddress ip) {
        if (ip.isLocal() || ip.isLoopback() || ip.isMulticast() || CGNAT_RANGE.contains(ip)) {
            return true;
        }

        if (!ip.isIPv6()) {
            return false;
        }

        // IPv6 transition mechanisms embed an IPv4 address that must be validated too, otherwise
        // they could be used to smuggle a blocked IPv4 target past the IPv6-only checks above
        IPv6Address ipv6 = ip.toIPv6();
        if (ipv6.isIPv4Mapped() || ipv6.isIPv4Compatible() || ipv6.isWellKnownIPv4Translatable()) {
            // IPv4-mapped (::ffff:x.x.x.x), IPv4-compatible (::x.x.x.x) and NAT64
            // (64:ff9b::/96, RFC 6052) addresses all embed the IPv4 address in the lowest 32 bits
            return isLocalAddress(ipv6.getEmbeddedIPv4Address());
        }
        if (ipv6.is6To4()) {
            // 6to4 (2002::/16, RFC 3056) embeds the IPv4 address in bits 16-47
            return isLocalAddress(ipv6.get6To4IPv4Address());
        }
        if (ipv6.isTeredo()) {
            // Teredo (2001::/32, RFC 4380) embeds the IPv4 address in the lowest 32 bits,
            // obfuscated with a bitwise complement
            IPv4Address obfuscated = ipv6.getEmbeddedIPv4Address();
            return isLocalAddress(new IPv4Address(~obfuscated.intValue()));
        }

        return false;
    }

    private record BlockLocalAddressesDnsResolver(DnsResolver delegate) implements DnsResolver {
        @Override
        public InetAddress[] resolve(String host) throws UnknownHostException {
            InetAddress[] addresses = delegate.resolve(host);
            for (InetAddress addr : addresses) {
                if (isLocalAddress(addr)) {
                    throw new UnknownHostException(
                            "Access to local address blocked: " + addr.getHostAddress());
                }
            }
            return addresses;
        }

        @Override
        public String resolveCanonicalHostname(String host) throws UnknownHostException {
            return delegate.resolveCanonicalHostname(host);
        }
    }

    @RequiredArgsConstructor
    private static class BlockLocalAddressesRedirectStrategy extends DefaultRedirectStrategy {

        private final DnsResolver delegate;

        @Override
        public URI getLocationURI(HttpRequest request, HttpResponse response, HttpContext context)
                throws HttpException {
            URI redirectUri = super.getLocationURI(request, response, context);

            String host = redirectUri.getHost();
            if (host == null) {
                throw new HttpException("Redirect URI does not have a host: " + redirectUri);
            }

            InetAddress[] addresses;
            try {
                addresses = delegate.resolve(host);
            } catch (UnknownHostException e) {
                throw new HttpException("Unknown host: " + host);
            }

            for (InetAddress addr : addresses) {
                if (isLocalAddress(addr)) {
                    throw new HttpException(
                            "Access to local address blocked: " + addr.getHostAddress());
                }
            }

            return redirectUri;
        }
    }
}
