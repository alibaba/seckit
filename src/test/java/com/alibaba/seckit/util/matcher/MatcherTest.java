package com.alibaba.seckit.util.matcher;

import org.junit.Assert;
import org.junit.Test;

import java.util.Locale;

public class MatcherTest {

    @Test
    public void testNullPatternsAndNullInput() {
        StringMatcher matcher = new BasicSuffixMatcher(null);
        Assert.assertFalse(matcher.matches(null));
        Assert.assertFalse(matcher.matches("example.com"));
    }

    @Test
    public void testBasicMatch() {
        StringMatcher matcher = new BasicSuffixMatcher(new String[]{"example.com", "another.org", "*.wildcard.example.org"});
        Assert.assertTrue(matcher.matches("example.com"));
        Assert.assertTrue(matcher.matches("another.org"));
        Assert.assertFalse(matcher.matches("sub.example.com"));
        Assert.assertFalse(matcher.matches("notallowed.com"));
        Assert.assertTrue(matcher.matches(".wildcard.example.org"));
        Assert.assertTrue(matcher.matches("www.wildcard.example.org"));
        Assert.assertFalse(matcher.matches("wildcard.example.org"));
    }

    @Test
    public void testSubDomainMatch() {
        StringMatcher matcher = new SubDomainMatcher(new String[]{"example.com", "another.org", "*.wildcard.example.org", "*suffix.example.net"});
        Assert.assertTrue(matcher.matches("example.com"));
        Assert.assertTrue(matcher.matches("another.org"));
        Assert.assertFalse(matcher.matches("sub.example.com"));
        Assert.assertFalse(matcher.matches("notallowed.com"));
        Assert.assertTrue(matcher.matches(".wildcard.example.org"));
        Assert.assertTrue(matcher.matches("www.wildcard.example.org"));
        Assert.assertTrue(matcher.matches("wildcard.example.org"));
        Assert.assertFalse(matcher.matches("xwildcard.example.org"));

        Assert.assertTrue(matcher.matches("suffix.example.net"));
        Assert.assertTrue(matcher.matches("www.suffix.example.net"));
        Assert.assertTrue(matcher.matches("xsuffix.example.net"));
    }

    @Test
    public void testBasicSuffixMatcherIsCaseSensitive() {
        StringMatcher matcher = new BasicSuffixMatcher(new String[]{"Exact.Example.COM", "*.Wildcard.Example.ORG"});
        Assert.assertTrue(matcher.matches("Exact.Example.COM"));
        Assert.assertFalse(matcher.matches("exact.example.com"));
        Assert.assertTrue(matcher.matches("www.Wildcard.Example.ORG"));
        Assert.assertFalse(matcher.matches("www.wildcard.example.org"));
        Assert.assertFalse(matcher.matches("Wildcard.Example.ORG"));
    }

    @Test
    public void testExactPatternsMustBeExplicit() {
        for (boolean includeExactPattern : new boolean[]{false, true}) {
            String[] patterns = includeExactPattern
                    ? new String[]{"*.Example.COM", "Example.COM", "Exact.Example.ORG"}
                    : new String[]{"*.Example.COM", "Exact.Example.ORG"};
            for (boolean caseInsensitive : new boolean[]{false, true}) {
                StringMatcher matcher = new BasicSuffixMatcher(patterns, caseInsensitive);
                Assert.assertEquals(includeExactPattern, matcher.matches("Example.COM"));
                Assert.assertEquals(includeExactPattern && caseInsensitive, matcher.matches("example.com"));
                Assert.assertTrue(matcher.matches("sub.Example.COM"));
                Assert.assertEquals(caseInsensitive, matcher.matches("sub.example.com"));
                Assert.assertTrue(matcher.matches("Exact.Example.ORG"));
                Assert.assertEquals(caseInsensitive, matcher.matches("exact.example.org"));
                Assert.assertFalse(matcher.matches("evilExample.COM"));
            }
        }
    }

    @Test
    public void testSubDomainMatcherIsCaseInsensitive() {
        StringMatcher matcher = new SubDomainMatcher(new String[]{"Exact.Example.COM", "*.Wildcard.Example.ORG", "*Suffix.Example.NET"});
        Assert.assertTrue(matcher.matches("exact.example.com"));
        Assert.assertTrue(matcher.matches("EXACT.EXAMPLE.COM"));
        Assert.assertFalse(matcher.matches("sub.exact.example.com"));
        Assert.assertTrue(matcher.matches("WILDCARD.EXAMPLE.ORG"));
        Assert.assertTrue(matcher.matches("www.WILDCARD.EXAMPLE.ORG"));
        Assert.assertTrue(matcher.matches("SUFFIX.EXAMPLE.NET"));
        Assert.assertTrue(matcher.matches("xSUFFIX.EXAMPLE.NET"));
        Assert.assertFalse(matcher.matches("xWILDCARD.EXAMPLE.ORG"));
        Assert.assertFalse(matcher.matches("wildcard.example.org.evil.test"));
        Assert.assertFalse(matcher.matches("suffix.example.net.evil.test"));
    }

    @Test
    public void testMatchersUnderTurkishLocale() {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr", "TR"));
            StringMatcher matcher = new SubDomainMatcher(new String[]{"I.EXAMPLE.COM", "*.I.EXAMPLE.ORG", "*I.EXAMPLE.NET"});
            Assert.assertTrue(matcher.matches("i.example.com"));
            Assert.assertTrue(matcher.matches("I.EXAMPLE.COM"));
            Assert.assertTrue(matcher.matches("i.example.org"));
            Assert.assertTrue(matcher.matches("WWW.I.EXAMPLE.ORG"));
            Assert.assertTrue(matcher.matches("xi.example.net"));
            Assert.assertTrue(matcher.matches("XI.EXAMPLE.NET"));
            Assert.assertFalse(matcher.matches("\u0131.example.com"));
            Assert.assertFalse(matcher.matches("xi.example.org"));
            Assert.assertFalse(matcher.matches("i.example.org.evil.test"));

            StringMatcher basic = new BasicSuffixMatcher(new String[]{"I.EXAMPLE.COM", "*.I.EXAMPLE.ORG"});
            Assert.assertTrue(basic.matches("I.EXAMPLE.COM"));
            Assert.assertFalse(basic.matches("i.example.com"));
            Assert.assertTrue(basic.matches("WWW.I.EXAMPLE.ORG"));
            Assert.assertFalse(basic.matches("www.i.example.org"));
        } finally {
            Locale.setDefault(previous);
        }
    }

    @Test
    public void testSubDomainPatternExpansionDoesNotMutateInput() {
        String[] patterns = {null, "*.Example.ORG", "*Suffix.Example.NET", "Exact.Example.COM"};
        String[] original = patterns.clone();
        StringMatcher matcher = new SubDomainMatcher(patterns);
        Assert.assertArrayEquals(original, patterns);
        Assert.assertTrue(matcher.matches("example.org"));
        Assert.assertTrue(matcher.matches("sub.example.org"));
        Assert.assertTrue(matcher.matches("suffix.example.net"));
        Assert.assertTrue(matcher.matches("xsuffix.example.net"));
        Assert.assertFalse(matcher.matches("xexample.org"));
    }

    @Test
    public void testNullAndEmptyDomainPatterns() {
        Assert.assertFalse(new SubDomainMatcher(null).matches("example.com"));
        Assert.assertFalse(new SubDomainMatcher(new String[0]).matches("example.com"));
        StringMatcher matcher = new SubDomainMatcher(new String[]{null, "*.example.org"});
        Assert.assertFalse(matcher.matches(null));
        Assert.assertTrue(matcher.matches("EXAMPLE.ORG"));
        Assert.assertFalse(matcher.matches("example.com"));
    }
}


