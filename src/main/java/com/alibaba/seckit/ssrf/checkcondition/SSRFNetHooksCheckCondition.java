package com.alibaba.seckit.ssrf.checkcondition;

import com.alibaba.seckit.ssrf.SSRFNetHooksEventListener;

import java.net.InetAddress;

/**
 * Condition for {@link SSRFNetHooksEventListener#check(InetAddress)} checking.
 *
 * @author renyi.cry
 * @date 17/12/26 下午9:43
 */
public interface SSRFNetHooksCheckCondition {

    boolean needsCheck(InetAddress address);

}
