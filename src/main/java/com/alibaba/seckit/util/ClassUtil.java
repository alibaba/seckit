package com.alibaba.seckit.util;

/**
 * @author mingyi
  */
public class ClassUtil {
    public static Class<?> classForNameQuietly(String name) {
        try {
            return Class.forName(name);
        } catch (Exception ignored) {}
        return null;
    }

    public static Class<?> classForNameQuietly(String name, ClassLoader classLoader) {
        try {
            return Class.forName(name, false, classLoader);
        } catch (Exception ignored) {}
        return null;
    }

    public static ClassLoader getCurrentClassLoader() {
        ClassLoader ret = null;
        try {
            ret = Thread.currentThread().getContextClassLoader();
        } catch (Exception ignored) {}
        if (ret != null) {
            return ret;
        }
        try {
            ret = ClassUtil.class.getClassLoader();
        } catch (Exception ignored) {}
        if (ret != null) {
            return ret;
        }
        try {
            ret = ClassLoader.getSystemClassLoader();
        } catch (Exception ignored) {}
        return ret;
    }
}
