package com.alibaba.seckit.ssrf;

import com.alibaba.seckit.SecurityUtil;
import com.alibaba.seckit.ssrf.exception.SSRFUnsafeConnectionException;
import com.alibaba.seckit.util.HttpServerForTest;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.fail;

public class SSRFTest {

    @Test
    public void testStartNetHooksWithThreadLocal() throws IOException {
        HttpServerForTest.startSingleton();
        String url = "http://127.0.0.1:8080";
        (new URL(url)).openConnection().getInputStream();

        SecurityUtil.startSSRFNetHookChecking();
        url = "http://127.0.0.1:8080";
        try {
            (new URL(url)).openConnection().getInputStream();
            fail("should throw SSRFUnsafeConnectionException");
        } catch (SSRFUnsafeConnectionException ignored) {}
        SecurityUtil.stopSSRFNetHookChecking();
    }

}
