package com.alibaba.seckit.url;

import com.alibaba.seckit.SecurityUtil;
import org.junit.Assert;
import org.junit.Test;

public class UrlToolTest {

    @Test
    public void testAllowedAndExcludedDomains() {

        Assert.assertTrue(SecurityUtil.isRedirectUrlAllowed("http://sub.example.com/path", "*.example.com"));
        Assert.assertTrue(SecurityUtil.isRedirectUrlAllowed("https://example.com/path", "*.example.com"));
        Assert.assertTrue(SecurityUtil.isRedirectUrlAllowed("https://example.com:8443/path", "*.example.com"));

        String[] allowed = new String[]{"*.example.com", "example.org"};
        String[] excluded = new String[]{"*.bad.example.com"};

        Assert.assertTrue(SecurityUtil.isRedirectUrlAllowed("http://a.example.com", allowed, excluded));
        Assert.assertTrue(SecurityUtil.isRedirectUrlAllowed("http://example.com/test", allowed, excluded));
        Assert.assertTrue(SecurityUtil.isRedirectUrlAllowed("https://example.org/page", allowed, excluded));
        Assert.assertFalse(SecurityUtil.isRedirectUrlAllowed("https://x.example.org/page", allowed, excluded));

        // excluded subdomain
        Assert.assertFalse(SecurityUtil.isRedirectUrlAllowed("http://evil.bad.example.com/login", allowed, excluded));

        // not in allowed list
        Assert.assertFalse(SecurityUtil.isRedirectUrlAllowed("http://notallowed.com", allowed, excluded));

        // relativePath
        Assert.assertTrue(SecurityUtil.isRedirectUrlAllowed("/relative/path", null, null));
    }

    @Test
    public void testMalformedAndAmbiguousUrlsAreRejected() {
        String[] unsafe = {
                null, "", "relative/path", "https:evil.com", "http:", "http:///example.com",
                "/\\evil.com", "\\\\evil.com", "///evil.com", "http:\\\\evil.com",
                "http://evil.com\\.example.com", "http://evil.com\\@example.com",
                " https://evil.com", " https://example.com", "https://example.com ",
                "\t//evil.com", "/\r/evil.com", "https://exa\nmple.com", "/path\u0000",
                "/path\u007f", "/path\u0085", "\u00a0https://example.com", "/path\\part",
                "javascript:alert(1)", "javascript://example.com/alert(1)",
                "data:text/html,test", "ftp://example.com", "file:///etc/passwd",
                "http://user@example.com", "http://evil.com@example.com",
                "http://example.com:65536", "http://example.com:-1", "http://example.com:abc",
                "http://evil.com%2f.example.com", "http://%65xample.com",
                "http://example.com%5c.evil.com", "http://example.com%00", "http://[::1",
                "https://example.com/%zz"
        };
        for (String url : unsafe) {
            Assert.assertFalse("unsafe URL was allowed: " + url,
                    SecurityUtil.isRedirectUrlAllowed(url, "*.example.com"));
        }
    }

    @Test
    public void testRootedRelativePathsAndNetworkPathReferences() {
        for (String relative : new String[]{"/", "/relative/path", "/?next=https://evil.com", "/#//evil.com", "/path%20name"}) {
            Assert.assertTrue(relative, SecurityUtil.isRedirectUrlAllowed(relative, null, null));
        }
        Assert.assertTrue(SecurityUtil.isRedirectUrlAllowed("//example.com/path", "*.example.com"));
        Assert.assertFalse(SecurityUtil.isRedirectUrlAllowed("//evil.com/path", "*.example.com"));
        Assert.assertFalse(SecurityUtil.isRedirectUrlAllowed("//example.com/path", (String[]) null));
        Assert.assertFalse(SecurityUtil.isRedirectUrlAllowed("https://example.com", new String[0]));
        Assert.assertTrue(SecurityUtil.isRedirectUrlAllowed("HTTPS://SUB.EXAMPLE.COM:65535/path", "*.Example.com"));
    }

    @Test
    public void testExclusionsTakePrecedence() {
        String[] allowed = {"*.example.com"};
        String[] excluded = {"*.BAD.EXAMPLE.COM"};
        for (String url : new String[]{"https://bad.example.com", "//BAD.EXAMPLE.COM/path", "https://x.bad.example.com"}) {
            Assert.assertFalse(url, SecurityUtil.isRedirectUrlAllowed(url, allowed, excluded));
        }
    }
}


