package com.alibaba.seckit.ssrf.adaptor;

import com.alibaba.seckit.SecurityUtil;
import com.alibaba.seckit.util.HttpServerForTest;
import com.alibaba.seckit.util.IpAddressUtilForTest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.*;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class HttpClientTest {

    @Test
    public void testHttpClient() throws IOException {
        HttpClientBuilder builder = SecurityUtil.withSSRFChecking(HttpClients.custom())
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
        CloseableHttpClient client = builder.build();
        assertRequestSuccess(client, "https://httpbin.org:443/redirect-to?url=https://www.baidu.com/");
        assertRequestSuccess(client, "http://httpbin.org:443/redirect-to?url=http://223.5.5.5/");
        assertRequestThrowsSecurityException(client, "http://10.0.0.1:8080/");
        assertRequestThrowsSecurityException(client, "http://127.0.0.1:8080/");
        assertRequestThrowsSecurityException(client, "https://httpbin.org:443/redirect-to?url=https://10.0.0.1:8080/");
        assertRequestThrowsSecurityException(client, "https://httpbin.org:443/redirect-to?url=https://0.0.0.0:8080/");
        assertRequestSuccess(client, "https://httpbin.org:443/redirect-to?url=https://www.example.org/");
        assertRequestThrowsSecurityException(client, "https://httpbin.org:443/redirect-to?url=http://0/");

        assertRequestThrowsSecurityException(client, "http://[::1]/");
        assertRequestThrowsSecurityException(client, "https://httpbin.org:443/redirect-to?url=http://[::]/");
        assertRequestThrowsSecurityException(client, "https://httpbin.org:443/redirect-to?url=http://[::1]/");


        assertRequestSuccess(client, "https://httpbin.org:443/redirect-to?url=https://www.baidu.com/");
        client.close();
    }

    @Test
    public void testDefaultHttpClient() throws IOException {
        HttpServerForTest.startSingleton();
        CloseableHttpClient client = new DefaultHttpClient();
        CloseableHttpClient systemDefaultClient = new SystemDefaultHttpClient();
        assertRequestSuccess(client, "http://127.0.0.1:8080");
        assertRequestSuccess(systemDefaultClient, "http://127.0.0.1:8080");

        client.close();
        systemDefaultClient.close();

        client = SecurityUtil.withSSRFChecking(new DefaultHttpClient());
        systemDefaultClient = SecurityUtil.withSSRFChecking(new SystemDefaultHttpClient());
        assertRequestThrowsSecurityException(client, "http://127.0.0.1:8080");
        assertRequestThrowsSecurityException(systemDefaultClient, "http://127.0.0.1:8080");
    }

    private void assertRequestSuccess(CloseableHttpClient client, String url) throws IOException {
        HttpGet get = new HttpGet(url);
        client.execute(get);
    }

    private void assertRequestThrowsSecurityException(CloseableHttpClient client, String url) throws IOException {
        try {
            assertRequestSuccess(client, url);
            Assert.fail("should throw security exception, url: " + url);
        } catch (SecurityException ignored) {
        }
    }

    @Test
    public void testAsyncClient() throws IOException, ExecutionException, InterruptedException {
        CloseableHttpAsyncClient client = SecurityUtil.withSSRFChecking(HttpAsyncClients.custom())
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();
        client.start();
        assertAsyncRequestSuccess(client, "https://httpbin.org:443/redirect-to?url=https://www.baidu.com/");
        assertAsyncRequestSuccess(client, "http://httpbin.org:443/redirect-to?url=http://223.5.5.5/");
        assertAsyncRequestThrowsSecurityException(client, "http://10.0.0.1:8080/");
        assertAsyncRequestThrowsSecurityException(client, "http://127.0.0.1:8080/");
        assertAsyncRequestThrowsSecurityException(client, "https://httpbin.org:443/redirect-to?url=https://10.0.0.1:8080/");
        assertAsyncRequestThrowsSecurityException(client, "https://httpbin.org:443/redirect-to?url=https://0.0.0.0:8080/");
        assertAsyncRequestSuccess(client, "https://httpbin.org:443/redirect-to?url=https://www.example.org/");
        assertAsyncRequestThrowsSecurityException(client, "https://httpbin.org:443/redirect-to?url=http://0/");
    }

    private void assertAsyncRequestSuccess(CloseableHttpAsyncClient client, String url) throws ExecutionException, InterruptedException {
        HttpGet get = new HttpGet(url);
        client.execute(get, null).get();
    }

    private void assertAsyncRequestThrowsSecurityException(CloseableHttpAsyncClient client, String url) {
        Future<HttpResponse> responseFuture = null;
        try {
            HttpGet get = new HttpGet(url);

            responseFuture = client.execute(get, null);
            responseFuture.get(10, TimeUnit.SECONDS);
            Assert.fail("should throw security exception, url: " + url);

        } catch (SecurityException | TimeoutException ignored) {
            // http async client 4.x 版本实现有bug，有些场景中抛出异常时无法处理，get()会一直block，callback的onFail也处理不到。
        } catch (ExecutionException e) {
            if (!(e.getCause() instanceof SecurityException)) {
                throw new RuntimeException(e);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
