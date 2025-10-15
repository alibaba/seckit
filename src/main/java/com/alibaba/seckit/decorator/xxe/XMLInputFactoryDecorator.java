package com.alibaba.seckit.decorator.xxe;

import com.alibaba.seckit.decorator.AbstractDecorator;

import javax.xml.stream.XMLInputFactory;
import java.lang.reflect.Method;

public class XMLInputFactoryDecorator extends AbstractDecorator {

    private static final Class<?> xifClass = classForNameQuietly("javax.xml.stream.XMLInputFactory");

    private static Method setPropertyMethod;

    @Override
    public boolean accept(Object clientOrBuilder) {
        return xifClass != null && xifClass.isInstance(clientOrBuilder);
    }

    @Override
    public <T> T decorate(T clientOrBuilder) {
        if (xifClass == null) {
            throw new RuntimeException("expected exception");
        }
        try {
            if (setPropertyMethod == null) {
                setPropertyMethod = xifClass.getMethod("setProperty", String.class, Object.class);
                setPropertyMethod.setAccessible(true);
            }
            setPropertyMethod.invoke(clientOrBuilder, XMLInputFactory.SUPPORT_DTD, false);
            setPropertyMethod.invoke(clientOrBuilder, XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        } catch (Exception e) {
            throw new RuntimeException("can not decorate xif", e);
        }
        return clientOrBuilder;
    }
}
