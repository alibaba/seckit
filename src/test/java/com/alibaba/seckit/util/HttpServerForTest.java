package com.alibaba.seckit.util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpServerForTest implements AutoCloseable {

    // TEST-NET-1 avoids real destinations; transport mapping sends requests only to loopback.
    public static final String PUBLIC_FIXTURE_HOST = "192.0.2.1";
    private HttpServer server;
    private ExecutorService executorService;
    private final int requestedPort;
    private final AtomicInteger forbiddenRequests = new AtomicInteger();

    public HttpServerForTest(int port) {
        this.requestedPort = port;
    }

    public synchronized void start() throws IOException {
        if (server != null) {
            return;
        }
        HttpServer candidate = HttpServer.create(new InetSocketAddress("127.0.0.1", requestedPort), 0);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            candidate.createContext("/", this::handle);
            candidate.setExecutor(executor);
            candidate.start();
            server = candidate;
            executorService = executor;
        } catch (RuntimeException e) {
            candidate.stop(0);
            executor.shutdownNow();
            throw e;
        }
    }

    public synchronized int getPort() {
        if (server == null) {
            throw new IllegalStateException("server is not started");
        }
        return server.getAddress().getPort();
    }

    public InetSocketAddress proxyAddress() {
        return new InetSocketAddress("127.0.0.1", getPort());
    }

    public String url(String path) {
        return "http://" + PUBLIC_FIXTURE_HOST + ":" + getPort() + path;
    }

    public String localUrl(String path) {
        return "http://127.0.0.1:" + getPort() + path;
    }

    public String redirectTo(String target) throws IOException {
        return url("/redirect?url=" + URLEncoder.encode(target, StandardCharsets.UTF_8.name()));
    }

    public String[] forbiddenUrls() {
        String suffix = ":" + getPort() + "/forbidden";
        return new String[]{
                "http://127.0.0.1" + suffix,
                "http://10.0.0.1" + suffix,
                "http://0.0.0.0" + suffix,
                "http://0" + suffix,
                "http://[::1]" + suffix,
                "http://[::]" + suffix
        };
    }

    public int getForbiddenRequestCount() {
        return forbiddenRequests.get();
    }

    public synchronized void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
        if (executorService != null) {
            executorService.shutdownNow();
            executorService = null;
        }
    }

    @Override
    public void close() {
        stop();
    }

    private void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            exchange.getResponseHeaders().set("X-Test-Host", exchange.getRequestHeaders().getFirst("Host"));
            if ("/redirect".equals(path)) {
                String query = exchange.getRequestURI().getRawQuery();
                if (query == null || !query.startsWith("url=")) {
                    exchange.sendResponseHeaders(400, -1);
                    return;
                }
                exchange.getResponseHeaders().set("Location", URLDecoder.decode(query.substring(4), StandardCharsets.UTF_8.name()));
                byte[] response = "redirect".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(302, response.length);
                try (OutputStream body = exchange.getResponseBody()) {
                    body.write(response);
                }
                return;
            }
            int status = 200;
            if ("/forbidden".equals(path)) {
                forbiddenRequests.incrementAndGet();
                status = 500;
            }
            byte[] response = path.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, response.length);
            try (OutputStream body = exchange.getResponseBody()) {
                body.write(response);
            }
        } finally {
            exchange.close();
        }
    }
}
