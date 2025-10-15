package com.alibaba.seckit.xxe;

import com.alibaba.seckit.decorator.Decorator;
import com.alibaba.seckit.decorator.xxe.SetFeatureDecorator;
import com.alibaba.seckit.decorator.xxe.SetPropertyDecorator;
import com.alibaba.seckit.decorator.xxe.XMLInputFactoryDecorator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class XxeToolImpl implements XxeTool {

    private volatile List<Decorator> decorators = null;

    public <T> T withXxeProtection(T builder) {
        if (decorators == null) {
            synchronized (this) {
                if (decorators == null) {
                    // load all decorators manually
                    decorators = new ArrayList<>(Arrays.asList(
                            (Decorator)new SetFeatureDecorator(),
                            new XMLInputFactoryDecorator(),
                            new SetPropertyDecorator()
                    ));
                }
            }
        }

        for (Decorator decorator : decorators) {
            if (decorator.accept(builder)) {
                return decorator.decorate(builder);
            }
        }
        throw new RuntimeException("withXxeProtection(" + builder.getClass().getName() + ") is not supported");
    }
}
