package com.alibaba.seckit.ssrf.decorator;

import com.alibaba.seckit.Decorator;
import com.alibaba.seckit.AbstractDecorator;
import com.alibaba.seckit.ssrf.interceptor.HttpClientInterceptor;
import com.google.auto.service.AutoService;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;

import java.lang.reflect.InvocationTargetException;

@AutoService(Decorator.class)
public class HttpAsyncClientDecorator extends AbstractDecorator {

    static Class<?> clientClass = classForNameQuietly("org.apache.http.impl.nio.client.HttpAsyncClientBuilder");

    @Override
    public boolean accept(Object clientOrBuilder) {
        return clientClass != null && clientClass.isInstance(clientOrBuilder);
    }

    @Override
    public <T> T decorate(T clientOrBuilder) {
        if (clientOrBuilder instanceof HttpAsyncClientBuilder) {
            try {
                invokeMethod(clientOrBuilder,
                        "addInterceptorFirst",
                        new Class[] {HttpClientInterceptor.class.getInterfaces()[0]},
                        HttpClientInterceptor.INSTANCE);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException("can not decorate client builder", e);
            }
            return clientOrBuilder;
        } else {
            throw new RuntimeException("unexpected exception");
        }
    }
}
