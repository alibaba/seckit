package com.alibaba.seckit.ssrf.checkcondition;

import com.alibaba.seckit.ssrf.SSRFNetHooksEventListener;

import java.net.InetAddress;

/**
 * Condition for {@link SSRFNetHooksEventListener#check(InetAddress)} checking.
 *
 * @author renyi.cry
  */
public interface SSRFNetHooksCheckCondition {

    boolean needsCheck(InetAddress address);

}
