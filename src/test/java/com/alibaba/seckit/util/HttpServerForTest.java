package com.alibaba.seckit.util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class HttpServerForTest {
    
    private HttpServer server;
    private ExecutorService executorService;
    private int port;
    private boolean initialized = false;

    private HttpServerForTest() {
        this.port = 8080;
    }

    public HttpServerForTest(int port) {
        this.port = port;
    }

    private static final HttpServerForTest instance = new HttpServerForTest();

    public static void startSingleton() {
        try {
            instance.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    synchronized public void start() throws IOException {
        if (!initialized) {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", new SimpleHandler());
            executorService = Executors.newFixedThreadPool(10);
            server.setExecutor(executorService);
            server.start();
            initialized = true;
            Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
        }
    }

    synchronized public void stop() {
        if (!initialized) {
            return;
        }
        if (server != null) {
            server.stop(0);
        }
        if (executorService != null) {
            executorService.shutdown();
        }
    }
    

    static class SimpleHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = exchange.getRequestURI().getPath();
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
            exchange.close();
        }
    }
}