package com.alibaba.seckit.ssrf.policy;

/**
 * @author renyi.cry
 * @date 2018/4/2上午11:45
 */
public class SsrfNetHookPolicyFactory {

    public static SsrfNetHookStarter getExpiredCachedBasedSsrfNetHookStarter() {
        return new ExpiredCachedBasedSsrfNetHookStarter();
    }

    public static SsrfNetHookStarter getThreadLocalBasedSsrfNetHookStarter() {
        return new ThreadLocalBasedSsrfNetHookStarter();
    }

}
