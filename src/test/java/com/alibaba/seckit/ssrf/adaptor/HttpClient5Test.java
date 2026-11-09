package com.alibaba.seckit.ssrf.adaptor;

import com.alibaba.seckit.SecurityUtil;
import com.alibaba.seckit.ssrf.exception.SSRFUnsafeConnectionException;
import com.alibaba.seckit.util.HttpServerForTest;
import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleRequestBuilder;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.Timeout;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class HttpClient5Test {

    private static final RequestConfig REQUEST_CONFIG = RequestConfig.custom()
            .setConnectTimeout(Timeout.ofSeconds(5))
            .setConnectionRequestTimeout(Timeout.ofSeconds(5))
            .setResponseTimeout(Timeout.ofSeconds(5)).build();

    @Test
    public void testHttpClient5() throws IOException, ParseException {
        try (HttpServerForTest server = new HttpServerForTest(0)) {
            server.start();
            try (CloseableHttpClient client = SecurityUtil.withSSRFChecking(HttpClients.custom())
                    .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                            .setDnsResolver(fixtureResolver(server)).build())
                    .setDefaultRequestConfig(REQUEST_CONFIG).build()) {
                assertRequestSuccess(client, server.url("/success"));
                assertRequestSuccess(client, server.redirectTo(server.url("/success")));
                assertRequestSuccess(client, server.redirectTo("/success"));
                for (String forbidden : server.forbiddenUrls()) {
                    assertRequestThrowsSecurityException(client, forbidden);
                    assertRequestThrowsSecurityException(client, server.redirectTo(forbidden));
                }
                Assert.assertEquals(0, server.getForbiddenRequestCount());
                assertRequestSuccess(client, server.url("/success"));
            }
        }
    }

    private DnsResolver fixtureResolver(HttpServerForTest server) {
        // Transport mapping only: the production checker still evaluates the public numeric host.
        // Never map a forbidden redirect destination or perform external DNS lookups.
        return new DnsResolver() {
            @Override
            public InetAddress[] resolve(String host) throws UnknownHostException {
                resolveCanonicalHostname(host);
                return new InetAddress[]{server.proxyAddress().getAddress()};
            }

            @Override
            public String resolveCanonicalHostname(String host) throws UnknownHostException {
                if (!HttpServerForTest.PUBLIC_FIXTURE_HOST.equals(host)) {
                    throw new UnknownHostException("unexpected transport destination: " + host);
                }
                return host;
            }
        };
    }

    private void assertRequestSuccess(CloseableHttpClient client, String url) throws IOException, ParseException {
        try (CloseableHttpResponse response = client.execute(new HttpGet(url))) {
            Assert.assertEquals(200, response.getCode());
            Assert.assertEquals("/success", EntityUtils.toString(response.getEntity()));
            Assert.assertEquals(URI.create(url).getRawAuthority(), response.getFirstHeader("X-Test-Host").getValue());
        }
    }

    private void assertRequestThrowsSecurityException(CloseableHttpClient client, String url) throws IOException, ParseException {
        try {
            assertRequestSuccess(client, url);
            Assert.fail("should reject unsafe URL: " + url);
        } catch (SSRFUnsafeConnectionException expected) {
            // Transport errors are not security success.
        }
    }

    @Test
    public void testAsyncClient() throws IOException, ExecutionException, InterruptedException, TimeoutException {
        try (HttpServerForTest server = new HttpServerForTest(0)) {
            server.start();
            assertAsyncRequest(server, server.url("/success"), false);
            assertAsyncRequest(server, server.redirectTo(server.url("/success")), false);
            assertAsyncRequest(server, server.redirectTo("/success"), false);
            for (String forbidden : server.forbiddenUrls()) {
                assertAsyncRequest(server, forbidden, true);
                assertAsyncRequest(server, server.redirectTo(forbidden), true);
            }
            Assert.assertEquals(0, server.getForbiddenRequestCount());
            assertAsyncRequest(server, server.url("/success"), false);
        }
    }

    private void assertAsyncRequest(HttpServerForTest server, String url, boolean shouldBlock)
            throws IOException, ExecutionException, InterruptedException, TimeoutException {
        try (CloseableHttpAsyncClient client = SecurityUtil.withSSRFChecking(HttpAsyncClients.custom())
                .setConnectionManager(PoolingAsyncClientConnectionManagerBuilder.create()
                        .setDnsResolver(fixtureResolver(server)).build())
                .setIOReactorConfig(IOReactorConfig.custom().setIoThreadCount(1).build())
                .setDefaultRequestConfig(REQUEST_CONFIG).build()) {
            client.start();
            Future<SimpleHttpResponse> request = null;
            try {
                request = client.execute(SimpleRequestBuilder.get(url).build(), null);
                SimpleHttpResponse response = request.get(10, TimeUnit.SECONDS);
                if (shouldBlock) {
                    Assert.fail("should reject unsafe URL: " + url);
                }
                Assert.assertEquals(200, response.getCode());
                Assert.assertEquals("/success", response.getBodyText());
                Assert.assertEquals(URI.create(url).getRawAuthority(), response.getFirstHeader("X-Test-Host").getValue());
            } catch (SSRFUnsafeConnectionException e) {
                if (!shouldBlock) {
                    throw e;
                }
            } catch (ExecutionException e) {
                if (!shouldBlock || !(e.getCause() instanceof SSRFUnsafeConnectionException)) {
                    throw e;
                }
            } finally {
                if (request != null) {
                    request.cancel(true);
                }
            }
        }
    }
}
