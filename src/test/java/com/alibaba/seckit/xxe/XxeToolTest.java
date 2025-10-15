package com.alibaba.seckit.xxe;


import org.apache.commons.lang3.SystemUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;


/*
XXE Payload: https://github.com/swisskyrepo/PayloadsAllTheThings/tree/master/XXE%20Injection#exploiting-blind-xxe-to-exfiltrate-data-out-of-band
不同框架可利用的XXE payload： https://semgrep.dev/docs/cheat-sheets/java-xxe/#3e-saxreader
*/
public class XxeToolTest {


    private static final Map<String, String> xmlContentMap = new HashMap<>();

    @BeforeClass
    public static void before() {
        xmlContentMap.put("file.xml", "root:");
        xmlContentMap.put("simple.xml", "Doe");
        xmlContentMap.put("ssrf.xml", "Answer");
    }

    @Test
    public void testXXE() {
        Class<?>[] parserClasses = new Class[]{
                XmlParsers.Dom4jSaxReader.class,
                XmlParsers.DocumentBuilderFactoryParser.class,
                XmlParsers.SaxParserFactoryParser.class,
                XmlParsers.XMLInputFactoryParser.class,
                XmlParsers.SaxBuilderParser.class,
                XmlParsers.XmlReaderParser.class,
        };

        for (Class<?> parserClass : parserClasses) {
            for (String f : xmlContentMap.keySet()) {
                assertResultOrException(parserClass, f, xmlContentMap.get(f), false, false);
                assertResultOrException(parserClass, f, null, false, true);
            }
        }

        // test xinclude
        Class<?>[] xIncludeClasses = new Class[]{
                XmlParsers.Dom4jSaxReader.class,
                XmlParsers.DocumentBuilderFactoryParser.class,
                XmlParsers.SaxParserFactoryParser.class,
        };

        for (Class<?> parserClass : xIncludeClasses) {
            assertResultOrException(parserClass, "xinclude.xml", "root:", true, false);
            assertResultNotContains(parserClass, "xinclude.xml", "root:", true, true);
        }

        // Validator, TransformerFactory
        parserClasses = new Class[]{
                XmlParsers.ValidatorParser.class,
                XmlParsers.TransformerFactoryParser.class,

        };
        for (Class<?> parserClass : parserClasses) {
            for (String f : new String[] {"file.xml", "ssrf.xml"}) {
                assertResultOrException(parserClass, f, "", false, false);
                assertResultOrException(parserClass, f, null, false, true);
            }
        }

        // test SchemaFactory
        try {
            getXmlParseResult(XmlParsers.SchemaFactoryParser.class, "file.xml", false, false);
        } catch (Exception e) {
            if(SystemUtils.IS_OS_LINUX) {
                Assert.assertTrue(e.getMessage(), e.getMessage().contains("root:x:0:0"));
            } else {
                Assert.assertTrue(e.getMessage(), e.getMessage().contains("User Database") || e.getMessage().contains("Saw '##'"));
            }
        }
        try {
            getXmlParseResult(XmlParsers.SchemaFactoryParser.class, "file.xml", false, true);
            Assert.fail();
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw new RuntimeException(e);
            }
        }
        try {
            getXmlParseResult(XmlParsers.SchemaFactoryParser.class, "ssrf.xml", false, false);
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("Answer"));
        }
        try {
            getXmlParseResult(XmlParsers.SchemaFactoryParser.class, "ssrf.xml", false, true);
            Assert.fail();
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw new RuntimeException(e);
            }
        }

    }


    public String getXmlParseResult(Class<?> parserClass, String fileName, boolean xInclude, boolean secure) throws Exception {
        return ((XmlParsers.XmlParser) parserClass.newInstance()).parse(
                this.getClass().getResourceAsStream("/xxe/" + fileName),
                xInclude, secure
        );
    }

    public void assertResultOrException(Class<?> parserClass, String fileName, String content, boolean xInclude, boolean secure) {
        try {
            String result = getXmlParseResult(parserClass, fileName, xInclude, secure);
            if (content == null) {
                Assert.fail(String.format("test for %s should throw an exception, actual: %s.", parserClass.getName(), result));
            } else {
                Assert.assertTrue(
                        String.format(
                                "test for %s should return a string contains:%s, but actual: %s",
                                parserClass.getName(),
                                content,
                                result),
                        result.contains(content));
            }
        } catch (Exception e) {
            if (e instanceof RuntimeException || content != null) {
                e.printStackTrace();
                Assert.fail();
            }
        }
    }

    public void assertResultNotContains(Class<?> parserClass, String fileName, String content, boolean xInclude, boolean secure) {
        try {
            String result = getXmlParseResult(parserClass, fileName, xInclude, secure);
            Assert.assertFalse(
                    String.format(
                            "test for %s should return not a string contains:%s, but actual: %s",
                            parserClass.getName(),
                            content,
                            result),
                    result.contains(content));

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
