package com.alibaba.seckit.ssrf.interceptor;

import com.alibaba.seckit.SecurityUtil;
import com.alibaba.seckit.ssrf.SSRFChecker;
import com.alibaba.seckit.ssrf.exception.SSRFUnsafeConnectionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;

import java.io.IOException;
import java.net.InetAddress;

@Slf4j
public class HttpClientInterceptor implements HttpRequestInterceptor {
    private final SSRFChecker ssrfChecker = SecurityUtil.getSSRFCheckerSingleton();

    public static final HttpClientInterceptor INSTANCE = new HttpClientInterceptor();

    @Override
    public void process(HttpRequest httpRequest, HttpContext httpContext) throws HttpException, IOException {
        HttpHost host = HttpCoreContext.adapt(httpContext).getTargetHost();
        String url = host.toURI();
        if (!ssrfChecker.checkUrlWithoutConnection(url)) {
            InetAddress address = null;
            try {
                address = InetAddress.getByName(host.getHostName());
            } catch (Exception e) {
                log.info("get name error", e);
            }
            log.warn("ssrf attack, url: {}, address: {}", url, address);
            throw new SSRFUnsafeConnectionException("unsafe url: " + host.toURI(), address);
        }
        // prevent dns rebinding.
        ssrfChecker.startNetHookWithExpirationCache(url);
    }
}
