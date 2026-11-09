package com.alibaba.seckit.xss;

import com.alibaba.seckit.SecurityUtil;
import lombok.AllArgsConstructor;
import static org.junit.Assert.*;
import org.junit.Test;

import java.util.function.UnaryOperator;

public class XssToolTest {



    @AllArgsConstructor
    public static class TestCase {
        String input;
        String expected;
    }

    @Test
    public void testEscapeHtml() {
        TestCase[] testCases = new TestCase[]{
                new TestCase("img src=a", "img src=a"),
                new TestCase("img src='a'", "img src=&#39;a&#39;"),
                new TestCase("<xxxx>", "&lt;xxxx&gt;"),
                new TestCase("<a href=\"http://example.com/\">", "&lt;a href=&quot;http://example.com/&quot;&gt;"),
                new TestCase("text <> \"&nbsp;", "text &lt;&gt; &quot;&amp;nbsp;"),
                new TestCase(null, null),
                new TestCase("", ""),
        };
        test(testCases, SecurityUtil::escapeHtml);
    }

    @Test
    public void testEscapeJavascript() {
        TestCase[] testCases = new TestCase[]{
                new TestCase("abc(null)", "abc\\u0028null\\u0029"),
                new TestCase("1+1", "1\\u002B1"),
                new TestCase("a=123", "a\\u003D123"),
                new TestCase("[123]", "\\u005B123\\u005D"),
                new TestCase("{\"a\":\"b\"}", "\\u007B\\u0022a\\u0022\\u003A\\u0022b\\u0022\\u007D"),
                new TestCase(null, null),
                new TestCase("", ""),
        };
        test(testCases, SecurityUtil::escapeJavascript);
    }



    private void test(TestCase[] testCases, UnaryOperator<String> escape) {
        for (TestCase testCase : testCases) {
            assertEquals(testCase.expected, escape.apply(testCase.input));
        }
    }

}


