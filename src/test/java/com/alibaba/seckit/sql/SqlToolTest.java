package com.alibaba.seckit.sql;

import com.alibaba.seckit.SecurityUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SqlToolTest {

    private void assertEscape(String raw, String expected) {
        assertEquals(expected, SecurityUtil.escapeSql(raw));
    }

    @Test
    public void testEscapeString() {
        assertEscape("abc 123 xxx", "abc 123 xxx");
        assertEscape("abc' 123", "abc\\' 123");
        assertEscape("abc\" 123", "abc\\\" 123");
        assertEscape("abc'\"\\ 123\nfoobar", "abc\\'\\\"\\\\ 123\\\nfoobar");
    }

    @Test
    public void testEscapeEdgeCases() {
        assertEscape(null, null);
        assertEscape("", "");
        assertEscape("\0", "\\\0");
        assertEscape("a\0b", "a\\\0b");
        assertEscape("\u001a", "\\Z");
        assertEscape("a\u001ab", "a\\Zb");
        assertEscape("\u001a\u001a", "\\Z\\Z");
        assertEscape("\\Z", "\\\\Z");
        assertEscape("\\\u001a", "\\\\" + "\\Z");
    }

    @Test
    public void testTrimEdgeCases() {
        assertEquals(null, SecurityUtil.trimSql(null));
        assertEquals("", SecurityUtil.trimSql(""));
        assertEquals("ab", SecurityUtil.trimSql("a\0\u001ab"));
        assertEquals(null, SecurityUtil.trimSql4OrderBy(null));
        assertEquals("", SecurityUtil.trimSql4OrderBy(""));
        assertEquals("ab", SecurityUtil.trimSql4OrderBy("a\0\u001ab"));
    }

    @Test
    public void testTrimRemovesConsecutiveHyphens() {
        for (String run : new String[]{"--", "---", "----", "-----"}) {
            assertEquals("", SecurityUtil.trimSql(run));
            assertEquals("", SecurityUtil.trimSql4OrderBy(run));
            assertEquals("ab", SecurityUtil.trimSql("a" + run + "b"));
            assertEquals("ab", SecurityUtil.trimSql4OrderBy("a" + run + "b"));
        }
        for (String input : new String[]{"a-#-b", "a-\n-b", "a-\0-b", "a-/*-*/b"}) {
            assertEquals("ab", SecurityUtil.trimSql(input));
            assertEquals("ab", SecurityUtil.trimSql4OrderBy(input));
        }
        assertEquals("id  ", SecurityUtil.trimSql4OrderBy("id -- "));
        assertEquals("id", SecurityUtil.trimSql("id -- "));
        assertEquals("-a-b-", SecurityUtil.trimSql("-a-b-"));
        assertEquals("-a-b-", SecurityUtil.trimSql4OrderBy("-a-b-"));
        assertEquals("ab", SecurityUtil.trimSql("a- -b"));
        assertEquals("a- -b", SecurityUtil.trimSql4OrderBy("a- -b"));
        assertEscape("a--b", "a--b");
    }

    @Test
    public void testTrimSql() {
        assertEquals("abc..-_345.lFOO", SecurityUtil.trimSql("abc..<><>-_ #$%345()*&{}::>?./[;l'FOO"));
        assertEquals(".lFOO", SecurityUtil.trimSql("<?./[; l'FOO"));
        assertEquals("FOO-_.", SecurityUtil.trimSql("FOO&() (-()_@#*.(@("));
        assertEquals("FOO-中文_测试.啊啊啊", SecurityUtil.trimSql("FOO&() (-(中文)_测试@#*.(@(啊啊啊"));
    }

    @Test
    public void trimSql4OrderBy() {
        assertEquals("abc..-_ 34,5 .lFOO", SecurityUtil.trimSql4OrderBy("abc..<><>-_ #$%34,5( )*&{}::>?./[;l'FOO"));
        assertEquals(". lFOO", SecurityUtil.trimSql4OrderBy("<?./[; l'FOO"));
        assertEquals("FOO -_,.", SecurityUtil.trimSql4OrderBy("FOO&() (-()_@,#*.(@("));
        assertEquals("FOO -中文_测试.啊啊啊", SecurityUtil.trimSql4OrderBy("FOO&() (-(中文)_测试@#*.(@(啊啊啊"));
    }

}
