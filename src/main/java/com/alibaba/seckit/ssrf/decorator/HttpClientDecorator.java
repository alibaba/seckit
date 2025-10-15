package com.alibaba.seckit.ssrf.decorator;

import com.alibaba.seckit.Decorator;
import com.alibaba.seckit.AbstractDecorator;
import com.alibaba.seckit.ssrf.interceptor.HttpClientInterceptor;
import com.google.auto.service.AutoService;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.lang.reflect.InvocationTargetException;

@AutoService(Decorator.class)
public class HttpClientDecorator extends AbstractDecorator {

    static Class<?> clientClass = classForNameQuietly("org.apache.http.impl.client.HttpClientBuilder");
    static Class<?> defaultClientClass = classForNameQuietly("org.apache.http.impl.client.DefaultHttpClient");

    @Override
    public boolean accept(Object clientOrBuilder) {
        return (clientClass != null && clientClass.isInstance(clientOrBuilder)) ||
                (defaultClientClass != null && defaultClientClass.isInstance(clientOrBuilder));
    }

    @Override
    public <T> T decorate(T clientOrBuilder) {
        if (clientOrBuilder instanceof HttpClientBuilder) {
            try {
                invokeMethod(clientOrBuilder,
                        "addInterceptorFirst",
                        new Class[] {HttpClientInterceptor.class.getInterfaces()[0]},
                        HttpClientInterceptor.INSTANCE);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException("can not decorate client builder", e);
            }
            return clientOrBuilder;
        } else if (clientOrBuilder instanceof DefaultHttpClient) {
            try {
                invokeMethod(clientOrBuilder,
                        "addRequestInterceptor",
                        new Class[] {HttpClientInterceptor.class.getInterfaces()[0], int.class},
                        HttpClientInterceptor.INSTANCE, 0);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException("can not decorate client builder", e);
            }
            return clientOrBuilder;
        } else {
            throw new RuntimeException("unexpected exception");
        }
    }

}
