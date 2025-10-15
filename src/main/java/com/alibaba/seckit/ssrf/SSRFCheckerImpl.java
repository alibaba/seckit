package com.alibaba.seckit.ssrf;

import com.alibaba.seckit.ssrf.exception.SSRFUnsafeConnectionException;
import com.alibaba.seckit.ssrf.policy.CompatibleSsrfCheckerRegister;
import com.alibaba.seckit.ssrf.policy.SsrfNetHookPolicyFactory;
import com.alibaba.seckit.ssrf.policy.SsrfNetHookStarter;
import com.alibaba.seckit.util.InetAddressResolver;
import com.alibaba.seckit.util.UrlUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.*;

/**
 * 一个单例的SSRF检查器。
 * <p>
 * <p>
 * 本类的实现与接口方案有所不同，具体在于:
 * <ol>
 * <li>十进制和八进制都为数字或"."的格式，可以通过是否全是数字或"."的格式快速排除
 * <li><code>uri.getHost()</code>将阻止0x7f.1等非标准格式输入，故不再做独立的过滤判断
 * <li><code>InetAddress</code>默认解析ipv4和ipv6地址，只要判断返回类型即可，无需判断路径是否包含":"
 * </ol>
 * <p>
 * <p><b>注意:</b> 由于检查每次将尝试解析地址，如果用户输入一个不存在地址时，本类
 * 中的函数将产生严重阻塞，可能存在被Dos攻击的风险!
 *
 * @author renyi.cry
 */
@Slf4j
public class SSRFCheckerImpl implements SSRFChecker {
    private volatile String[] allowedProtocols = ALLOWED_PROTOCOLS;


    // ***********************************************************************************
    // public method
    // ***********************************************************************************

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
     * @param url
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
     * @param url
     * @return
     */
    public boolean checkUrlWithoutConnection(String url) {
        try {
            return checkUrlWithoutConnection(url, false);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return true;
        }

    }

    // ***********************************************************************************
    // private method
    // ***********************************************************************************

    /**
     * check if the {@InetAddress} is an allowed address.
     *
     * @param inetAddress
     * @return
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
     * @param url
     * @param isHost
     * @return
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
     * @param url
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
     * @param host
     * @return
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

    /**
     * 检查一个 InetAddress 是否是私有地址、回环地址或本地链接地址。
     * @param address 要检查的地址
     * @return 如果是私有地址则返回 true
     */
    private static boolean isPrivate(InetAddress address) {
        // isLoopbackAddress() 检查是否为 127.0.0.1 或 ::1
        // isSiteLocalAddress() 检查是否为私有地址段，如 10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16
        // isAnyLocalAddress() 检查是否为 0.0.0.0 或 ::0
        // isLinkLocalAddress() 检查是否为 169.254.0.0/16
        return address.isLoopbackAddress() || address.isSiteLocalAddress() || address.isAnyLocalAddress() || address.isLinkLocalAddress();
    }

}
