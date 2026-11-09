package com.alibaba.seckit.ssrf;

import com.alibaba.seckit.SecurityUtil;
import com.alibaba.seckit.ssrf.exception.SSRFUnsafeConnectionException;
import com.alibaba.seckit.util.HttpServerForTest;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CheckURLTest {

    private static final String[] SPECIAL_LOCAL_ADDRESSES = {
            "fc00::", "fc00::1", "fd00::1", "fdff:ffff:ffff:ffff:ffff:ffff:ffff:ffff",
            "100.64.0.0", "100.64.0.1", "100.127.255.255",
            "198.18.0.0", "198.18.0.1", "198.19.255.255"
    };
    private static final String[] OUTSIDE_SPECIAL_RANGES = {
            "fbff:ffff:ffff:ffff:ffff:ffff:ffff:ffff", "fe00::",
            "100.63.255.255", "100.128.0.0", "198.17.255.255", "198.20.0.0"
    };

    @Test
    public void testCheckUrlWithoutConnection() {
        SSRFChecker checker = new SSRFCheckerImpl();
        boolean result;

        result = checker.checkUrlWithoutConnection("http://1.1.1.1/");
        assertTrue(result);
        result = checker.checkUrlWithoutConnection("http://10.0.1.1/");
        assertFalse(result);

        result = checker.checkUrlWithoutConnection("http://localhost/");
        assertFalse(result);
        result = checker.checkUrlWithoutConnection("http://" + HttpServerForTest.PUBLIC_FIXTURE_HOST + "/");
        assertTrue(result);

        // This method checks only the initial host; redirect interception is exercised by adaptor tests.
        result = checker.checkUrlWithoutConnection("http://" + HttpServerForTest.PUBLIC_FIXTURE_HOST
                + "/redirect-to?url=http://127.0.0.1/");
        assertTrue(result);
    }

    @Test
    public void testSpecialLocalAddressRangesAreBlockedByPrecheckAndNetHook() throws UnknownHostException {
        SSRFCheckerImpl checker = new SSRFCheckerImpl();
        SSRFNetHooksEventListener listener = new SSRFNetHooksEventListener(address -> true);
        for (String literal : SPECIAL_LOCAL_ADDRESSES) {
            InetAddress address = InetAddress.getByName(literal);
            assertFalse(literal, checker.checkUrlWithoutConnection(urlFor(literal)));
            assertFalse(literal, SSRFCheckerImpl.isAllowedAddress(address));
            assertNetHookRejects(listener, address);
        }
    }

    @Test
    public void testAddressesImmediatelyOutsideSpecialRangesRemainAllowed() throws UnknownHostException {
        SSRFCheckerImpl checker = new SSRFCheckerImpl();
        SSRFNetHooksEventListener listener = new SSRFNetHooksEventListener(address -> true);
        for (String literal : OUTSIDE_SPECIAL_RANGES) {
            InetAddress address = InetAddress.getByName(literal);
            assertTrue(literal, checker.checkUrlWithoutConnection(urlFor(literal)));
            assertTrue(literal, SSRFCheckerImpl.isAllowedAddress(address));
            listener.handleBeforeTcpConnect(new NetHooksEvent(address, 80));
        }
    }

    @Test
    public void testMappedIpv4SpecialRangesAreBlocked() throws UnknownHostException {
        SSRFCheckerImpl checker = new SSRFCheckerImpl();
        SSRFNetHooksEventListener listener = new SSRFNetHooksEventListener(address -> true);
        for (String literal : new String[]{"::ffff:100.64.0.1", "::ffff:198.18.0.1"}) {
            assertFalse(literal, checker.checkUrlWithoutConnection(urlFor(literal)));
            assertNetHookRejects(listener, InetAddress.getByName(literal));
        }
    }

    @Test
    public void testUnspecifiedIpv6SocketDestinationIsRejectedByNetHook() throws IOException {
        try (ServerSocket server = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
            for (String literal : new String[]{"::", "0:0:0:0:0:0:0:0", "::ffff:0.0.0.0"}) {
                for (boolean withUrl : new boolean[]{false, true}) {
                    if (withUrl) {
                        SecurityUtil.startSSRFNetHookChecking(urlFor(literal));
                    } else {
                        SecurityUtil.startSSRFNetHookChecking();
                    }
                    try (Socket socket = new Socket()) {
                        socket.connect(new InetSocketAddress(InetAddress.getByName(literal), server.getLocalPort()), 1000);
                        fail("NetHook must reject " + literal);
                    } catch (SSRFUnsafeConnectionException expected) {
                    } finally {
                        SecurityUtil.stopSSRFNetHookChecking();
                    }
                }
            }
        }
    }

    @Test
    public void testExistingLocalAddressProtectionAndNetHookConditionRemainUnchanged() throws UnknownHostException {
        SSRFCheckerImpl checker = new SSRFCheckerImpl();
        for (String literal : new String[]{"127.0.0.1", "10.0.0.1", "172.16.0.1", "192.168.0.1",
                "0.0.0.0", "169.254.0.1", "::1", "::", "fe80::1"}) {
            assertFalse(literal, checker.checkUrlWithoutConnection(urlFor(literal)));
            assertFalse(literal, SSRFCheckerImpl.isAllowedAddress(InetAddress.getByName(literal)));
        }
        SSRFNetHooksEventListener inactive = new SSRFNetHooksEventListener(address -> false);
        inactive.handleBeforeTcpConnect(new NetHooksEvent(InetAddress.getByName("fc00::1"), 80));
    }

    private static String urlFor(String literal) {
        return "http://" + (literal.indexOf(':') >= 0 ? "[" + literal + "]" : literal) + "/";
    }

    private static void assertNetHookRejects(SSRFNetHooksEventListener listener, InetAddress address) {
        try {
            listener.handleBeforeTcpConnect(new NetHooksEvent(address, 80));
            fail("NetHook must reject " + address.getHostAddress());
        } catch (SSRFUnsafeConnectionException expected) {
        }
    }
}
