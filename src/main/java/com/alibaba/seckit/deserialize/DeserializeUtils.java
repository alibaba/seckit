package com.alibaba.seckit.deserialize;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.*;
import java.util.regex.Pattern;

public final class DeserializeUtils {

    private DeserializeUtils() {
    }

    private static final Class<?>[] INTERNAL_ALLOWED_CLASSES = {
            String.class,
            Integer.class,
            Long.class,
            Byte.class,
            Short.class,
            Float.class,
            Double.class,
            Character.class,
            Boolean.class,
            Number.class,
            BigInteger.class,
            BigDecimal.class,

            AtomicBoolean.class,
            AtomicInteger.class,
            AtomicLong.class,
            AtomicIntegerArray.class,
            AtomicLongArray.class,

            SimpleDateFormat.class,
            Date.class,
            GregorianCalendar.class,
            SimpleTimeZone.class,
            TimeZone.class,
            DecimalFormat.class,

            Currency.class,
            Locale.class,
            StringBuffer.class,
            StringBuilder.class,
            Pattern.class,
            UUID.class,

            HashMap.class,
            Map.class,
            Map.Entry.class,
            ConcurrentHashMap.class,
            HashSet.class,
            ArrayList.class,
            LinkedList.class,
            Properties.class,
    };

    private static final Set<String> INTERNAL_ALLOWED_CLASS_NAMES;

    static {
        Set<String> names = new HashSet<>();
        for (Class<?> clazz : INTERNAL_ALLOWED_CLASSES) {
            allowClass(names, clazz);
        }
        names.add("sun.util.calendar.ZoneInfo");
        // Collections allocate Object[] storage; each contained object is filtered independently.
        names.add("java.lang.Object");
        // ConcurrentHashMap writes these compatibility fields explicitly, despite their static/transient declarations.
        names.addAll(Arrays.asList(
                "java.util.concurrent.ConcurrentHashMap$Segment",
                "java.util.concurrent.locks.ReentrantLock",
                "java.util.concurrent.locks.ReentrantLock$NonfairSync",
                "java.util.concurrent.locks.ReentrantLock$Sync",
                "java.util.concurrent.locks.AbstractQueuedSynchronizer",
                "java.util.concurrent.locks.AbstractOwnableSynchronizer"
        ));
        INTERNAL_ALLOWED_CLASS_NAMES = Collections.unmodifiableSet(names);
    }

    static String generateFilterPattern(String[] allowedPatterns, Class<?>[] allowedClasses) {
        HashSet<String> set = new HashSet<>();
        if (allowedPatterns != null) {
            set.addAll(Arrays.asList(allowedPatterns));
        }
        set.addAll(INTERNAL_ALLOWED_CLASS_NAMES);

        if (allowedClasses != null) {
            for (Class<?> clazz : allowedClasses) {
                allowClass(set, clazz);
            }
        }

        set.add("maxdepth=1000");
        StringJoiner joiner = new StringJoiner(";");
        for (String s : set) {
            joiner.add(s);
        }
        return joiner.toString();
    }

    private static void allowClass(Set<String> set, Class<?> clazz) {
        if (clazz == null || clazz == Object.class) {
            return;
        }
        if (clazz.isArray()) {
            do {
                clazz = clazz.getComponentType();
            } while (clazz.isArray());
        }
        if (clazz.isPrimitive()) {
            return;
        }

        String name = clazz.getName();
        if (set.contains(name)) {
            return;
        }
        set.add(name);
        allowClass(set, clazz.getSuperclass());
        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                continue;
            }
            Class<?> fieldCls = field.getType();
            if (fieldCls.isPrimitive() || fieldCls == Object.class) {
                continue;
            }
            allowClass(set, field.getType());
        }
    }
}
