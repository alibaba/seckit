package com.alibaba.seckit.ssrf.policy;

import com.alibaba.seckit.ssrf.SecurityNetHooksProvider;
import com.alibaba.seckit.ssrf.SSRFChecker;
import com.alibaba.seckit.ssrf.HostCachePolicy;
import com.alibaba.seckit.ssrf.SSRFNetHooksEventListener;
import com.alibaba.seckit.ssrf.checkcondition.ExpirationCacheSSRFNetHooksCheckCondition;
import com.alibaba.seckit.ssrf.checkcondition.SSRFNetHooksCheckCondition;
import com.alibaba.seckit.ssrf.checkcondition.ThreadLocalSSRFNetHooksCheckCondition;
import com.alibaba.seckit.ssrf.exception.NetHookUnsupportedException;
import com.alibaba.seckit.util.ExpiringSet;

/**
 * {@link CompatibleSsrfCheckerRegister} which registers the {@link SSRFChecker} to
 * the application to prevent the SSRF attack by dns rebinding.
 *
 * @author renyi.cry
 * @date 17/6/26 下午8:34
 */
public class CompatibleSsrfCheckerRegister {

    public static class SSRFNetHookCheckingFlagHolder {
        public static final ThreadLocal<Boolean> NEED_CHECKING = new ThreadLocal<Boolean>(){
            public Boolean initialValue() {
                return false;
            }
        };
    }

    public static final ExpiringSet<String> EXPIRING_SET = initExpiringSet();

    private static final String NET_HOOKS_PROVIDER_CLASS_PATH = "sun.net.NetHooks.Provider";

    private static volatile Boolean Initiated = false;

    /**
     * Start hooking checking.
     */
    public static void start() {
        initiate();
        SSRFNetHookCheckingFlagHolder.NEED_CHECKING.set(true);
    }

    /**
     * Start hooking checking.
     */
    public static void startCachedNetHooking(String host) {
        initiate();
        EXPIRING_SET.add(host);
    }


    private static void initiate() {

            synchronized (NET_HOOKS_PROVIDER_CLASS_PATH) {
                // Net hooks for jdk1.7 or above

                    if(!Initiated) {
                        try {
                            SSRFNetHooksEventListener listener = new SSRFNetHooksEventListener(getDefaultSSRFNetHooksCheckCondition());
                            SecurityNetHooksProvider.registerListener(listener);
                        }
                        catch (Throwable e) {
                            throw new NetHookUnsupportedException("SsrfChecker requires packages `sun.net` and `java.net` to be opened to the unnamed module.\n" +
                                    "Solution: Add these JVM arguments to your application's launch command:\n" +
                                    "--add-opens java.base/sun.net=ALL-UNNAMED\n" +
                                    "--add-opens java.base/java.net=ALL-UNNAMED");
                        } finally {
                            Initiated = true;
                        }
                    }
            }
    }

    private static SSRFNetHooksCheckCondition[] getDefaultSSRFNetHooksCheckCondition() {
        return new SSRFNetHooksCheckCondition[]{
                new ThreadLocalSSRFNetHooksCheckCondition(),
                new ExpirationCacheSSRFNetHooksCheckCondition(),
        };
    }

    /**
     * Stop hooking checking
     */
    public static void stop() {
        SSRFNetHookCheckingFlagHolder.NEED_CHECKING.set(false);
    }

    private static ExpiringSet<String> initExpiringSet() {
        return HostCachePolicy.expiringSet();
    }

}
