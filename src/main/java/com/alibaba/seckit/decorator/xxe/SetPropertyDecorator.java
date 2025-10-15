package com.alibaba.seckit.decorator.xxe;

import javax.xml.XMLConstants;
import java.lang.reflect.Method;


public class SetPropertyDecorator extends AbstractXxeDecorator {

    private static final Class<?> sfClass = classForNameQuietly("javax.xml.validation.SchemaFactory");
    private static final Class<?> validatorClass = classForNameQuietly("javax.xml.validation.Validator");
    private static final Class<?> tfClass = classForNameQuietly("javax.xml.transform.TransformerFactory");

    private static final Class<?>[] CLASSES = {sfClass, validatorClass, tfClass};

    @Override
    public boolean accept(Object clientOrBuilder) {
        return getInterface(CLASSES, clientOrBuilder) != null;
    }

    @Override
    public <T> T decorate(T builder) {
        Class<?> builderInterFace = getInterface(CLASSES, builder);

        boolean isTf = tfClass != null && tfClass.isInstance(builder);
        try {
            String methodName = isTf ? "setAttribute" : "setProperty";
            Method m = builderInterFace.getMethod(methodName, String.class, Object.class);
            m.setAccessible(true);
            m.invoke(builder, XMLConstants.ACCESS_EXTERNAL_DTD, "");
            if (isTf) {
                m.invoke(builder, XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            }
            else {
                m.invoke(builder, XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            }
        } catch (Exception e) {
            throw new RuntimeException("can not decorate builder " + builder.getClass().getName(), e);
        }
        return builder;
    }


}