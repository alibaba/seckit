package com.alibaba.seckit.ssrf.adaptor;

import com.alibaba.seckit.SecurityUtil;
import com.alibaba.seckit.util.HttpServerForTest;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class OK3Test {

    @Test
    public void testInterceptor() throws IOException {
        OkHttpClient client = SecurityUtil.withSSRFChecking(new OkHttpClient.Builder()).build();
        HttpServerForTest.startSingleton();
        assertRequestSuccess(client, "https://httpbin.org:443/redirect-to?url=https://www.baidu.com/");
        assertRequestSuccess(client, "https://httpbin.org:443/redirect-to?url=/");
        assertRequestSuccess(client, "http://httpbin.org:443/redirect-to?url=http://223.5.5.5/");
        assertRequestThrowsSecurityException(client, "http://127.0.0.1:8080/");
        assertRequestThrowsSecurityException(client, "https://httpbin.org:443/redirect-to?url=http://0.0.0.0:8080/");
        assertRequestSuccess(client, "https://httpbin.org:443/redirect-to?url=https://www.example.org/");
    }

    private void assertRequestSuccess(OkHttpClient client, String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        Call call = client.newCall(request);
        Response resp = call.execute();
        resp.close();
    }

    private void assertRequestThrowsSecurityException(OkHttpClient client, String url) throws IOException {
        try {
            assertRequestSuccess(client, url);
            Assert.fail();
        } catch (SecurityException ignored) {
        }
    }
}
