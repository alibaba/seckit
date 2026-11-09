package com.alibaba.seckit.cli;

import com.alibaba.seckit.SecurityUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class CliToolTest {

    @Test
    public void testNullAndEmpty() {
        assertNull(SecurityUtil.trimCmd(null));
        assertEquals("", SecurityUtil.trimCmd(""));
        assertEquals("", SecurityUtil.trimCmd(" ;&|$()`\"'\\/\t\n\r\0"));
    }

    @Test
    public void testAsciiAllowlist() {
        String allowed = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-.,";
        assertEquals(allowed, SecurityUtil.trimCmd(allowed));
        assertEquals("aA09_-.,z", SecurityUtil.trimCmd(" aA09_-.,z "));
        assertEquals("Aa123.", SecurityUtil.trimCmd("Aa1&&$@^%!()2\"3#.\0"));
    }

    @Test
    public void testChineseCharactersArePreserved() {
        assertEquals("中文参数,测试文件.txt", SecurityUtil.trimCmd("中文参数,测试文件.txt"));
        assertEquals("abc中文_-.,123", SecurityUtil.trimCmd("abc 中文_-.,123"));
        assertEquals("测试.txt", SecurityUtil.trimCmd("'测试.txt'"));
    }

    @Test
    public void testChineseRangeBoundariesAndUnsupportedUnicode() {
        assertEquals("\u4e00\u9fbb", SecurityUtil.trimCmd("\u4dff\u4e00\u9fbb\u9fbc\u9fff"));
        assertEquals("中文", SecurityUtil.trimCmd("中の文あいうéＡ１\ud83d\ude00"));
        assertEquals("ab", SecurityUtil.trimCmd("a\ud800\udc00\ud800\udc00b"));
        assertEquals("ab", SecurityUtil.trimCmd("a\ud800b\udc00"));
    }

    @Test
    public void testControlCharactersAndSeparatorsAreRemoved() {
        assertEquals("abcdef", SecurityUtil.trimCmd("a\0b\tc\nd\re\u001af"));
        assertEquals("中文abcdef", SecurityUtil.trimCmd("中文 a;b&c|d$e`f`"));
        assertEquals("abcdef", SecurityUtil.trimCmd("a/b\\c:d=e+f"));
        assertEquals("ab", SecurityUtil.trimCmd("a\u00a0\u3000\u200bb"));
    }

    @Test
    public void testConsecutiveHyphensAndCommasFollowCommandAllowlist() {
        assertEquals("--name中文", SecurityUtil.trimCmd("--name=中文"));
        assertEquals("a--b,,c", SecurityUtil.trimCmd("a--b,,c"));
        assertEquals("arg1,arg2中文", SecurityUtil.trimCmd("arg1, arg2中文"));
    }

    @Test
    public void testEveryCharacterMatchesTheSpecifiedAllowlist() {
        CLITool tool = new CLIToolImpl();
        for (int value = Character.MIN_VALUE; value <= Character.MAX_VALUE; value++) {
            char c = (char) value;
            boolean allowed = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
                    || (c >= '0' && c <= '9') || c == '_' || c == '-' || c == ',' || c == '.'
                    || (c >= 0x4e00 && c <= 0x9fbb);
            String input = String.valueOf(c);
            assertEquals("character U+" + Integer.toHexString(value), allowed ? input : "", tool.filter(input));
        }
    }

    @Test
    public void testOutputIsStableUnderRepeatedFiltering() {
        String input = "'中文文件.txt';$(echo other) --flag,arg";
        String filtered = SecurityUtil.trimCmd(input);
        assertEquals(filtered, SecurityUtil.trimCmd(filtered));
        assertFalse(filtered.contains(" "));
        assertFalse(filtered.contains(";"));
        assertEquals(new CLIToolImpl().filter(input), filtered);
    }
}
