package com.alibaba.seckit.ssrf.interceptor;

import com.alibaba.seckit.SecurityUtil;
import com.alibaba.seckit.ssrf.SSRFChecker;
import com.alibaba.seckit.ssrf.exception.SSRFUnsafeConnectionException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Response;

import java.io.IOException;
import java.net.InetAddress;

@Slf4j
public class OK3Interceptor implements Interceptor {
    private static final SSRFChecker ssrfChecker = SecurityUtil.getSSRFCheckerSingleton();
    public static final OK3Interceptor INSTANCE = new OK3Interceptor();

    @Override
    public Response intercept(Chain chain) throws IOException {
        String url = chain.request().url().toString();
        if (!ssrfChecker.checkUrlWithoutConnection(url)) {
            InetAddress address = null;
            try {
                address = InetAddress.getByName(chain.request().url().host());
            } catch (Exception e) {
                log.info("get name error", e);
            }
            log.warn("ssrf attack, url: {}, address: {}", url, address);
            throw new SSRFUnsafeConnectionException("unsafe url: " + url, address);
        }
        ssrfChecker.startNetHookWithExpirationCache(url);
        return chain.proceed(chain.request());
    }
}
