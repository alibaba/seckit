package com.alibaba.seckit.decorator.xxe;


import java.lang.reflect.Method;

public class SetFeatureDecorator extends AbstractXxeDecorator {
    private static final Class<?> dbfClass = classForNameQuietly("javax.xml.parsers.DocumentBuilderFactory");
    private static final Class<?> saxReaderClass = classForNameQuietly("org.dom4j.io.SAXReader");
    private static final Class<?> spfClass = classForNameQuietly("javax.xml.parsers.SAXParserFactory");
    private static final Class<?> saxBuilderClass = classForNameQuietly("org.jdom2.input.SAXBuilder");
    private static final Class<?> xrClass = classForNameQuietly("org.xml.sax.XMLReader");
    private static final Class<?> jxrClass = classForNameQuietly("jdk.internal.org.xml.sax.XMLReader");

    private static final Class<?>[] CLASSES = new Class<?>[] {dbfClass, saxReaderClass, spfClass, saxBuilderClass, xrClass, jxrClass};

    @Override
    public boolean accept(Object clientOrBuilder) {
        return getInterface(CLASSES, clientOrBuilder) != null;
    }

    public <T> T decorate(T builder) {
        Class<?> builderInterFace = getInterface(CLASSES, builder);
        if (builderInterFace == null) {
            throw new RuntimeException("can not find builder interface " + builder.getClass().getName());
        }
        try {
            Method m = builderInterFace.getMethod("setFeature", String.class, boolean.class);
            m.setAccessible(true);
            m.invoke(builder, "http://apache.org/xml/features/disallow-doctype-decl", true);
            m.invoke(builder, "http://xml.org/sax/features/external-general-entities", false);
            m.invoke(builder, "http://xml.org/sax/features/external-parameter-entities", false);

            try {
                m.invoke(builder, "http://apache.org/xml/features/xinclude", false);
            } catch (Exception ignored){}

            // special case
            if ((xrClass != null && xrClass.isInstance(builder))
                    || (jxrClass != null && jxrClass.isInstance(builder))) {
                m.invoke(builder, "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            }

        } catch (Exception e) {
            throw new RuntimeException("can not decorate builder " + builder.getClass().getName(), e);
        }
        return builder;
    }
}
