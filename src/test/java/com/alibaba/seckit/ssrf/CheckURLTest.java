package com.alibaba.seckit.ssrf;

import com.alibaba.seckit.util.IpAddressUtilForTest;
import org.apache.commons.lang3.SystemUtils;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CheckURLTest {

    @Test
    public void testCheckUrlWithoutConnection() {
        SSRFChecker checker = new SSRFCheckerImpl();
        boolean result;

        result = checker.checkUrlWithoutConnection("http://1.1.1.1/");
        assertTrue(result);
        result = checker.checkUrlWithoutConnection("http://10.0.1.1/");
        assertFalse(result);

        result = checker.checkUrlWithoutConnection("http://10.1.2.3.sslip.io/");
        assertFalse(result);
        result = checker.checkUrlWithoutConnection("http://www.baidu.com/");
        assertTrue(result);

        result = checker.checkUrlWithoutConnection("http://httpbin.org/redirect-to?url=https://1.1.1.1.sslip.io");
        assertTrue(result);


    }

}