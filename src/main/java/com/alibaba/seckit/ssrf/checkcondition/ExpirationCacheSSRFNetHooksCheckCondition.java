package com.alibaba.seckit.ssrf.checkcondition;

import com.alibaba.seckit.ssrf.policy.CompatibleSsrfCheckerRegister;
import com.alibaba.seckit.util.InetAddressResolver;
import com.alibaba.seckit.util.IpFormatter;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.List;

/**
 * @author renyi.cry
  */
public class ExpirationCacheSSRFNetHooksCheckCondition implements SSRFNetHooksCheckCondition {

    @Override
    public boolean needsCheck(InetAddress address) {
        List<String> hostNames = InetAddressResolver.resolves(address);

        Iterator<String> iterator = CompatibleSsrfCheckerRegister.EXPIRING_SET.iterator();
        while (iterator.hasNext()) {
            String host = iterator.next();
            String formatJavaIp = IpFormatter.formatJavaIp(host);
            if (hostNames.contains(host.toLowerCase()) || hostNames.contains(formatJavaIp)) {
                return true;
            }
        }

        return false;
    }

}
