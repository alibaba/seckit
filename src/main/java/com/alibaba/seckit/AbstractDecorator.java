package com.alibaba.seckit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class AbstractDecorator implements Decorator {
    protected static void invokeMethod(Object obj, String name, Class<?>[] parameterTypes, Object ...parameters) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method =  obj.getClass().getMethod(name, parameterTypes);
        method.invoke(obj, parameters);
    }

    protected static Class<?> classForNameQuietly(String name) {
        try {
            return Class.forName(name);
        } catch (Exception ignored) {}
        return null;
    }

}
