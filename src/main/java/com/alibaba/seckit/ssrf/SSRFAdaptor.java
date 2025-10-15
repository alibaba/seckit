package com.alibaba.seckit.ssrf;

import com.alibaba.seckit.Decorator;
import com.alibaba.seckit.ssrf.decorator.*;
import com.google.auto.service.AutoService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;

@AutoService(HttpClientsAdaptor.class)
public class SSRFAdaptor implements HttpClientsAdaptor {

    private volatile List<Decorator> decorators = null;

    @Override
    public <T> T withSSRFChecking(T clientOrBuilder) {
        if (decorators == null) {
            synchronized (this) {
                if (decorators == null) {
                    List<Decorator> tmpDecorators = new ArrayList<>();
                    try {
                        for (Decorator decorator : ServiceLoader.load(Decorator.class, Decorator.class.getClassLoader())) {
                            tmpDecorators.add(decorator);
                        }
                    } catch (Throwable t) {
                        tmpDecorators.clear();
                    }
                    // fall back to load decorators
                    if (tmpDecorators.isEmpty()) {
                        tmpDecorators.addAll(Arrays.asList(
                                new HttpClientDecorator(),
                                new HttpClient5Decorator(),
                                new HttpAsyncClientDecorator(),
                                new HttpAsyncClient5Decorator(),
                                new OkHttpDecorator(),
                                new OkHttp3Decorator()
                        ));
                    }
                    decorators = tmpDecorators;
                }
            }
        }

        for (Decorator decorator : decorators) {
            if (decorator.accept(clientOrBuilder)) {
                return decorator.decorate(clientOrBuilder);
            }
        }
        throw new RuntimeException("withSSRFChecking(" + clientOrBuilder.getClass().getName() + ") is not supported");

    }
}
