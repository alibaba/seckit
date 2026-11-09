package com.alibaba.seckit.ssrf.adaptor;

import com.alibaba.seckit.SecurityUtil;
import com.alibaba.seckit.ssrf.exception.SSRFUnsafeConnectionException;
import com.alibaba.seckit.util.HttpServerForTest;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.SystemDefaultHttpClient;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.NHttpClientConnection;
import org.apache.http.nio.protocol.HttpAsyncRequestExecutor;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class HttpClientTest {

    private static final RequestConfig REQUEST_CONFIG = RequestConfig.custom()
            .setConnectTimeout(5000).setConnectionRequestTimeout(5000).setSocketTimeout(5000).build();

    @Test
    public void testHttpClient() throws IOException {
        try (HttpServerForTest server = new HttpServerForTest(0)) {
            server.start();
            try (CloseableHttpClient client = SecurityUtil.withSSRFChecking(HttpClients.custom())
                    .setProxy(new HttpHost("127.0.0.1", server.getPort()))
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

    @Test
    public void testDefaultHttpClient() throws IOException {
        try (HttpServerForTest server = new HttpServerForTest(0)) {
            server.start();
            String url = server.localUrl("/success");
            try (CloseableHttpClient client = new DefaultHttpClient();
                 CloseableHttpClient systemDefaultClient = new SystemDefaultHttpClient()) {
                assertRequestSuccess(client, url);
                assertRequestSuccess(systemDefaultClient, url);
            }
            try (CloseableHttpClient client = SecurityUtil.withSSRFChecking(new DefaultHttpClient());
                 CloseableHttpClient systemDefaultClient = SecurityUtil.withSSRFChecking(new SystemDefaultHttpClient())) {
                assertRequestThrowsSecurityException(client, url);
                assertRequestThrowsSecurityException(systemDefaultClient, url);
            }
        }
    }

    private void assertRequestSuccess(CloseableHttpClient client, String url) throws IOException {
        try (CloseableHttpResponse response = client.execute(new HttpGet(url))) {
            assertResponse(response, url);
        }
    }

    private void assertResponse(HttpResponse response, String url) throws IOException {
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        Assert.assertEquals("/success", EntityUtils.toString(response.getEntity()));
        Assert.assertEquals(URI.create(url).getRawAuthority(), response.getFirstHeader("X-Test-Host").getValue());
    }

    private void assertRequestThrowsSecurityException(CloseableHttpClient client, String url) throws IOException {
        try {
            assertRequestSuccess(client, url);
            Assert.fail("should reject unsafe URL: " + url);
        } catch (SSRFUnsafeConnectionException expected) {
            // Only an actual SSRF rejection counts, not a transport failure.
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

    private CloseableHttpAsyncClient newAsyncClient(HttpServerForTest server, CompletableFuture<HttpResponse> outcome) {
        // HttpAsyncClient 4.x can lose redirect failures instead of completing its Future.
        // Observe the real dispatch exception for this single request; a timeout still fails.
        return SecurityUtil.withSSRFChecking(HttpAsyncClients.custom())
                .setEventHandler(new HttpAsyncRequestExecutor() {
                    @Override
                    public void exception(NHttpClientConnection connection, Exception exception) {
                        outcome.completeExceptionally(exception);
                        super.exception(connection, exception);
                    }
                })
                .setDefaultIOReactorConfig(IOReactorConfig.custom().setIoThreadCount(1).build())
                .setProxy(new HttpHost("127.0.0.1", server.getPort()))
                .setDefaultRequestConfig(REQUEST_CONFIG).build();
    }

    private void assertAsyncRequest(HttpServerForTest server, String url, boolean shouldBlock)
            throws IOException, ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<HttpResponse> outcome = new CompletableFuture<>();
        Future<HttpResponse> request = null;
        try (CloseableHttpAsyncClient client = newAsyncClient(server, outcome)) {
            client.start();
            try {
                request = client.execute(new HttpGet(url), new FutureCallback<HttpResponse>() {
                    @Override
                    public void completed(HttpResponse response) {
                        outcome.complete(response);
                    }

                    @Override
                    public void failed(Exception exception) {
                        outcome.completeExceptionally(exception);
                    }

                    @Override
                    public void cancelled() {
                        outcome.cancel(false);
                    }
                });
                HttpResponse response = outcome.get(10, TimeUnit.SECONDS);
                if (shouldBlock) {
                    EntityUtils.consume(response.getEntity());
                    Assert.fail("should reject unsafe URL: " + url);
                }
                assertResponse(response, url);
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
