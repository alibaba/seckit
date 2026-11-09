package com.alibaba.seckit.ssrf;

import com.alibaba.seckit.ssrf.exception.SSRFUnsafeConnectionException;
import com.alibaba.seckit.ssrf.policy.CompatibleSsrfCheckerRegister;
import com.alibaba.seckit.ssrf.policy.SsrfNetHookPolicyFactory;
import com.alibaba.seckit.ssrf.policy.SsrfNetHookStarter;
import com.alibaba.seckit.util.InetAddressResolver;
import com.alibaba.seckit.util.UrlUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * SSRF checker; DNS lookups may block when a supplied hostname cannot be resolved.
 *
 * @author renyi.cry
 */
@Slf4j
public class SSRFCheckerImpl implements SSRFChecker {
    private volatile String[] allowedProtocols = ALLOWED_PROTOCOLS;


    /**
     * Start hooking checking.  {@link NetHooksEventListener} will be registered
     * to {@link SecurityNetHooksProvider} when invoking this method. Particularly,
     * to achieve performance improvements, ENABLE_NET_HOOKS will be true in the
     * meantime, whether {@link NetHooksEventListener} could be registered or not.
     */
    @Override
    public void startNetHookWithThreadLocal() {
        CompatibleSsrfCheckerRegister.start();
    }

    /**
     * Start hooking checking.  {@link NetHooksEventListener} will be registered
     * to {@link SecurityNetHooksProvider} when invoking this method. Particularly,
     * to achieve performance improvements, ENABLE_NET_HOOKS will be true in the
     * meantime, whether {@link NetHooksEventListener} could be registered or not.
     *
     * @param url to be checked
     */
    @Override
    public void startNetHookWithThreadLocal(String url) {

        SsrfNetHookStarter starter = SsrfNetHookPolicyFactory.getThreadLocalBasedSsrfNetHookStarter();

        startNetHooking(url, starter);

    }

    /**
     * Stop hooking checking
     */
    @Override
    public void stopNetHookWithThreadLocal() {

        SsrfNetHookStarter starter = SsrfNetHookPolicyFactory.getThreadLocalBasedSsrfNetHookStarter();

        starter.stop();

    }

    @Override
    public void startNetHookWithExpirationCache(String url) {

        startNetHooking(url, SsrfNetHookPolicyFactory.getExpiredCachedBasedSsrfNetHookStarter());

    }

    /**
     * Check if the input url is vulnerable to ssrf attack without connection.
     *
     * @param url to be checked
     * @return true if the url is safe, false otherwise
     */
    public boolean checkUrlWithoutConnection(String url) {
        try {
            return checkUrlWithoutConnection(url, false);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return true;
        }

    }

    /**
     * check if the inetAddress is an allowed address.
     *
     * @param inetAddress to be checked
     * @return true if the inetAddress is an allowed address, false otherwise
     */
    static boolean isAllowedAddress(InetAddress inetAddress) {

        if (inetAddress == null) {
            return false;
        }

        String host = InetAddressResolver.resolve(inetAddress);
        if (host != null && !UrlUtil.validateHost(host)) {
            return false;
        }

        return !isPrivate(inetAddress);
    }

    private void startNetHooking(String url, SsrfNetHookStarter starter) {

        if (url == null) {
            return;
        }

        // check protocol
        if (!checkprotocol(url)) {
            log.warn("ssrf attack, unsafe protocol for url: {}", url);
            throw new SSRFUnsafeConnectionException("[" + url + "]" + "Unsafe protocol for url.");
        }

        // parse url and get the host.
        String host = UrlUtil.parseUrl(url, false);
        if (host == null) {
            log.warn("ssrf attack, host is null: {}", url);
            throw new SSRFUnsafeConnectionException("[" + url + "]" + "The host is null.");
        }

        starter.setHost(host);

        if (!UrlUtil.validateHost(host)) {
            log.warn("ssrf attack, host not allowed: {}", url);
            throw new SSRFUnsafeConnectionException("[" + url + "]" + "The host is banned.");
        }

        starter.start();
    }

    /**
     * Check url without connection (visible for testing)
     *
     * @param url to be checked
     * @param isHost param url is host or not
     * @return true if the url is safe, false otherwise
     */
    boolean checkUrlWithoutConnection(String url, boolean isHost) {
        return checkUrlWithoutConnection0(url, isHost).isSafe();
    }

    SSRFResult checkUrlWithoutConnection0(String url, boolean isHost) {
        if (url == null) {
            return SSRFResult.ofNotSafe();
        }

        // check protocol
        if (!checkprotocol(url)) {
            return SSRFResult.ofNotSafe();
        }

        // parse url and get the host.
        String host = UrlUtil.parseUrl(url, isHost);
        if (host == null) {
            return SSRFResult.ofNotSafe();
        }

        // check whether host is valid or not
        if (!UrlUtil.validateHost(host)) {
            return SSRFResult.ofNotSafe();
        }

        // check if it contains a blocked ip.
        return checkSSRFBannedAddress(host);
    }


    /**
     * Check if it's protocol is allowed
     *
     * @param url to be checked
     * @return protocol is allowed or not
     */
    boolean checkprotocol(String url) {
        String theUrl = url.substring(0, Math.min(8, url.length())).toLowerCase();
        for (String protocol : allowedProtocols) {
            if (theUrl.startsWith(protocol)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Check if the address contains a banned ip.
     *
     * @param host to be checked
     * @return {@code SSRFResult.ofNotSafe()} if the address contains a banned ip or invalid, {@code SSRFResult.ofSafe()}
     */
    private static SSRFResult checkSSRFBannedAddress(String host) {
        if (host == null) {
            return SSRFResult.ofNotSafe();
        }
        try {
            //  Resolve host to IP address and then convert the result ips to a binary array.
            InetAddress[] addresses = InetAddress.getAllByName(host);

            if (addresses != null && addresses.length != 0) {
                for (InetAddress address : addresses) {
                    if (isPrivate(address)) {
                        return SSRFResult.ofNotSafe(address.getHostAddress());
                    }
                }
            } else {
                return SSRFResult.ofNotSafe(SSRFResult.NO_DNS_IP);
            }
        } catch (UnknownHostException e) {
            return SSRFResult.ofNotSafe();
        }
        return SSRFResult.ofSafe();
    }

    // ***********************************************************************************
    // deprecated method
    // ***********************************************************************************

    @Deprecated
    public void start() {
        startNetHookWithThreadLocal();
    }

    @Deprecated
    public void stop() {
        stopNetHookWithThreadLocal();
    }

    private static boolean isPrivate(InetAddress address) {
        if (address.isLoopbackAddress() || address.isSiteLocalAddress()
                || address.isAnyLocalAddress() || address.isLinkLocalAddress()
                || LocalAddressesHolder.ADDRESSES == null
                || LocalAddressesHolder.ADDRESSES.contains(address)) {
            return true;
        }
        // JDK local-address predicates omit IPv6 ULA, IPv4 CGNAT, and benchmarking ranges.
        byte[] bytes = address.getAddress();
        if (address instanceof Inet6Address) {
            return (bytes[0] & 0xfe) == 0xfc;
        }
        int first = bytes[0] & 0xff;
        int second = bytes[1] & 0xff;
        return (first == 100 && (second & 0xc0) == 64)
                || (first == 198 && (second & 0xfe) == 18);
    }

    // Snapshot once: JDK may rewrite unspecified destinations to local addresses before NetHook runs.
    private static final class LocalAddressesHolder {
        static final Set<InetAddress> ADDRESSES = loadLocalAddresses();

        private static Set<InetAddress> loadLocalAddresses() {
            Set<InetAddress> addresses = new HashSet<>();
            try {
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                if (interfaces != null) {
                    while (interfaces.hasMoreElements()) {
                        addresses.addAll(Collections.list(interfaces.nextElement().getInetAddresses()));
                    }
                }
            } catch (SocketException | SecurityException e) {
                log.warn("Unable to load local interface addresses; SSRF address checks will reject connections", e);
                // Cache the failure as well, without allowing unknown destinations or poisoning class initialization.
                return null;
            }
            return Collections.unmodifiableSet(addresses);
        }
    }

}
