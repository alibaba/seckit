package com.alibaba.seckit.ssrf.checkcondition;

import com.alibaba.seckit.ssrf.policy.CompatibleSsrfCheckerRegister;

import java.net.InetAddress;

/**
 * @author renyi.cry
 * @date 17/12/26 下午9:43
 */
public class ThreadLocalSSRFNetHooksCheckCondition implements SSRFNetHooksCheckCondition {

    @Override
    public boolean needsCheck(InetAddress address) {
        return CompatibleSsrfCheckerRegister.SSRFNetHookCheckingFlagHolder.NEED_CHECKING.get();
    }
}
