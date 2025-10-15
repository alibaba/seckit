package com.alibaba.seckit.ssrf.interceptor;

import com.alibaba.seckit.SecurityUtil;
import com.alibaba.seckit.ssrf.SSRFChecker;
import com.alibaba.seckit.ssrf.exception.SSRFUnsafeConnectionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.async.AsyncExecCallback;
import org.apache.hc.client5.http.async.AsyncExecChain;
import org.apache.hc.client5.http.async.AsyncExecChainHandler;
import org.apache.hc.client5.http.classic.ExecChain;
import org.apache.hc.client5.http.classic.ExecChainHandler;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * HttpComponents 5 里面 request interceptor 在端口连接之后的，会被用来探测端口是否存活，
 * 所以必须用 execChain，位置在connect之前，保证每次跳转 Connect 前都能执行到
 */
@Slf4j
public class HttpClient5Interceptor implements ExecChainHandler, AsyncExecChainHandler {

    public static final HttpClient5Interceptor INSTANCE = new HttpClient5Interceptor();
    private final SSRFChecker ssrfChecker = SecurityUtil.getSSRFCheckerSingleton();

    private void startNetHookChecker(HttpRequest httpRequest) {
        URI uri = null;
        try {
            uri = httpRequest.getUri();
        } catch (URISyntaxException e) {
            log.warn("ssrfchecker get uri error", e);
            throw new SSRFUnsafeConnectionException("get uri error" + e.getMessage(), null);
        }
        String url = uri.toString();
        if (!ssrfChecker.checkUrlWithoutConnection(url)) {
            InetAddress address = null;
            try {
                address = InetAddress.getByName(uri.getHost());
            } catch (Exception e) {
                log.info("get name error", e);
            }
            log.warn("ssrf attack, url: {}, address: {}", url, address);
            throw new SSRFUnsafeConnectionException("unsafe url: " + uri, address);
        }
        ssrfChecker.startNetHookWithExpirationCache(uri.toString());
    }

    @Override
    public ClassicHttpResponse execute(ClassicHttpRequest request, ExecChain.Scope scope, ExecChain chain) throws IOException, HttpException {
        startNetHookChecker(request);
        return chain.proceed(request, scope);
    }

    @Override
    public void execute(HttpRequest request, AsyncEntityProducer entityProducer, AsyncExecChain.Scope scope, AsyncExecChain chain, AsyncExecCallback asyncExecCallback) throws HttpException, IOException {
        startNetHookChecker(request);
        chain.proceed(request, entityProducer, scope, asyncExecCallback);
    }
}
