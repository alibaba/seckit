package com.alibaba.seckit.ssrf.decorator;

import com.alibaba.seckit.Decorator;
import com.alibaba.seckit.AbstractDecorator;
import com.alibaba.seckit.ssrf.interceptor.HttpClient5Interceptor;
import com.google.auto.service.AutoService;
import org.apache.hc.client5.http.impl.ChainElement;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;

import java.lang.reflect.InvocationTargetException;

@AutoService(Decorator.class)
public class HttpAsyncClient5Decorator extends AbstractDecorator {

    private static final Class<?> clientClass = classForNameQuietly("org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder");

    @Override
    public boolean accept(Object clientOrBuilder) {
        return clientClass != null && clientClass.isInstance(clientOrBuilder);
    }

    @Override
    public <T> T decorate(T clientOrBuilder) {
        if (clientOrBuilder instanceof HttpAsyncClientBuilder) {
            try {
                invokeMethod(clientOrBuilder,
                        "addExecInterceptorBefore",
                        new Class[] {
                                String.class,
                                String.class,
                                HttpClient5Interceptor.class.getInterfaces()[1]
                        },
                        ChainElement.CONNECT.name(),
                        "alibaba-ssrf-checker",
                        HttpClient5Interceptor.INSTANCE);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException("can not decorate client builder", e);
            }
            return clientOrBuilder;
        } else {
            throw new RuntimeException("unexpected exception");
        }
    }
}
