package com.alibaba.seckit.ssrf.decorator;

import com.alibaba.seckit.Decorator;
import com.alibaba.seckit.AbstractDecorator;
import com.alibaba.seckit.ssrf.interceptor.OK3Interceptor;
import com.google.auto.service.AutoService;
import okhttp3.OkHttpClient;

@AutoService(Decorator.class)
public class OkHttp3Decorator extends AbstractDecorator {
    static Class<?> clientClass = classForNameQuietly("okhttp3.OkHttpClient$Builder");

    @Override
    public boolean accept(Object clientOrBuilder) {
        return clientClass != null && clientClass.isInstance(clientOrBuilder);
    }

    @Override
    public <T> T decorate(T clientOrBuilder) {
        if (clientOrBuilder instanceof OkHttpClient.Builder) {
            ((OkHttpClient.Builder) clientOrBuilder).networkInterceptors().add(OK3Interceptor.INSTANCE);
            return clientOrBuilder;
        } else {
            throw new RuntimeException("unexpected exception");
        }
    }
}
