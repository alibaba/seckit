package com.alibaba.seckit.deserialize;

import com.alibaba.seckit.SecurityUtil;
import com.alibaba.seckit.deserialize.testcase.Basic;
import com.alibaba.seckit.deserialize.testcase.InnerClass;
import com.alibaba.seckit.deserialize.testcase.Nested;
import com.alibaba.seckit.deserialize.testcase.SubPackage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.Assert;
import org.junit.Test;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DeserializeToolTest {

    @AllArgsConstructor
    @Getter
    static class TestCase {
        Class<?> cls;
        String[] allowedPatterns;
        Class<?>[] allowedClasses;
        boolean shouldBlock;
    }

    @Test
    public void testDeserialize() throws IOException, ReflectiveOperationException {
        TestCase[] testCases = new TestCase[]{
                new TestCase(Basic.class, null, null, true),
                new TestCase(Basic.class, new String[]{Basic.class.getName()}, null, false),
                new TestCase(Basic.class, null, new Class[]{Basic.class}, false),
                new TestCase(Nested.class, null, new Class[]{Nested.class}, false),
                new TestCase(Nested.class, new String[]{Nested.class.getName()}, new Class[]{}, true),
                new TestCase(Nested.class, new String[]{"com.alibaba.seckit.deserialize.testcase.*"}, null, false),
                new TestCase(SubPackage.class, new String[]{"com.alibaba.seckit.deserialize.testcase.*"}, null, true),
                new TestCase(SubPackage.class, new String[]{"com.alibaba.seckit.deserialize.testcase.**"}, null, false),
                new TestCase(SubPackage.class, new String[]{}, new Class[]{SubPackage.class}, false),
                new TestCase(SubPackage.class, new String[]{"com.alibaba.seckit.deserialize.testcase.*", "com.alibaba.seckit.deserialize.testcase.allowed.*"}, null, true),
                new TestCase(SubPackage.class, new String[]{"com.alibaba.seckit.deserialize.testcase.*", "com.alibaba.seckit.deserialize.testcase.allowed.*", "com.alibaba.seckit.deserialize.testcase.blocked.*"}, null, false),
                new TestCase(InnerClass.class, new String[]{}, new Class[]{InnerClass.class}, false),
                new TestCase(InnerClass.class, new String[]{"com.alibaba.seckit.deserialize.testcase.*"}, null, false),
        };

        for (TestCase testCase : testCases) {
            assertDeserialize(testCase.cls, testCase.allowedPatterns, testCase.allowedClasses, testCase.shouldBlock);
        }
    }

    private void assertDeserialize(Class<?> cls, String[] allowedPatterns, Class<?>[] allowedClasses, boolean shouldBlock)
            throws IOException, ReflectiveOperationException {
        Object obj = cls.getDeclaredConstructor().newInstance();
        byte[] serialized;
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bytes)) {
            oos.writeObject(obj);
            oos.flush();
            serialized = bytes.toByteArray();
        }
        try (ByteArrayInputStream bytes = new ByteArrayInputStream(serialized);
             ObjectInputStream ois = newObjectInputStream(bytes, allowedPatterns, allowedClasses)) {
            try {
                ois.readObject();
                if (shouldBlock) {
                    Assert.fail("test case failed: " + cls.getName() + "#" + Arrays.toString(allowedPatterns) + "#" + Arrays.toString(allowedClasses));
                }
            } catch (InvalidClassException e) {
                if (!shouldBlock) {
                    throw e;
                }
            }
        }
    }

    private ObjectInputStream newObjectInputStream(ByteArrayInputStream bytes, String[] allowedPatterns, Class<?>[] allowedClasses)
            throws IOException {
        if (allowedPatterns != null && allowedClasses != null) {
            return SecurityUtil.newObjectInputStream(bytes, allowedPatterns, allowedClasses);
        } else if (allowedPatterns != null) {
            return SecurityUtil.newObjectInputStream(bytes, allowedPatterns);
        } else if (allowedClasses != null) {
            return SecurityUtil.newObjectInputStream(bytes, allowedClasses);
        } else {
            return SecurityUtil.newObjectInputStream(bytes);
        }
    }

    @Test
    public void testDefaultFilterIsCached() throws ReflectiveOperationException {
        DeserializeToolImpl tool = new DeserializeToolImpl();
        // The return type moves from sun.misc to java.io in the Java 9 overlay.
        Method factory = DeserializeToolImpl.class.getMethod("newObjectInputFilter", String[].class, Class[].class);
        Object defaultFilter = factory.invoke(tool, new Object[]{null, null});
        Assert.assertNotNull(defaultFilter);
        Assert.assertSame(defaultFilter, factory.invoke(tool, new Object[]{null, null}));
        Assert.assertSame(defaultFilter, factory.invoke(tool, new Object[]{new String[0], new Class<?>[0]}));
        Assert.assertSame(defaultFilter, factory.invoke(tool, new Object[]{null, new Class<?>[0]}));
        Assert.assertSame(defaultFilter, factory.invoke(tool, new Object[]{new String[0], null}));
        Assert.assertSame(defaultFilter, factory.invoke(new DeserializeToolImpl(), new Object[]{null, null}));
    }

    @Test
    public void testOnlyPersistentInstanceFieldsAreAllowed() throws IOException, ClassNotFoundException {
        Set<String> names = new HashSet<>(Arrays.asList(
                DeserializeUtils.generateFilterPattern(null, new Class<?>[]{PersistentModel.class}).split(";")));
        Assert.assertTrue(names.contains(PersistentValue.class.getName()));
        Assert.assertFalse(names.contains(StaticOnly.class.getName()));
        Assert.assertFalse(names.contains(TransientOnly.class.getName()));
        PersistentModel restored = (PersistentModel) roundTrip(new PersistentModel(), PersistentModel.class);
        Assert.assertEquals("persistent", restored.value.text);
        Assert.assertNull(restored.temporary);
    }

    @Test
    public void testDefaultAllowlistDoesNotReachReflectionInfrastructure() {
        Set<String> names = new HashSet<>(Arrays.asList(DeserializeUtils.generateFilterPattern(null, null).split(";")));
        Assert.assertFalse(names.contains("java.lang.Class"));
        Assert.assertFalse(names.contains("java.lang.ClassLoader"));
        Assert.assertFalse(names.contains("java.lang.reflect.Method"));
        Assert.assertFalse(names.contains("sun.misc.Unsafe"));
        Assert.assertTrue(names.contains("java.util.Hashtable"));
    }

    @Test
    public void testRecursiveFieldsAndArraysTerminate() {
        Set<String> names = new HashSet<>(Arrays.asList(
                DeserializeUtils.generateFilterPattern(null, new Class<?>[]{RecursiveModel.class}).split(";")));
        Assert.assertTrue(names.contains(RecursiveModel.class.getName()));
        Assert.assertTrue(names.contains(PersistentValue.class.getName()));
        Assert.assertFalse(names.contains(TransientOnly.class.getName()));
    }

    @Test
    public void testCustomSerializationNeedsExplicitExtraClasses() throws IOException, ClassNotFoundException {
        Set<String> names = new HashSet<>(Arrays.asList(
                DeserializeUtils.generateFilterPattern(null, new Class<?>[]{CustomSerializedModel.class}).split(";")));
        Assert.assertFalse(names.contains(TransientOnly.class.getName()));
        try {
            roundTrip(new CustomSerializedModel(), CustomSerializedModel.class);
            Assert.fail("a transient type written by custom serialization must be explicitly allowed");
        } catch (InvalidClassException expected) {
        }
        CustomSerializedModel restored = (CustomSerializedModel) roundTrip(
                new CustomSerializedModel(), CustomSerializedModel.class, TransientOnly.class);
        Assert.assertEquals("custom", restored.value.text);
    }

    @Test
    public void testDefaultContainerSerialization() throws IOException, ClassNotFoundException {
        Properties properties = new Properties();
        properties.setProperty("key", "value");
        Assert.assertEquals(properties, roundTrip(properties));
        ConcurrentHashMap<String, String> concurrent = new ConcurrentHashMap<>();
        concurrent.put("key", "value");
        Assert.assertEquals(concurrent, roundTrip(concurrent));
        Assert.assertArrayEquals(new Object[]{"value", 42}, (Object[]) roundTrip(new Object[]{"value", 42}));
        try {
            roundTrip(new Object[]{new TransientOnly()});
            Assert.fail("Object[] storage must not allow unapproved contained classes");
        } catch (InvalidClassException expected) {
        }
    }

    private Object roundTrip(Serializable value, Class<?>... allowedClasses) throws IOException, ClassNotFoundException {
        byte[] serialized;
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(value);
            output.flush();
            serialized = bytes.toByteArray();
        }
        try (ObjectInputStream input = SecurityUtil.newObjectInputStream(new ByteArrayInputStream(serialized), allowedClasses)) {
            return input.readObject();
        }
    }

    private static class PersistentModel implements Serializable {
        private static final long serialVersionUID = 1L;
        private static final StaticOnly shared = new StaticOnly();
        private final PersistentValue value = new PersistentValue();
        private transient TransientOnly temporary = new TransientOnly();
    }

    private static class PersistentValue implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String text = "persistent";
    }

    private static class StaticOnly implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    private static class TransientOnly implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String text = "custom";
    }

    private static class RecursiveModel implements Serializable {
        private static final long serialVersionUID = 1L;
        private RecursiveModel next;
        private PersistentValue[][] values;
        private transient TransientOnly temporary;
    }

    private static class CustomSerializedModel implements Serializable {
        private static final long serialVersionUID = 1L;
        private transient TransientOnly value = new TransientOnly();

        private void writeObject(ObjectOutputStream output) throws IOException {
            output.defaultWriteObject();
            output.writeObject(value);
        }

        private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
            input.defaultReadObject();
            value = (TransientOnly) input.readObject();
        }
    }
}
