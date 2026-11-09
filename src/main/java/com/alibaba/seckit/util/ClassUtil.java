package com.alibaba.seckit.util;

import lombok.extern.slf4j.Slf4j;

/**
 * @author mingyi
 */
@Slf4j
public class ClassUtil {
    public static Class<?> classForNameQuietly(String name) {
        try {
            return Class.forName(name);
        } catch (Exception e) {
            log.debug("Unable to load class {}", name, e);
        }
        return null;
    }

    public static Class<?> classForNameQuietly(String name, ClassLoader classLoader) {
        try {
            return Class.forName(name, false, classLoader);
        } catch (Exception e) {
            log.debug("Unable to load class {}", name, e);
        }
        return null;
    }

    public static ClassLoader getCurrentClassLoader() {
        ClassLoader ret = null;
        try {
            ret = Thread.currentThread().getContextClassLoader();
        } catch (Exception e) {
            log.debug("Unable to get the context class loader", e);
        }
        if (ret != null) {
            return ret;
        }
        try {
            ret = ClassUtil.class.getClassLoader();
        } catch (Exception e) {
            log.debug("Unable to get the library class loader", e);
        }
        if (ret != null) {
            return ret;
        }
        try {
            ret = ClassLoader.getSystemClassLoader();
        } catch (Exception e) {
            log.debug("Unable to get the system class loader", e);
        }
        return ret;
    }
}
