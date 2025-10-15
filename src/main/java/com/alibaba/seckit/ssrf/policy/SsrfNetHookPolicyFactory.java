package com.alibaba.seckit.ssrf.policy;

/**
 * @author renyi.cry
  */
public class SsrfNetHookPolicyFactory {

    public static SsrfNetHookStarter getExpiredCachedBasedSsrfNetHookStarter() {
        return new ExpiredCachedBasedSsrfNetHookStarter();
    }

    public static SsrfNetHookStarter getThreadLocalBasedSsrfNetHookStarter() {
        return new ThreadLocalBasedSsrfNetHookStarter();
    }

}
