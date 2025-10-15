package com.alibaba.seckit.ssrf.decorator;

import com.alibaba.seckit.Decorator;
import com.alibaba.seckit.AbstractDecorator;
import com.alibaba.seckit.ssrf.interceptor.OkHttpInterceptor;
import com.google.auto.service.AutoService;
import com.squareup.okhttp.OkHttpClient;

@AutoService(Decorator.class)
public class OkHttpDecorator extends AbstractDecorator {
    static Class<?> clientClass = classForNameQuietly("com.squareup.okhttp.OkHttpClient");

    @Override
    public boolean accept(Object clientOrBuilder) {
        return clientClass != null && clientClass.isInstance(clientOrBuilder);
    }

    @Override
    public <T> T decorate(T clientOrBuilder) {
        if (clientOrBuilder instanceof OkHttpClient) {
            ((OkHttpClient) clientOrBuilder).networkInterceptors().add(OkHttpInterceptor.INSTANCE);
            return clientOrBuilder;
        } else {
            throw new RuntimeException("unexpected exception");
        }
    }
}
