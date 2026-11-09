package com.alibaba.seckit.ssrf;

import com.alibaba.seckit.SecurityUtil;
import com.alibaba.seckit.ssrf.exception.SSRFUnsafeConnectionException;
import com.alibaba.seckit.util.HttpServerForTest;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import static org.junit.Assert.fail;

public class SSRFTest {

    @Test
    public void testStartNetHooksWithThreadLocal() throws IOException {
        try (HttpServerForTest server = new HttpServerForTest(0)) {
            server.start();
            String url = server.localUrl("/success");
            URLConnection baseline = new URL(url).openConnection();
            baseline.setConnectTimeout(5000);
            baseline.setReadTimeout(5000);
            // Force a fresh TCP connection below instead of reusing HTTP keep-alive.
            baseline.setRequestProperty("Connection", "close");
            try (InputStream input = baseline.getInputStream()) {
                input.read();
            }

            SecurityUtil.startSSRFNetHookChecking();
            try {
                URLConnection guarded = new URL(url).openConnection();
                guarded.setConnectTimeout(5000);
                guarded.setReadTimeout(5000);
                try (InputStream input = guarded.getInputStream()) {
                    fail("should throw SSRFUnsafeConnectionException");
                }
            } catch (SSRFUnsafeConnectionException expected) {
            } finally {
                SecurityUtil.stopSSRFNetHookChecking();
            }
        }
    }

}
