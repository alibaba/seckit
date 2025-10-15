package com.alibaba.seckit.decorator.xxe;

import com.alibaba.seckit.decorator.AbstractDecorator;

public abstract class AbstractXxeDecorator extends AbstractDecorator {

    protected static Class<?> getInterface(Class<?>[] classes, Object builder) {
        for (Class<?> aClass : classes) {
            if (aClass != null && aClass.isInstance(builder)) {
                return aClass;
            }
        }
        return null;
    }
}
