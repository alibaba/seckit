package com.alibaba.seckit.util;

import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import sun.misc.Unsafe;

import java.lang.reflect.*;
import java.util.Map;

public abstract class ReflectionUtil {

    private static final Map<Pair<Class<?>, String>, Field> findFieldCache = new LRUCache<>(8);


    @SuppressWarnings("unchecked")
    public static <T> T getNestedFieldValue(final Object target, final Field... fields) {
        Object fieldValue = target;
        for (Field field : fields) {
            fieldValue = getFieldValue(field, fieldValue);
        }
        return (T)fieldValue;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(final Field field, final Object target) {
        boolean accessible = field.isAccessible();
        try {
            if (!accessible) {
                field.setAccessible(true);
            }
            return (T)field.get(target);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException(ex);
        } finally {
            if (!accessible) {
                field.setAccessible(false);
            }
        }
    }

    public static Field findField(Class<?> clazz, String name) {
        Field f = findFieldCache.get(new ImmutablePair<Class<?>, String>(clazz, name));
        if (f == null) {
            Class<?> current = clazz;
            while (!Object.class.equals(current) && current != null) {
                for (Field field : current.getDeclaredFields()) {
                    if (name.equals(field.getName())) {
                        findFieldCache.put(new ImmutablePair<>(clazz, name), field);
                        return field;
                    }
                }
                current = current.getSuperclass();
            }
        }
        return f;
    }

    public static void removeFinalModifierFromField(Field field)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        if (!Modifier.isFinal(field.getModifiers())) {
            return;
        }
        // for java 7-11, modify `modifiers` field directly
        if (SystemUtils.isJavaVersionAtMost(JavaVersion.JAVA_11)) {
            field.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            return;
        }
        removeFinalModifierFromFieldForJava12(field);
    }

    public static void removeFinalModifierFromFieldForJava12(Field field)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // for java 12+, we can not get modifiers field
        // so we need to use low-level API getDeclaredField0() to get modifiers field
        Method getDeclaredFields0 = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
        getDeclaredFields0.setAccessible(true);
        Object fields = getDeclaredFields0.invoke(Field.class, false);
        if (!(fields instanceof Field[])) {
            throw new IllegalStateException("Unexpected field type: " + fields.getClass());
        }
        for (Field f : (Field[]) fields) {
            if (f.getName().equals("modifiers")) {
                f.setAccessible(true);
                f.setInt(field, field.getModifiers() & ~Modifier.FINAL);
                break;
            }
        }
    }

    public static void setFinalField(Field field, Object target, Object value) throws Exception {
        if (SystemUtils.isJavaVersionAtMost(JavaVersion.JAVA_20)) {
            removeFinalModifierFromField(field);
            field.set(target, value);
        } else {
            setFinalFieldByUnsafe(field, target, value);
        }
    }

    public static void setFinalFieldByUnsafe(Field field, Object target, Object value) {
        long offset;
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Unsafe unsafe = (Unsafe) unsafeField.get(null);

            if (target == null) {
                target = unsafe.staticFieldBase(field);
                offset = unsafe.staticFieldOffset(field);
            } else {
                offset = unsafe.objectFieldOffset(field);
            }

            if (field.getType() == byte.class) {
                unsafe.putByte(target, offset, ((Number) value).byteValue());
            } else if (field.getType() == short.class) {
                unsafe.putShort(target, offset, ((Number) value).shortValue());
            } else if (field.getType() == int.class) {
                unsafe.putInt(target, offset, ((Number) value).intValue());
            } else if (field.getType() == long.class) {
                unsafe.putLong(target, offset, ((Number) value).longValue());
            } else if (field.getType() == float.class) {
                unsafe.putFloat(target, offset, ((Number) value).floatValue());
            } else if (field.getType() == double.class) {
                unsafe.putDouble(target, offset, ((Number) value).doubleValue());
            } else if (field.getType() == char.class) {
                unsafe.putChar(target, offset, (char) value);
            } else if (field.getType() == boolean.class) {
                unsafe.putBoolean(target, offset, (boolean) value);
            } else {
                unsafe.putObject(target, offset, value);
            }
        }
        catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
