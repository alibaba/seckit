package com.alibaba.seckit.ssrf.adaptor;

import com.alibaba.seckit.SecurityUtil;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleRequestBuilder;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class HttpClient5Test {

    @Test
    public void testHttpClient5() throws IOException {
        CloseableHttpClient client = SecurityUtil.withSSRFChecking(HttpClients.custom())
                .build();

        assertRequestSuccess(client, "https://httpbin.org:443/redirect-to?url=https://www.baidu.com/");
        assertRequestSuccess(client, "https://httpbin.org:443/redirect-to?url=/");
        assertRequestSuccess(client, "http://httpbin.org:443/redirect-to?url=http://223.5.5.5/");
        assertRequestThrowsSecurityException(client, "http://10.0.0.1:8080/");
        assertRequestThrowsSecurityException(client, "http://127.0.0.1:8080/");
        assertRequestThrowsSecurityException(client, "https://httpbin.org:443/redirect-to?url=https://10.0.0.1:8080/");
        assertRequestThrowsSecurityException(client, "https://httpbin.org:443/redirect-to?url=https://0.0.0.0:8080/");
        assertRequestSuccess(client, "https://httpbin.org:443/redirect-to?url=https://www.example.org/");

        client.close();
    }

    private void assertRequestSuccess(CloseableHttpClient client, String url) throws IOException {
        HttpGet get = new HttpGet(url);
        CloseableHttpResponse resp = client.execute(get);
        resp.getHeaders();
    }

    private void assertRequestThrowsSecurityException(CloseableHttpClient client, String url) throws IOException {
        try {
            assertRequestSuccess(client, url);
            Assert.fail("should throw security exception, url: " + url);
        } catch (SecurityException ignored) {
        }
    }

    private void assertAsyncRequestSuccess(CloseableHttpAsyncClient client, String uri) throws ExecutionException, InterruptedException, TimeoutException {
        final SimpleHttpRequest request = SimpleRequestBuilder.get().setUri(uri).build();
        client.execute(request, null).get(60, TimeUnit.SECONDS);
    }

    private void assertAsyncRequestThrowsSecurityException(CloseableHttpAsyncClient client, String uri) throws ExecutionException, InterruptedException, TimeoutException {
        try {
            assertAsyncRequestSuccess(client, uri);
            Assert.fail();
        } catch (SecurityException ignored) {
        } catch (ExecutionException e) {
            if (!(e.getCause() instanceof SecurityException)) {
                throw e;
            }
            Thread.sleep(100);
        }
    }

    @Test
    public void testAsyncClient() throws IOException, ExecutionException, InterruptedException, TimeoutException {
        final CloseableHttpAsyncClient client = SecurityUtil.withSSRFChecking(HttpAsyncClients.custom())
                .build();
        client.start();

        assertAsyncRequestSuccess(client, "https://httpbin.org:443/redirect-to?url=https://www.baidu.com/");
        assertAsyncRequestSuccess(client, "https://httpbin.org:443/redirect-to?url=/");
        assertAsyncRequestSuccess(client, "http://httpbin.org:443/redirect-to?url=http://223.5.5.5/");
        assertAsyncRequestThrowsSecurityException(client, "http://10.0.0.1:8080/");
        assertAsyncRequestThrowsSecurityException(client, "http://127.0.0.1:8080/");
        assertAsyncRequestThrowsSecurityException(client, "https://httpbin.org:443/redirect-to?url=https://10.0.0.1:8080/");
        assertAsyncRequestThrowsSecurityException(client, "https://httpbin.org:443/redirect-to?url=https://0.0.0.0:8080/");
        assertAsyncRequestSuccess(client, "http://httpbin.org/redirect-to?url=http://www.example.org/");

        client.close();
    }
}
