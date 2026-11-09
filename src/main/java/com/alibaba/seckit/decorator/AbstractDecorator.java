package com.alibaba.seckit.decorator;

import com.alibaba.seckit.util.ClassUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class AbstractDecorator implements Decorator {
    protected static void invokeMethod(Object obj, String name, Class<?>[] parameterTypes, Object... parameters) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = obj.getClass().getMethod(name, parameterTypes);
        method.invoke(obj, parameters);
    }

    protected static Class<?> classForNameQuietly(String name) {
        return ClassUtil.classForNameQuietly(name);
    }

}
