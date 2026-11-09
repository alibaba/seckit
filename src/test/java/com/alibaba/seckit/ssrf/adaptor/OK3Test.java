package com.alibaba.seckit.ssrf.adaptor;

import com.alibaba.seckit.SecurityUtil;
import com.alibaba.seckit.ssrf.exception.SSRFUnsafeConnectionException;
import com.alibaba.seckit.util.HttpServerForTest;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.Proxy;
import java.net.URI;
import java.util.concurrent.TimeUnit;

public class OK3Test {

    @Test
    public void testInterceptor() throws IOException {
        try (HttpServerForTest server = new HttpServerForTest(0)) {
            server.start();
            OkHttpClient client = SecurityUtil.withSSRFChecking(new OkHttpClient.Builder())
                    .proxy(new Proxy(Proxy.Type.HTTP, server.proxyAddress()))
                    .connectTimeout(5, TimeUnit.SECONDS).readTimeout(5, TimeUnit.SECONDS)
                    .callTimeout(10, TimeUnit.SECONDS).build();
            try {
                assertRequestSuccess(client, server.url("/success"));
                assertRequestSuccess(client, server.redirectTo(server.url("/success")));
                assertRequestSuccess(client, server.redirectTo("/success"));
                for (String forbidden : server.forbiddenUrls()) {
                    assertRequestThrowsSecurityException(client, forbidden);
                    assertRequestThrowsSecurityException(client, server.redirectTo(forbidden));
                }
                Assert.assertEquals(0, server.getForbiddenRequestCount());
                assertRequestSuccess(client, server.url("/success"));
            } finally {
                client.dispatcher().executorService().shutdownNow();
                client.connectionPool().evictAll();
            }
        }
    }

    private void assertRequestSuccess(OkHttpClient client, String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            Assert.assertEquals(200, response.code());
            Assert.assertEquals("/success", response.body().string());
            Assert.assertEquals(URI.create(url).getRawAuthority(), response.header("X-Test-Host"));
        }
    }

    private void assertRequestThrowsSecurityException(OkHttpClient client, String url) throws IOException {
        try {
            assertRequestSuccess(client, url);
            Assert.fail("should reject unsafe URL: " + url);
        } catch (SSRFUnsafeConnectionException expected) {
            // Keep checking redirects; do not accept connection failures as SSRF rejection.
        }
    }
}
