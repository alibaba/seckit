package com.alibaba.seckit.ssrf.adaptor;

import com.alibaba.seckit.SecurityUtil;
import com.alibaba.seckit.ssrf.exception.SSRFUnsafeConnectionException;
import com.alibaba.seckit.util.HttpServerForTest;
import com.squareup.okhttp.ConnectionPool;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.Proxy;
import java.net.URI;
import java.util.concurrent.TimeUnit;

public class OKHttpTest {

    @Test
    public void testInterceptor() throws IOException {
        try (HttpServerForTest server = new HttpServerForTest(0)) {
            server.start();
            OkHttpClient client = SecurityUtil.withSSRFChecking(new OkHttpClient());
            client.setConnectionPool(new ConnectionPool(5, 1, TimeUnit.MINUTES));
            client.setProxy(new Proxy(Proxy.Type.HTTP, server.proxyAddress()));
            client.setConnectTimeout(5, TimeUnit.SECONDS);
            client.setReadTimeout(5, TimeUnit.SECONDS);
            client.setWriteTimeout(5, TimeUnit.SECONDS);
            try {
                assertRequestSuccess(client, server.url("/success"));
                assertRequestSuccess(client, server.redirectTo(server.url("/success")));
                assertRequestSuccess(client, server.redirectTo("/success"));
                for (String host : new String[]{"127.0.0.1", "10.0.0.1", "0.0.0.0", "0"}) {
                    String forbidden = "http://" + host + ":" + server.getPort() + "/forbidden";
                    assertRequestThrowsSecurityException(client, forbidden);
                    assertRequestThrowsSecurityException(client, server.redirectTo(forbidden));
                }
                Assert.assertEquals(0, server.getForbiddenRequestCount());
                assertRequestSuccess(client, server.url("/success"));
            } finally {
                client.getDispatcher().getExecutorService().shutdownNow();
                client.getConnectionPool().evictAll();
            }
        }
    }

    @Ignore("OkHttp 2.7.5 rejects IPv6 Address construction before its SSRF interceptor; do not count that as SSRF coverage")
    @Test
    public void testIpv6Interceptor() throws IOException {
        try (HttpServerForTest server = new HttpServerForTest(0)) {
            server.start();
            OkHttpClient client = SecurityUtil.withSSRFChecking(new OkHttpClient());
            client.setConnectionPool(new ConnectionPool(5, 1, TimeUnit.MINUTES));
            client.setProxy(new Proxy(Proxy.Type.HTTP, server.proxyAddress()));
            client.setConnectTimeout(5, TimeUnit.SECONDS);
            client.setReadTimeout(5, TimeUnit.SECONDS);
            try {
                for (String host : new String[]{"[::1]", "[::]"}) {
                    String forbidden = "http://" + host + ":" + server.getPort() + "/forbidden";
                    assertRequestThrowsSecurityException(client, forbidden);
                    assertRequestThrowsSecurityException(client, server.redirectTo(forbidden));
                }
                Assert.assertEquals(0, server.getForbiddenRequestCount());
            } finally {
                client.getDispatcher().getExecutorService().shutdownNow();
                client.getConnectionPool().evictAll();
            }
        }
    }

    private void assertRequestSuccess(OkHttpClient client, String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        try (ResponseBody body = response.body()) {
            Assert.assertEquals(200, response.code());
            Assert.assertEquals("/success", body.string());
            Assert.assertEquals(URI.create(url).getRawAuthority(), response.header("X-Test-Host"));
        }
    }

    private void assertRequestThrowsSecurityException(OkHttpClient client, String url) throws IOException {
        try {
            assertRequestSuccess(client, url);
            Assert.fail("should reject unsafe URL: " + url);
        } catch (SSRFUnsafeConnectionException expected) {
            // Only rejection by the SSRF checker counts.
        }
    }
}
